package ke.co.maupishi.app.content

import kotlinx.serialization.Serializable

/*
 * The shape of one recipe file. These classes mirror the JSON and exist only
 * for parsing and validation; the rest of the app uses the `model` package.
 *
 * Enum-like fields are plain strings here so the validator can report every
 * unknown value in a file at once, instead of the parser stopping at the first.
 * Optional fields default to null, so a file may leave them out.
 */

@Serializable
data class LocalizedTextDto(
    val en: String,
    val sw: String? = null,
)

@Serializable
data class ApproxMetricDto(
    val amount: Double,
    val unit: String,
)

@Serializable
data class CostDto(
    val minKes: Int,
    val maxKes: Int,
    val basis: LocalizedTextDto? = null,
)

@Serializable
data class ImageDto(
    val file: String,
    val alt: LocalizedTextDto? = null,
)

@Serializable
data class IngredientDto(
    val name: LocalizedTextDto,
    val quantity: Double? = null,
    val unit: String? = null,
    val group: LocalizedTextDto? = null,
    val approxMetric: ApproxMetricDto? = null,
    /**
     * Set by the cook when a local unit has no metric equivalent on purpose.
     * Without it, a local unit must carry [approxMetric].
     */
    val approxMetricUnknown: Boolean = false,
    val prepNote: LocalizedTextDto? = null,
    val substitution: LocalizedTextDto? = null,
)

@Serializable
data class StepDto(
    val text: LocalizedTextDto,
    val timerSeconds: Int? = null,
)

@Serializable
data class RecipeDto(
    val id: String,
    val verified: Boolean = false,
    val name: LocalizedTextDto,
    val description: LocalizedTextDto,
    val region: String,
    val community: String? = null,
    val categories: List<String> = emptyList(),
    val occasions: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val prepMinutes: Int,
    val cookMinutes: Int,
    val servings: Int,
    val cost: CostDto? = null,
    val safetyNote: LocalizedTextDto? = null,
    val culturalNote: LocalizedTextDto? = null,
    val credit: String,
    val image: ImageDto? = null,
    val ingredients: List<IngredientDto>,
    val steps: List<StepDto>,
)
