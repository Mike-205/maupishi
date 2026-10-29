package ke.co.maupishi.app.model

/** A rough price range in Kenya shillings, with what it is based on. */
data class CostEstimate(
    val minKes: Int,
    val maxKes: Int,
    val basis: LocalizedText? = null,
)

/** A bundled photo and its description for screen readers. */
data class RecipeImage(
    val file: String,
    val alt: LocalizedText? = null,
)

/**
 * A recipe as the rest of the app sees it: both languages kept, the UI picks
 * one with [LocalizedText.resolve]. Built from a recipe file now, and from the
 * database from M1b.
 *
 * [id] is a stable slug that is never changed or reused once published.
 */
data class Recipe(
    val id: String,
    val source: RecipeSource,
    val verified: Boolean,
    val name: LocalizedText,
    val description: LocalizedText,
    val region: Region,
    /** The community the dish comes from, where known. */
    val community: String?,
    val categories: Set<Category>,
    val occasions: Set<Occasion>,
    val allergens: Set<Allergen>,
    val prepMinutes: Int,
    val cookMinutes: Int,
    val servings: Int,
    val cost: CostEstimate?,
    val safetyNote: LocalizedText?,
    val culturalNote: LocalizedText?,
    /** Who the recipe is from, for example a chef's kitchen. */
    val credit: String,
    val image: RecipeImage?,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
)
