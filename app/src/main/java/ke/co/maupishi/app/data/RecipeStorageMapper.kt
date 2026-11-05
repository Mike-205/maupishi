package ke.co.maupishi.app.data

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
 * Converts between the app shape (`model`) and the stored shape (`data`).
 * Only this app writes the database, so every stored code is expected to be a
 * valid enum constant name; `valueOf` throws if one is not.
 */

private fun localizedOrNull(en: String?, sw: String?): LocalizedText? =
    en?.let { LocalizedText(it, sw) }

internal fun Recipe.toStored(): RecipeWithDetails {
    val recipe = RecipeEntity(
        id = id,
        source = source.name,
        verified = verified,
        nameEn = name.en,
        nameSw = name.sw,
        descriptionEn = description.en,
        descriptionSw = description.sw,
        region = region.name,
        community = community,
        prepMinutes = prepMinutes,
        cookMinutes = cookMinutes,
        servings = servings,
        costMinKes = cost?.minKes,
        costMaxKes = cost?.maxKes,
        costBasisEn = cost?.basis?.en,
        costBasisSw = cost?.basis?.sw,
        safetyNoteEn = safetyNote?.en,
        safetyNoteSw = safetyNote?.sw,
        culturalNoteEn = culturalNote?.en,
        culturalNoteSw = culturalNote?.sw,
        credit = credit,
        imageFile = image?.file,
        imageAltEn = image?.alt?.en,
        imageAltSw = image?.alt?.sw,
    )
    return RecipeWithDetails(
        recipe = recipe,
        categories = categories.map { RecipeCategoryEntity(id, it.name) },
        occasions = occasions.map { RecipeOccasionEntity(id, it.name) },
        allergens = allergens.map { RecipeAllergenEntity(id, it.name) },
        ingredients = ingredients.mapIndexed { position, ingredient ->
            IngredientEntity(
                recipeId = id,
                position = position,
                groupEn = ingredient.group?.en,
                groupSw = ingredient.group?.sw,
                nameEn = ingredient.name.en,
                nameSw = ingredient.name.sw,
                quantity = ingredient.quantity,
                unit = ingredient.unit?.name,
                approxMetricAmount = ingredient.approxMetric?.amount,
                approxMetricUnit = ingredient.approxMetric?.unit?.name,
                prepNoteEn = ingredient.prepNote?.en,
                prepNoteSw = ingredient.prepNote?.sw,
                substitutionEn = ingredient.substitution?.en,
                substitutionSw = ingredient.substitution?.sw,
            )
        },
        steps = steps.mapIndexed { position, step ->
            StepEntity(
                recipeId = id,
                position = position,
                textEn = step.text.en,
                textSw = step.text.sw,
                timerSeconds = step.timerSeconds,
            )
        },
    )
}

internal fun RecipeWithDetails.toModel(): Recipe = Recipe(
    id = recipe.id,
    source = RecipeSource.valueOf(recipe.source),
    verified = recipe.verified,
    name = LocalizedText(recipe.nameEn, recipe.nameSw),
    description = LocalizedText(recipe.descriptionEn, recipe.descriptionSw),
    region = Region.valueOf(recipe.region),
    community = recipe.community,
    categories = categories.map { Category.valueOf(it.category) }.toSet(),
    occasions = occasions.map { Occasion.valueOf(it.occasion) }.toSet(),
    allergens = allergens.map { Allergen.valueOf(it.allergen) }.toSet(),
    prepMinutes = recipe.prepMinutes,
    cookMinutes = recipe.cookMinutes,
    servings = recipe.servings,
    cost = if (recipe.costMinKes != null && recipe.costMaxKes != null) {
        CostEstimate(
            minKes = recipe.costMinKes,
            maxKes = recipe.costMaxKes,
            basis = localizedOrNull(recipe.costBasisEn, recipe.costBasisSw),
        )
    } else {
        null
    },
    safetyNote = localizedOrNull(recipe.safetyNoteEn, recipe.safetyNoteSw),
    culturalNote = localizedOrNull(recipe.culturalNoteEn, recipe.culturalNoteSw),
    credit = recipe.credit,
    image = recipe.imageFile?.let {
        RecipeImage(file = it, alt = localizedOrNull(recipe.imageAltEn, recipe.imageAltSw))
    },
    ingredients = ingredients.sortedBy { it.position }.map { it.toModel() },
    steps = steps.sortedBy { it.position }.map { Step(LocalizedText(it.textEn, it.textSw), it.timerSeconds) },
)

private fun IngredientEntity.toModel() = Ingredient(
    name = LocalizedText(nameEn, nameSw),
    quantity = quantity,
    unit = unit?.let(MeasureUnit::valueOf),
    group = localizedOrNull(groupEn, groupSw),
    approxMetric = if (approxMetricAmount != null && approxMetricUnit != null) {
        ApproxMetric(approxMetricAmount, MeasureUnit.valueOf(approxMetricUnit))
    } else {
        null
    },
    prepNote = localizedOrNull(prepNoteEn, prepNoteSw),
    substitution = localizedOrNull(substitutionEn, substitutionSw),
)
