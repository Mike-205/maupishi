package ke.co.maupishi.app.content

import ke.co.maupishi.app.model.Category
import ke.co.maupishi.app.model.Language
import ke.co.maupishi.app.model.MeasureUnit
import ke.co.maupishi.app.model.Recipe
import ke.co.maupishi.app.model.RecipeSource
import ke.co.maupishi.app.model.Region
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeParserTest {

    private val valid = """
        {
          "id": "test-recipe",
          "name": { "en": "Test recipe", "sw": "Test recipe (sw)" },
          "description": { "en": "A test." },
          "region": "COAST",
          "categories": ["MAIN"],
          "allergens": ["PEANUTS", "DAIRY"],
          "prepMinutes": 10,
          "cookMinutes": 20,
          "servings": 4,
          "credit": "Test credit",
          "ingredients": [
            { "name": { "en": "Flour" }, "quantity": 2, "unit": "CUP" },
            { "name": { "en": "Salt" } },
            { "name": { "en": "Milk" }, "quantity": 1, "unit": "KIKOMBE", "approxMetric": { "amount": 250, "unit": "ML" } }
          ],
          "steps": [
            { "text": { "en": "Mix." }, "timerSeconds": 60 }
          ]
        }
    """.trimIndent()

    /** The valid recipe with one piece of text replaced, to make one specific mistake. */
    private fun valid(replace: String, with: String): String {
        check(valid.contains(replace)) { "Test setup: '$replace' is not in the valid recipe" }
        return valid.replace(replace, with)
    }

    private fun parse(
        json: String = valid,
        fileName: String = "test-recipe.json",
        imageExists: (String) -> Boolean = { true },
    ) = RecipeParser.parse(json, fileName, imageExists)

    private fun recipeOf(json: String = valid): Recipe =
        (parse(json) as? ParseResult.Success)?.recipe ?: throw AssertionError("Expected the file to parse")

    private fun errorsOf(json: String, fileName: String = "test-recipe.json"): List<String> =
        (parse(json, fileName) as? ParseResult.Failure)?.errors
            ?: throw AssertionError("Expected the file to be rejected")

    private fun assertRejectedWith(expected: String, json: String, fileName: String = "test-recipe.json") {
        val errors = errorsOf(json, fileName)
        assertTrue("Expected an error containing '$expected', got $errors", errors.any { expected in it })
    }

    @Test
    fun validFile_parsesIntoTheModel() {
        val recipe = recipeOf()
        assertEquals("test-recipe", recipe.id)
        assertEquals(RecipeSource.BUNDLED, recipe.source)
        assertEquals(false, recipe.verified)
        assertEquals(Region.COAST, recipe.region)
        assertEquals(setOf(Category.MAIN), recipe.categories)
        assertEquals(3, recipe.ingredients.size)
        assertEquals(MeasureUnit.CUP, recipe.ingredients[0].unit)
        assertEquals(250.0, recipe.ingredients[2].approxMetric?.amount ?: 0.0, 0.0)
        assertEquals(60, recipe.steps[0].timerSeconds)
    }

    @Test
    fun missingSwahili_resolvesToEnglish() {
        val recipe = recipeOf()
        assertEquals("Test recipe (sw)", recipe.name.resolve(Language.SWAHILI))
        assertEquals("A test.", recipe.description.resolve(Language.SWAHILI))
    }

    @Test
    fun toTasteIngredient_hasNoQuantityOrUnit() {
        val salt = recipeOf().ingredients[1]
        assertNull(salt.quantity)
        assertNull(salt.unit)
    }

    @Test
    fun invalidJson_isRejected() {
        assertRejectedWith("file:", "this is not json")
    }

    @Test
    fun missingRequiredField_isRejected() {
        assertRejectedWith("servings", valid(replace = "\"servings\": 4,", with = ""))
    }

    @Test
    fun misspelledField_isRejected_notIgnored() {
        assertRejectedWith("servngs", valid(replace = "\"servings\"", with = "\"servngs\""))
    }

    @Test
    fun badId_isRejected() {
        assertRejectedWith("id:", valid(replace = "\"id\": \"test-recipe\"", with = "\"id\": \"Test Recipe\""), "Test Recipe.json")
    }

    @Test
    fun idThatDoesNotMatchTheFileName_isRejected() {
        assertRejectedWith("must match the file name", valid, fileName = "other-name.json")
    }

    @Test
    fun unknownRegion_isRejected() {
        assertRejectedWith("unknown region \"COST\"", valid(replace = "\"COAST\"", with = "\"COST\""))
    }

    @Test
    fun unknownAllergen_isReportedWithItsPosition() {
        assertRejectedWith("allergens[1]", valid(replace = "\"DAIRY\"", with = "\"MILK\""))
    }

    @Test
    fun unknownUnit_isRejected() {
        assertRejectedWith("unknown unit \"CUPS\"", valid(replace = "\"CUP\"", with = "\"CUPS\""))
    }

    @Test
    fun blankEnglishName_isRejected() {
        assertRejectedWith("name:", valid(replace = "\"en\": \"Test recipe\"", with = "\"en\": \"  \""))
    }

    @Test
    fun blankStepText_isRejected() {
        assertRejectedWith("steps[0].text", valid(replace = "\"en\": \"Mix.\"", with = "\"en\": \"\""))
    }

    @Test
    fun blankCredit_isRejected() {
        assertRejectedWith("credit", valid(replace = "\"Test credit\"", with = "\"\""))
    }

    @Test
    fun nonPositiveServings_areRejected() {
        assertRejectedWith("servings", valid(replace = "\"servings\": 4", with = "\"servings\": 0"))
    }

    @Test
    fun localUnitWithoutEquivalent_isRejected() {
        val json = valid(replace = ", \"approxMetric\": { \"amount\": 250, \"unit\": \"ML\" }", with = "")
        assertRejectedWith("local unit", json)
    }

    @Test
    fun localUnitMarkedUnknownOnPurpose_isAccepted() {
        val json = valid(
            replace = "\"approxMetric\": { \"amount\": 250, \"unit\": \"ML\" }",
            with = "\"approxMetricUnknown\": true",
        )
        assertNull(recipeOf(json).ingredients[2].approxMetric)
    }

    @Test
    fun equivalentThatIsAlsoMarkedUnknown_isRejected() {
        val json = valid(
            replace = "\"approxMetric\": {",
            with = "\"approxMetricUnknown\": true, \"approxMetric\": {",
        )
        assertRejectedWith("contradicts", json)
    }

    @Test
    fun equivalentInANonMetricUnit_isRejected() {
        assertRejectedWith("must be a metric unit", valid(replace = "\"unit\": \"ML\"", with = "\"unit\": \"CUP\""))
    }

    @Test
    fun quantityWithoutUnit_isRejected() {
        assertRejectedWith("is required when there is a quantity", valid(replace = ", \"unit\": \"CUP\"", with = ""))
    }

    @Test
    fun unitWithoutQuantity_isRejected() {
        assertRejectedWith("needs a quantity", valid(replace = "\"name\": { \"en\": \"Salt\" }", with = "\"name\": { \"en\": \"Salt\" }, \"unit\": \"PINCH\""))
    }

    @Test
    fun imageThatIsNotBundled_isRejected() {
        val json = valid(replace = "\"credit\": \"Test credit\",", with = "\"credit\": \"Test credit\", \"image\": { \"file\": \"missing.webp\" },")
        val result = parse(json, imageExists = { false })
        assertTrue(result is ParseResult.Failure)
        assertTrue((result as ParseResult.Failure).errors.any { "missing.webp" in it })
    }

    @Test
    fun imageThatIsBundled_isAccepted() {
        val json = valid(replace = "\"credit\": \"Test credit\",", with = "\"credit\": \"Test credit\", \"image\": { \"file\": \"ok.webp\" },")
        assertEquals("ok.webp", (parse(json, imageExists = { it == "ok.webp" }) as ParseResult.Success).recipe.image?.file)
    }

    @Test
    fun everyProblemInAFile_isReportedAtOnce() {
        val json = valid(replace = "\"COAST\"", with = "\"COST\"")
            .replace("\"servings\": 4", "\"servings\": 0")
        assertEquals(2, errorsOf(json).size)
    }
}
