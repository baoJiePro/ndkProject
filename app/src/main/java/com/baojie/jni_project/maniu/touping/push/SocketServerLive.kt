package com.baojie.jni_project.maniu.touping.push

import android.media.projection.MediaProjection
import com.blankj.utilcode.util.LogUtils
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/16 22:17
 */
class SocketServerLive(private val port: Int) {

    private var webSocket: WebSocket ?= null
    private var codecLiveH265Thread: CodecLiveH265Thread? = null

    private val webSocketServer =object : WebSocketServer(InetSocketAddress(port)){
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            LogUtils.d("打开socketServer onOpen")
            webSocket = conn
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            LogUtils.d("socketServer onClose code=$code, reason=$reason")
        }

        override fun onMessage(conn: WebSocket?, message: String?) {

        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            LogUtils.d("socketServer onError")
        }

        override fun onStart() {

        }

    }


    fun start(mediaProjection: MediaProjection){
        webSocketServer.start()
        codecLiveH265Thread = CodecLiveH265Thread(this, mediaProjection)
        codecLiveH265Thread?.startLive()
    }

    fun sendData(byteArray: ByteArray){
        if (webSocket != null && webSocket!!.isOpen){
            webSocket?.send(byteArray)
        }
    }

    fun close(){
        webSocket?.close()
        webSocketServer.stop()
    }


}