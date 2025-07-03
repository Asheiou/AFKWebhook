package cymru.asheiou.afkwebhook

import com.google.gson.JsonObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/*
Abridged implementation of https://github.com/AeoliaXYZ/AshUtils class of same name
 */
class WebhookSender {
  companion object {
    fun postWebhook(uri: URI, content: String): HttpResponse<String?> {
      val builder = HttpRequest.newBuilder(uri)
      val json = JsonObject()
      json.addProperty("content", content)

      builder.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
      builder.headers("content-type", "application/json")
      val request = builder.build()
      return HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers
        .ofString()).join()
    }

    fun validateResponse(response: HttpResponse<String?>): Boolean {
      val validationCheck = response.statusCode() - 200
      if (validationCheck >= 0 && validationCheck < 100) {
        return true
      } else {
        return false
      }
    }
  }
}