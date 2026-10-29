package ke.co.maupishi.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizedTextTest {

    @Test
    fun swahili_isUsed_whenPresent() {
        val text = LocalizedText(en = "Rice", sw = "Wali")
        assertEquals("Wali", text.resolve(Language.SWAHILI))
    }

    @Test
    fun english_isUsed_whenEnglishIsAsked_evenIfSwahiliExists() {
        val text = LocalizedText(en = "Rice", sw = "Wali")
        assertEquals("Rice", text.resolve(Language.ENGLISH))
    }

    @Test
    fun missingSwahili_fallsBackToEnglish() {
        val text = LocalizedText(en = "Rice")
        assertEquals("Rice", text.resolve(Language.SWAHILI))
    }

    @Test
    fun emptySwahili_fallsBackToEnglish() {
        val text = LocalizedText(en = "Rice", sw = "")
        assertEquals("Rice", text.resolve(Language.SWAHILI))
    }

    @Test
    fun whitespaceOnlySwahili_fallsBackToEnglish() {
        val text = LocalizedText(en = "Rice", sw = "  \n\t ")
        assertEquals("Rice", text.resolve(Language.SWAHILI))
    }

    @Test
    fun swahiliWithSurroundingSpaces_isReturnedUnchanged() {
        val text = LocalizedText(en = "Rice", sw = " Wali ")
        assertEquals(" Wali ", text.resolve(Language.SWAHILI))
    }
}
