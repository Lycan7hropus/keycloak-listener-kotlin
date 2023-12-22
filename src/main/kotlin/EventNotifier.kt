package org.example

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jboss.logging.Logger
import org.keycloak.events.Event
import org.keycloak.events.admin.AdminEvent

class EventNotifier(
    val endpoint: String,
    val adminEndpoint: String,
    private val client: HttpClient,
    private val gson: Gson = Gson()
) {
    private val logger: Logger = Logger.getLogger(EventNotifier ::class.java)

    suspend fun sendEvent(event: Event): HttpResponse {
        return sendEventGeneric(endpoint, event).apply {
            logger.infov("${event.type} event sent to $endpoint")
        }
    }

    suspend fun sendAdminEvent(event: AdminEvent): HttpResponse {
        return sendEventGeneric(adminEndpoint, event).apply {
            logger.infov("Admin event sent to $adminEndpoint")
        }
    }

    private suspend fun sendEventGeneric(url: String, event: Any): HttpResponse {
        val eventData = gson.toJson(event)

        return withContext(Dispatchers.IO) {
            try {
                client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(eventData)
                }
            } catch (e: Exception) {
                logger.error("Error while sending event: ${e.message}")
                throw e
            }
        }
    }

    fun close() {
        client.close()
    }
}