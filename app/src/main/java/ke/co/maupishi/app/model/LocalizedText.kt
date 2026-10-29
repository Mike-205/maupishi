package ke.co.maupishi.app.model

/** Languages the app can show recipe text in. */
enum class Language(val tag: String) {
    ENGLISH("en"),
    SWAHILI("sw"),
}

/**
 * A piece of recipe text in English, with an optional Swahili version.
 * English is always present, so [resolve] always has something to show.
 */
data class LocalizedText(
    val en: String,
    val sw: String? = null,
) {
    /**
     * The text for [language]. Swahili is used only when it exists and is not
     * blank; otherwise this falls back to English.
     */
    fun resolve(language: Language): String = when (language) {
        Language.ENGLISH -> en
        Language.SWAHILI -> sw?.takeIf { it.isNotBlank() } ?: en
    }
}
