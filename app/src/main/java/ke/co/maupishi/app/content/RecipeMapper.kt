package ke.co.maupishi.app.content

import ke.co.maupishi.app.model.Allergen
import ke.co.maupishi.app.model.ApproxMetric
import ke.co.maupishi.app.model.Category
import ke.co.maupishi.app.model.CostEstimate
import ke.co.maupishi.app.model.Ingredient
import ke.co.maupishi.app.model.LocalizedText
import ke.co.maupishi.app.model.MeasureUnit
import ke.co.maupishi.app.model.Occasion
import ke.co.maupishi.app.model.Recipe
import ke.co.maupishi.app.model.RecipeImage
import ke.co.maupishi.app.model.RecipeSource
import ke.co.maupishi.app.model.Region
import ke.co.maupishi.app.model.Step

/*
 * Turns the file shape into the app shape. It trusts its input: call it only
 * after RecipeValidator has accepted the file, because valueOf throws on a
 * code that is not an allowed value.
 */

internal fun LocalizedTextDto.toModel() = LocalizedText(en = en, sw = sw)

private fun IngredientDto.toModel() = Ingredient(
    name = name.toModel(),
    quantity = quantity,
    unit = unit?.let(MeasureUnit::valueOf),
    group = group?.toModel(),
    approxMetric = approxMetric?.let { ApproxMetric(it.amount, MeasureUnit.valueOf(it.unit)) },
    prepNote = prepNote?.toModel(),
    substitution = substitution?.toModel(),
)

private fun StepDto.toModel() = Step(text = text.toModel(), timerSeconds = timerSeconds)

/** Recipes loaded from files are always [RecipeSource.BUNDLED]. */
internal fun RecipeDto.toModel() = Recipe(
    id = id,
    source = RecipeSource.BUNDLED,
    verified = verified,
    name = name.toModel(),
    description = description.toModel(),
    region = Region.valueOf(region),
    community = community,
    categories = categories.map(Category::valueOf).toSet(),
    occasions = occasions.map(Occasion::valueOf).toSet(),
    allergens = allergens.map(Allergen::valueOf).toSet(),
    prepMinutes = prepMinutes,
    cookMinutes = cookMinutes,
    servings = servings,
    cost = cost?.let { CostEstimate(it.minKes, it.maxKes, it.basis?.toModel()) },
    safetyNote = safetyNote?.toModel(),
    culturalNote = culturalNote?.toModel(),
    credit = credit,
    image = image?.let { RecipeImage(it.file, it.alt?.toModel()) },
    ingredients = ingredients.map { it.toModel() },
    steps = steps.map { it.toModel() },
)
