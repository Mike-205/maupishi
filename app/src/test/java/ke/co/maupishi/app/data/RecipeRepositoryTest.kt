package ke.co.maupishi.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.co.maupishi.app.model.Ingredient
import ke.co.maupishi.app.model.LocalizedText
import ke.co.maupishi.app.model.MeasureUnit
import ke.co.maupishi.app.model.Recipe
import ke.co.maupishi.app.model.RecipeSource
import ke.co.maupishi.app.model.Region
import ke.co.maupishi.app.model.Step
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeRepositoryTest {

    private lateinit var db: MaupishiDatabase
    private lateinit var repository: RecipeRepository

    @Before
    fun openDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MaupishiDatabase::class.java).build()
        repository = RoomRecipeRepository(db.recipeDao())
    }

    @After
    fun closeDatabase() {
        db.close()
    }

    private fun recipe(id: String, name: String) = Recipe(
        id = id,
        source = RecipeSource.BUNDLED,
        verified = false,
        name = LocalizedText(name, "$name (sw)"),
        description = LocalizedText("Description"),
        region = Region.NATIONWIDE,
        community = null,
        categories = emptySet(),
        occasions = emptySet(),
        allergens = emptySet(),
        prepMinutes = 1,
        cookMinutes = 1,
        servings = 1,
        cost = null,
        safetyNote = null,
        culturalNote = null,
        credit = "Credit",
        image = null,
        ingredients = listOf(Ingredient(LocalizedText("Item"), quantity = 1.0, unit = MeasureUnit.PIECE)),
        steps = listOf(Step(LocalizedText("Do it"))),
    )

    private suspend fun save(recipe: Recipe) = db.recipeDao().replace(recipe.toStored())

    @Test
    fun observeRecipes_returnsAppModelsOrderedByNameIgnoringCase() = runTest {
        val banana = recipe("b", "banana")
        val apple = recipe("a", "Apple")
        save(banana)
        save(apple)

        assertEquals(listOf(apple, banana), repository.observeRecipes().first())
    }

    @Test
    fun observeRecipes_isEmptyBeforeAnythingIsStored() = runTest {
        assertEquals(emptyList<Recipe>(), repository.observeRecipes().first())
    }

    @Test
    fun observeRecipes_emitsAgainWhenARecipeIsAdded() = runTest {
        val firstNonEmpty = async { repository.observeRecipes().first { it.isNotEmpty() } }

        val added = recipe("a", "Apple")
        save(added)

        assertEquals(listOf(added), firstNonEmpty.await())
    }

    @Test
    fun getAndObserveRecipe_returnTheRecipeOrNull() = runTest {
        val apple = recipe("a", "Apple")
        save(apple)

        assertEquals(apple, repository.getRecipe("a"))
        assertEquals(apple, repository.observeRecipe("a").first())
        assertNull(repository.getRecipe("missing"))
        assertNull(repository.observeRecipe("missing").first())
    }
}
