package org.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.example.Constants.API_CONNECTION_REQUEST_TIMEOUT_DEFAULT
import org.example.Constants.API_CONNECT_TIMEOUT_DEFAULT
import org.example.Constants.API_MAX_CONNECTIONS_DEFAULT
import org.example.Constants.API_SOCKET_TIMEOUT_DEFAULT
import org.jboss.logging.Logger
import org.keycloak.Config
import org.keycloak.events.EventListenerProvider
import org.keycloak.events.EventListenerProviderFactory
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory

class CustomEventListenerProviderFactory : EventListenerProviderFactory {

    private val logger: Logger = Logger.getLogger(CustomEventListenerProviderFactory::class.java)
    private lateinit var eventNotifier: EventNotifier
    private lateinit var config: Config.Scope

    private val id: String = "custom-event-listener"

    override fun create(keycloakSession: KeycloakSession?): EventListenerProvider {
        return CustomEventListenerProvider(keycloakSession, eventNotifier)
    }

    override fun init(config: Config.Scope) {
        this.config = config

        val endpoint: String = config.get("apiEndpoint").also { endpoint ->
            logger.info("Your endpoint is $endpoint")
        } ?: throw IllegalArgumentException("Endpoint is null, please check your configuration")
        val adminEndpoint: String = config.get("apiAdminEndpoint").also { adminEndpoint ->
            logger.info("Your admin endpoint is $adminEndpoint")
        } ?: endpoint


        val maxConnections: Int = config.getInt("apiMaxConnections", API_MAX_CONNECTIONS_DEFAULT)
        val connectionRequestTimeout: Long =
            config.getLong("apiConnectionRequestTimeout", API_CONNECTION_REQUEST_TIMEOUT_DEFAULT)
        val connectTimeout: Long = config.getLong("apiConnectTimeout", API_CONNECT_TIMEOUT_DEFAULT)
        val socketTimeout: Long = config.getLong("apiSocketTimeout", API_SOCKET_TIMEOUT_DEFAULT)

        val client = HttpClient(CIO) {
            engine {
                maxConnectionsCount = maxConnections
                requestTimeout = connectionRequestTimeout
                endpoint {
                    this.connectTimeout = connectTimeout
                    this.socketTimeout = socketTimeout
                }
            }
        }

        eventNotifier = EventNotifier(endpoint = endpoint, adminEndpoint = adminEndpoint, client = client)
    }

    override fun postInit(p0: KeycloakSessionFactory?) {

    }

    override fun close() {
    }

    override fun getId(): String {
        return config.get("spiId") ?: id
    }

}