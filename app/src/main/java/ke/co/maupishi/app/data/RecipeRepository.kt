package ke.co.maupishi.app.data

import ke.co.maupishi.app.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The only way the rest of the app reads recipes. It returns the app's own
 * [Recipe] with both languages kept; the UI picks one with `resolve`. Callers
 * never see database classes, so the storage can change behind this interface.
 */
interface RecipeRepository {
    /** All recipes by English name, emitting again whenever the stored recipes change. */
    fun observeRecipes(): Flow<List<Recipe>>

    /** One recipe, or null when there is none with this id. Emits again on change. */
    fun observeRecipe(id: String): Flow<Recipe?>

    suspend fun getRecipe(id: String): Recipe?
}

/** Reads recipes from the Room database. */
class RoomRecipeRepository(private val dao: RecipeDao) : RecipeRepository {

    override fun observeRecipes(): Flow<List<Recipe>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeRecipe(id: String): Flow<Recipe?> =
        dao.observeById(id).map { it?.toModel() }

    override suspend fun getRecipe(id: String): Recipe? = dao.getById(id)?.toModel()
}
