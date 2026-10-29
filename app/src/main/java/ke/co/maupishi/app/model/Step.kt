package ke.co.maupishi.app.model

/** One instruction. [timerSeconds] is set when the step has a wait the app can time. */
data class Step(
    val text: LocalizedText,
    val timerSeconds: Int? = null,
)
