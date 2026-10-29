package ke.co.maupishi.app.content

import ke.co.maupishi.app.model.Allergen
import ke.co.maupishi.app.model.Category
import ke.co.maupishi.app.model.MeasureUnit
import ke.co.maupishi.app.model.Occasion
import ke.co.maupishi.app.model.Region
import ke.co.maupishi.app.model.UnitGroup

/**
 * Checks one parsed recipe file against the rules in `docs/DATA_MODEL.md`
 * and reports every problem found, each as `path: message`. An empty list
 * means the file is valid.
 *
 * Rules that span several files, such as unique ids, are checked by the test
 * that scans all recipe files.
 */
internal object RecipeValidator {

    private val slug = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

    private val regions = Region.entries.map { it.name }.toSet()
    private val categories = Category.entries.map { it.name }.toSet()
    private val occasions = Occasion.entries.map { it.name }.toSet()
    private val allergens = Allergen.entries.map { it.name }.toSet()
    private val units = MeasureUnit.entries.map { it.name }.toSet()
    private val metricUnits =
        MeasureUnit.entries.filter { it.group == UnitGroup.METRIC }.map { it.name }.toSet()
    private val localUnits = MeasureUnit.entries.filter { it.isLocal }.map { it.name }.toSet()

    /**
     * @param fileName the file's name, which must be the recipe id plus `.json`
     * @param imageExists tells whether a named image file is bundled
     */
    fun validate(
        recipe: RecipeDto,
        fileName: String,
        imageExists: (String) -> Boolean = { true },
    ): List<String> {
        val errors = mutableListOf<String>()

        fun fail(path: String, message: String) {
            errors += "$path: $message"
        }

        // Swahili may be missing or blank (it falls back to English); English may not.
        fun checkText(path: String, text: LocalizedTextDto?) {
            if (text != null && text.en.isBlank()) fail(path, "English text is required")
        }

        fun checkCodes(path: String, values: List<String>, allowed: Set<String>, label: String) {
            values.forEachIndexed { index, value ->
                if (value !in allowed) fail("$path[$index]", "unknown $label \"$value\"")
            }
        }

        if (!slug.matches(recipe.id)) {
            fail("id", "must be lowercase letters, digits and single hyphens, got \"${recipe.id}\"")
        }
        if (fileName != "${recipe.id}.json") {
            fail("id", "must match the file name \"$fileName\"")
        }

        checkText("name", recipe.name)
        checkText("description", recipe.description)

        if (recipe.region !in regions) fail("region", "unknown region \"${recipe.region}\"")
        checkCodes("categories", recipe.categories, categories, "category")
        checkCodes("occasions", recipe.occasions, occasions, "occasion")
        checkCodes("allergens", recipe.allergens, allergens, "allergen")

        if (recipe.prepMinutes <= 0) fail("prepMinutes", "must be a positive number")
        if (recipe.cookMinutes <= 0) fail("cookMinutes", "must be a positive number")
        if (recipe.servings <= 0) fail("servings", "must be a positive number")

        recipe.cost?.let { cost ->
            if (cost.minKes <= 0) fail("cost.minKes", "must be a positive number")
            if (cost.maxKes < cost.minKes) fail("cost.maxKes", "must not be below minKes")
            checkText("cost.basis", cost.basis)
        }

        checkText("safetyNote", recipe.safetyNote)
        checkText("culturalNote", recipe.culturalNote)

        if (recipe.credit.isBlank()) fail("credit", "is required")

        recipe.image?.let { image ->
            if (image.file.isBlank()) {
                fail("image.file", "is required when an image is given")
            } else if (!imageExists(image.file)) {
                fail("image.file", "\"${image.file}\" is not bundled")
            }
            checkText("image.alt", image.alt)
        }

        if (recipe.ingredients.isEmpty()) fail("ingredients", "at least one is required")
        recipe.ingredients.forEachIndexed { index, ingredient ->
            val at = "ingredients[$index]"
            checkText("$at.name", ingredient.name)
            checkText("$at.group", ingredient.group)
            checkText("$at.prepNote", ingredient.prepNote)
            checkText("$at.substitution", ingredient.substitution)

            // A missing quantity means "to taste", which has no unit either.
            val quantity = ingredient.quantity
            val unit = ingredient.unit
            if (quantity != null && quantity <= 0.0) fail("$at.quantity", "must be a positive number")
            if (quantity != null && unit == null) fail("$at.unit", "is required when there is a quantity")
            if (quantity == null && unit != null) fail("$at.unit", "needs a quantity, or leave both out for \"to taste\"")
            if (unit != null && unit !in units) fail("$at.unit", "unknown unit \"$unit\"")

            if (unit in localUnits && ingredient.approxMetric == null && !ingredient.approxMetricUnknown) {
                fail(
                    "$at.approxMetric",
                    "$unit is a local unit: give the cook's metric equivalent, or set approxMetricUnknown",
                )
            }
            if (ingredient.approxMetric != null && ingredient.approxMetricUnknown) {
                fail("$at.approxMetricUnknown", "contradicts the approxMetric that is given")
            }
            ingredient.approxMetric?.let { metric ->
                if (metric.amount <= 0.0) fail("$at.approxMetric.amount", "must be a positive number")
                if (metric.unit !in metricUnits) {
                    fail("$at.approxMetric.unit", "must be a metric unit, got \"${metric.unit}\"")
                }
            }
        }

        if (recipe.steps.isEmpty()) fail("steps", "at least one is required")
        recipe.steps.forEachIndexed { index, step ->
            checkText("steps[$index].text", step.text)
            val seconds = step.timerSeconds
            if (seconds != null && seconds <= 0) fail("steps[$index].timerSeconds", "must be a positive number")
        }

        return errors
    }
}
