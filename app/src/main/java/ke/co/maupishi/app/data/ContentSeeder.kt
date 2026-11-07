package ke.co.maupishi.app.data

import androidx.room.withTransaction
import ke.co.maupishi.app.content.ContentSource
import ke.co.maupishi.app.content.ParseResult
import ke.co.maupishi.app.content.RecipeParser
import ke.co.maupishi.app.model.Recipe
import ke.co.maupishi.app.model.RecipeSource
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The outcome of one seeding attempt. */
sealed interface SeedResult {
    /** The stored content version already matches the bundled one; nothing was read or written. */
    data object UpToDate : SeedResult

    data class Applied(val recipeCount: Int, val removedCount: Int) : SeedResult

    /** Nothing was changed. Each error says which file or check failed. */
    data class Failed(val errors: List<String>) : SeedResult
}

/**
 * Brings the database's bundled recipes in line with the bundled content files.
 *
 * It only ever creates, updates or deletes rows with `source = BUNDLED`, so
 * user recipes and favorites are never touched. All files are parsed and
 * validated before anything is written, and the writes happen in one
 * transaction, so a bad file leaves the previous content exactly as it was.
 * An unexpected database error is not caught here: the transaction rolls back
 * and the exception reaches the caller.
 */
class ContentSeeder(
    private val database: MaupishiDatabase,
    private val source: ContentSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /**
     * @param forceSync apply the files even when the content version is unchanged
     *   (debug builds use this, because placeholders change without a version bump)
     */
    suspend fun seed(forceSync: Boolean = false): SeedResult {
        val bundledVersion = try {
            source.contentVersion()
        } catch (e: IOException) {
            return SeedResult.Failed(listOf("manifest: ${e.message}"))
        } catch (e: IllegalArgumentException) {
            // Also covers a SerializationException from a damaged manifest.
            return SeedResult.Failed(listOf("manifest: ${e.message?.lineSequence()?.first()}"))
        }

        val storedVersion = database.contentMetaDao().get(VERSION_KEY)?.toIntOrNull()
        if (!forceSync && storedVersion == bundledVersion) return SeedResult.UpToDate

        val recipes = when (val parsed = readAndParse()) {
            is Parsed.Recipes -> parsed.recipes
            is Parsed.Errors -> return SeedResult.Failed(parsed.errors)
        }

        return database.withTransaction {
            val dao = database.recipeDao()
            val bundled = RecipeSource.BUNDLED.name
            val ids = recipes.map { it.id }

            // A file whose id belongs to a user or community recipe must not overwrite it.
            val clashes = ids.chunked(ID_CHUNK).flatMap { dao.idsNotFromSource(it, bundled) }
            if (clashes.isNotEmpty()) {
                return@withTransaction SeedResult.Failed(
                    clashes.map { "$it: a recipe with this id already exists and is not bundled" },
                )
            }

            recipes.forEach { dao.replace(it.toStored()) }

            val removed = dao.idsBySource(bundled) - ids.toSet()
            removed.forEach { dao.deleteByIdAndSource(it, bundled) }

            database.contentMetaDao().put(ContentMetaEntity(VERSION_KEY, bundledVersion.toString()))
            SeedResult.Applied(recipeCount = recipes.size, removedCount = removed.size)
        }
    }

    private sealed interface Parsed {
        data class Recipes(val recipes: List<Recipe>) : Parsed
        data class Errors(val errors: List<String>) : Parsed
    }

    private suspend fun readAndParse(): Parsed = try {
        withContext(ioDispatcher) {
            val recipes = mutableListOf<Recipe>()
            val errors = mutableListOf<String>()
            for (file in source.recipeFiles()) {
                when (val result = RecipeParser.parse(file.text, file.name, source::imageExists)) {
                    is ParseResult.Success -> recipes += result.recipe
                    is ParseResult.Failure -> errors += result.errors.map { "${file.name} $it" }
                }
            }
            if (errors.isEmpty()) Parsed.Recipes(recipes) else Parsed.Errors(errors)
        }
    } catch (e: IOException) {
        Parsed.Errors(listOf("recipe files: ${e.message}"))
    }

    private companion object {
        const val VERSION_KEY = "contentVersion"

        // Keeps each query well under SQLite's limit on bound parameters.
        const val ID_CHUNK = 500
    }
}
