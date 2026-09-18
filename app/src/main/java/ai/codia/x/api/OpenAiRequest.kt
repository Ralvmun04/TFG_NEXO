package ai.codia.x.api

data class OpenAiMessage(
    val role: String,
    val content: String
)

data class OpenAiRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<OpenAiMessage>,
    val temperature: Double = 0.2
)
