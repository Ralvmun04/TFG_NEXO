package ai.codia.x.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call

interface OpenAiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun generateSQL(@Body request: OpenAiRequest): Call<OpenAiResponse>
}
