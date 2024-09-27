package berlin.ong.student

import com.ditchoom.websocket.WebSocketClient
import com.ditchoom.websocket.WebSocketConnectionOptions
import com.ditchoom.websocket.WebSocketMessage
import com.ditchoom.websocket.allocate
import com.jme3.math.Vector3f
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Receive a `Vector3f` every time the client sends a rotation update.
 * Run `InvadeControllerReceiver::listen` with a `InvadeControllerReceiver.Listener` as the argument.
 *
 * @see InvadeControllerReceiver.Listener
 * @see listen
 */
class InvadeControllerReceiver(host: String, port: Int, path: String) {
    private val client = WebSocketClient.Companion.allocate(
        WebSocketConnectionOptions(
            name = host,
            port = port,
            websocketEndpoint = path,
        )
    )

    /**
     * Start listening for rotation updates.
     * This is *not* a blocking operation and runs asynchronously.
     *
     * @param listener the listener the updates are sent to
     * @see Listener
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun listen(listener: Listener) {
        GlobalScope.launch {
            client.connect()

            client.onIncomingWebsocketMessage onMessage@{ message ->
                if (message !is WebSocketMessage.Text) return@onMessage

                println("connect")

                try {
                    val floats = message.value
                        .split(";")
                        .map { v -> v.toFloat() }

                    assert(floats.size == 3)

                    val vec = Vector3f(floats[0], floats[1], floats[2])
                    listener.onRotationChange(vec)
                } catch (_: Exception) {
                    return@onMessage
                }
            }
        }
    }

    /**
     * Disconnect the client, updates will no longer be sent.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun disconnect() {
        GlobalScope.launch { client.close() }
    }

    interface Listener {
        /**
         * This is called every time the client sends an update
         */
        fun onRotationChange(rotation: Vector3f)

        /**
         * This is called should there be an error of any kind.
         * This should probably call `InvadeControllerReceiver::disconnect` if it ends the program.
         * @see disconnect
         */
        fun onError()
    }
}
