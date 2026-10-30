package ke.co.maupishi.app.content

import ke.co.maupishi.app.model.Recipe
import kotlinx.serialization.json.Json

/** The outcome of reading one recipe file: a recipe, or every problem found. */
sealed interface ParseResult {
    data class Success(val recipe: Recipe) : ParseResult
    data class Failure(val errors: List<String>) : ParseResult
}

/**
 * Reads a recipe file's JSON text into a [Recipe]. It takes text rather than a
 * file or an Android asset, so it runs in plain JVM tests; reading the bundled
 * files is the seeder's job.
 *
 * The JSON must match [RecipeDto] exactly: an unknown or misspelled field is
 * an error, so a typo cannot silently drop data.
 */
object RecipeParser {

    fun parse(
        text: String,
        fileName: String,
        imageExists: (String) -> Boolean = { true },
    ): ParseResult {
        val dto = try {
            Json.decodeFromString<RecipeDto>(text)
        } catch (e: IllegalArgumentException) {
            // SerializationException is an IllegalArgumentException. Its message
            // can quote the JSON over many lines; the first line is the reason.
            return ParseResult.Failure(listOf("file: ${e.message?.lineSequence()?.first()}"))
        }

        val errors = RecipeValidator.validate(dto, fileName, imageExists)
        return if (errors.isEmpty()) ParseResult.Success(dto.toModel()) else ParseResult.Failure(errors)
    }
}
