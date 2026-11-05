package ke.co.maupishi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes recipes with their child rows. Queries that return a
 * [RecipeWithDetails] run in a transaction so a recipe is never read half
 * updated. Reads return a [Flow] that emits again whenever the data changes.
 */
@Dao
abstract class RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY nameEn COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    abstract fun observeById(id: String): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    abstract suspend fun getById(id: String): RecipeWithDetails?

    /** Deleting a recipe removes its child rows too (cascade). */
    @Query("DELETE FROM recipes WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    /**
     * Inserts the recipe or updates it in place, then replaces all of its
     * child rows, all in one transaction. The recipe row is updated rather
     * than deleted and re-inserted, so nothing cascades by accident.
     */
    @Transaction
    open suspend fun replace(details: RecipeWithDetails) {
        val id = details.recipe.id
        upsertRecipe(details.recipe)
        deleteCategories(id)
        deleteOccasions(id)
        deleteAllergens(id)
        deleteIngredients(id)
        deleteSteps(id)
        insertCategories(details.categories)
        insertOccasions(details.occasions)
        insertAllergens(details.allergens)
        insertIngredients(details.ingredients)
        insertSteps(details.steps)
    }

    // Building blocks of [replace]. Room needs them public on the abstract class.

    @Upsert
    abstract suspend fun upsertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipe_categories WHERE recipeId = :recipeId")
    abstract suspend fun deleteCategories(recipeId: String)

    @Query("DELETE FROM recipe_occasions WHERE recipeId = :recipeId")
    abstract suspend fun deleteOccasions(recipeId: String)

    @Query("DELETE FROM recipe_allergens WHERE recipeId = :recipeId")
    abstract suspend fun deleteAllergens(recipeId: String)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    abstract suspend fun deleteIngredients(recipeId: String)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    abstract suspend fun deleteSteps(recipeId: String)

    @Insert
    abstract suspend fun insertCategories(items: List<RecipeCategoryEntity>)

    @Insert
    abstract suspend fun insertOccasions(items: List<RecipeOccasionEntity>)

    @Insert
    abstract suspend fun insertAllergens(items: List<RecipeAllergenEntity>)

    @Insert
    abstract suspend fun insertIngredients(items: List<IngredientEntity>)

    @Insert
    abstract suspend fun insertSteps(items: List<StepEntity>)
}
