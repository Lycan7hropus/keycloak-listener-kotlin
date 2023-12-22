package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jboss.logging.Logger
import org.keycloak.events.Event
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventType
import org.keycloak.events.admin.AdminEvent
import org.keycloak.models.KeycloakSession


class CustomEventListenerProvider(
    private val keycloakSession: KeycloakSession?,
    private val eventNotifier: EventNotifier
) : EventListenerProvider, CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger: Logger = Logger.getLogger(CustomEventListenerProvider::class.java)

    override fun close() {
        eventNotifier.close()
    }

    override fun onEvent(event: Event) {
        launch {
            logger.debugv("onEvent(Event): ${event.toFormattedString()}")
            when (event.type) {
                EventType.REGISTER, EventType.LOGIN -> eventNotifier.sendEvent(event)
                else -> logger.debugv("Unhandled event type: ${event.type}")
            }
        }
    }

    override fun onEvent(event: AdminEvent, includeRepresentation: Boolean) {
        launch {
            logger.tracev("Sending ${event.toFormattedString()} to ${eventNotifier.adminEndpoint}")
            eventNotifier.sendAdminEvent(event)
        }
    }

    private fun AdminEvent.toFormattedString(): String = buildString {
        append("operationType=").append(this@toFormattedString.operationType)
        append(", realmId=").append(this@toFormattedString.authDetails.realmId)
        append(", clientId=").append(this@toFormattedString.authDetails.clientId)
        append(", userId=").append(this@toFormattedString.authDetails.userId)
        append(", ipAddress=").append(this@toFormattedString.authDetails.ipAddress)
        append(", resourcePath=").append(this@toFormattedString.resourcePath)
        this@toFormattedString.error?.let { append(", error=").append(it) }
    }

    private fun Event.toFormattedString(): String = buildString {
        append("type=").append(this@toFormattedString.type)
        append(", realmId=").append(this@toFormattedString.realmId)
        append(", clientId=").append(this@toFormattedString.clientId)
        append(", userId=").append(this@toFormattedString.userId)
        append(", ipAddress=").append(this@toFormattedString.ipAddress)
        this@toFormattedString.error?.let { append(", error=").append(it) }
        this@toFormattedString.details?.forEach { (key, value) ->
            append(", ").append(key).append("=").append(value)
        }
    }
}