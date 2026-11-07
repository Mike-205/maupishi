package ke.co.maupishi.app.content

import android.content.res.AssetManager
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One bundled recipe file: its name (the recipe id plus `.json`) and its text. */
data class ContentFile(val name: String, val text: String)

/**
 * Where the seeder gets the bundled content from. The app reads it from assets
 * ([AssetContentSource]); tests supply their own, so seeding can be tested
 * without an Android asset folder.
 *
 * Reading may throw [IOException], and a damaged manifest throws
 * [IllegalArgumentException]; the seeder reports both as a failed seed.
 */
interface ContentSource {
    /** The integer bumped whenever bundled content changes. */
    suspend fun contentVersion(): Int

    suspend fun recipeFiles(): List<ContentFile>

    fun imageExists(name: String): Boolean
}

/** The content manifest file: today only the version. */
@Serializable
internal data class ContentManifest(val contentVersion: Int)

/**
 * Reads the manifest and recipe files from the app's assets. Debug builds see
 * the placeholder recipes from `src/debug/assets` as well, because Android
 * merges the asset folders of the build type and `main`.
 */
class AssetContentSource(private val assets: AssetManager) : ContentSource {

    override suspend fun contentVersion(): Int = withContext(Dispatchers.IO) {
        Json.decodeFromString<ContentManifest>(readAsset(MANIFEST_PATH)).contentVersion
    }

    override suspend fun recipeFiles(): List<ContentFile> = withContext(Dispatchers.IO) {
        assets.list(RECIPES_DIR).orEmpty()
            .filter { it.endsWith(".json") }
            .sorted()
            .map { name -> ContentFile(name, readAsset("$RECIPES_DIR/$name")) }
    }

    override fun imageExists(name: String): Boolean =
        assets.list(IMAGES_DIR).orEmpty().contains(name)

    private fun readAsset(path: String): String =
        assets.open(path).bufferedReader().use { it.readText() }

    private companion object {
        // The manifest sits outside recipes/ so it is never mistaken for a recipe file.
        const val MANIFEST_PATH = "content-manifest.json"
        const val RECIPES_DIR = "recipes"
        const val IMAGES_DIR = "images"
    }
}
