package ke.co.maupishi.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The constant names are stored codes, so these tests fail loudly if one is
 * renamed or removed. Adding a constant means updating the expected list here
 * and the allowed values in `docs/DATA_MODEL.md`.
 */
class RecipeAttributesTest {

    @Test
    fun regionCodes_areStable() {
        assertEquals(
            listOf(
                "COAST", "CENTRAL", "EASTERN", "WESTERN", "NYANZA",
                "RIFT_VALLEY", "NORTH_EASTERN", "NAIROBI", "NATIONWIDE",
            ),
            Region.entries.map { it.name },
        )
    }

    @Test
    fun categoryCodes_areStable() {
        assertEquals(
            listOf("BREAKFAST", "MAIN", "SIDE", "SNACK", "DRINK", "DESSERT", "SOUP_STEW"),
            Category.entries.map { it.name },
        )
    }

    @Test
    fun occasionCodes_areStable() {
        assertEquals(
            listOf(
                "CHRISTMAS", "EID", "WEDDING", "FUNERAL", "EVERYDAY",
                "BUDGET_MEAL", "STUDENT_FRIENDLY", "SCHOOL_HOLIDAY", "SPECIAL_GUEST",
            ),
            Occasion.entries.map { it.name },
        )
    }

    @Test
    fun allergenCodes_areStable() {
        assertEquals(
            listOf(
                "PEANUTS", "TREE_NUTS", "DAIRY", "EGGS", "GLUTEN",
                "FISH", "SHELLFISH", "SOY", "SESAME",
            ),
            Allergen.entries.map { it.name },
        )
    }

    @Test
    fun sourceCodes_areStable() {
        assertEquals(listOf("BUNDLED", "USER", "COMMUNITY"), RecipeSource.entries.map { it.name })
    }

    @Test
    fun unitCodes_areStable() {
        assertEquals(
            listOf(
                "G", "KG", "ML", "L",
                "TSP", "TBSP", "CUP",
                "KIKOMBE", "HANDFUL", "PINCH", "TIN", "DEBE",
                "PIECE", "BUNCH", "CLOVE",
            ),
            MeasureUnit.entries.map { it.name },
        )
    }

    @Test
    fun localUnits_areExactlyTheInformalOnes() {
        assertEquals(
            setOf(
                MeasureUnit.KIKOMBE, MeasureUnit.HANDFUL, MeasureUnit.PINCH,
                MeasureUnit.TIN, MeasureUnit.DEBE,
            ),
            MeasureUnit.entries.filter { it.isLocal }.toSet(),
        )
    }

    @Test
    fun languageTags_matchResourceFolders() {
        assertEquals(listOf("en", "sw"), Language.entries.map { it.tag })
    }
}
