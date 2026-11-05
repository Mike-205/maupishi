package ke.co.maupishi.app.data

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A recipe row together with all of its child rows, as read from or written to
 * the database in one go. Room fills the lists in no particular order, so sort
 * ingredients and steps by `position` before using them.
 */
data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val categories: List<RecipeCategoryEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val occasions: List<RecipeOccasionEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val allergens: List<RecipeAllergenEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val ingredients: List<IngredientEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val steps: List<StepEntity>,
)
