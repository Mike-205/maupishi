package ke.co.maupishi.app.model

/**
 * A metric size for a local unit, supplied by the cook and never calculated.
 * The unit is expected to be metric; the validator checks that.
 */
data class ApproxMetric(
    val amount: Double,
    val unit: MeasureUnit,
)

/**
 * One line of a recipe's ingredient list. A null [quantity] means "to taste",
 * and such an ingredient has no [unit]. Order in the recipe's list is the
 * display order.
 */
data class Ingredient(
    val name: LocalizedText,
    val quantity: Double?,
    val unit: MeasureUnit?,
    /** Heading such as "For the stew". */
    val group: LocalizedText? = null,
    val approxMetric: ApproxMetric? = null,
    val prepNote: LocalizedText? = null,
    val substitution: LocalizedText? = null,
)
