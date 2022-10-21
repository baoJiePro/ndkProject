package com.baojie.jni_project.maniu.touping.player

import com.blankj.utilcode.util.LogUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/16 21:16
 */
class SocketLive(private val url: String, private val port: Int) {

    private var myWebSocketClient: MyWebSocketClient ? = null

    private var socketCallback: SocketCallback ? = null

    fun start(){
        val uri = URI("$url:$port")
        myWebSocketClient = MyWebSocketClient(uri)
        myWebSocketClient?.connect()
    }

    fun setSocketCallBack(callback: SocketCallback){
        socketCallback = callback
        myWebSocketClient?.setCallBackData(socketCallback)
    }

    fun sendData(byteArray: ByteArray){
        if (myWebSocketClient != null && myWebSocketClient!!.isOpen){
            myWebSocketClient?.send(byteArray)
        }
    }

    class MyWebSocketClient(serverUri: URI?) : WebSocketClient(serverUri) {

        private var callBack: SocketCallback ?= null

        fun setCallBackData(socketCallback: SocketCallback?){
            callBack = socketCallback
        }

        override fun onOpen(handshakedata: ServerHandshake?) {
            LogUtils.d("打开socket onOpen")
        }

        override fun onMessage(message: String?) {

        }

        override fun onMessage(bytes: ByteBuffer?) {
            bytes?.let {
                LogUtils.d("消息长度：${it.remaining()}")
                val buf = ByteArray(it.remaining())
                it.get(buf)
                callBack?.callBack(buf)
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            LogUtils.d("socket onClose code=$code, reason=$reason")
        }

        override fun onError(ex: Exception?) {
            LogUtils.d("socket onError")
        }

    }

    interface SocketCallback{
        fun callBack(data: ByteArray)
    }
}