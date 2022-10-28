package com.baojie.jni_project.maniu.rtmpbili

import android.media.projection.MediaProjection
import com.blankj.utilcode.util.LogUtils
import java.util.concurrent.LinkedBlockingQueue

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/28 09:15
 */
class ScreenLive: Thread(){

    private val TAG = this.javaClass.simpleName

    private var url: String = ""
    private var mediaProjection: MediaProjection? = null
    private var isLiving = false
    private var queue = LinkedBlockingQueue<RTMPPackage>()

//    companion object {
//        init {
//            System.loadLibrary("jni_project")
//        }
//    }

    fun addPackage(rtmpPackage: RTMPPackage){
        if (!isLiving){
            return
        }
        queue.add(rtmpPackage)
    }

    //开启直播
    fun startLive(urlLive: String, media: MediaProjection){
        url = urlLive
        mediaProjection = media
        start()
    }

    override fun run() {
        if (!connect(url)){
            LogUtils.d(TAG, "run: ----------->推送失败")
            return
        }
        //开启线程
        val videoCodec = VideoCodec(this)
        mediaProjection ?: return
        videoCodec.startLive(mediaProjection!!)
        isLiving = true
        while (isLiving){
            var rtmpPackage: RTMPPackage ?= null
            try {
                rtmpPackage = queue.take()
            }catch (e: InterruptedException){
                e.printStackTrace()
            }
            if (rtmpPackage?.buffer != null && rtmpPackage.buffer!!.isNotEmpty()){
                LogUtils.d(TAG, "run: ----------->推送 ${rtmpPackage.buffer!!.size}")
                sendData(rtmpPackage.buffer!!, rtmpPackage.buffer!!.size, rtmpPackage.tms)
            }

        }

    }

    private external fun connect(url: String): Boolean
    private external fun sendData(data: ByteArray, len: Int, tms: Long): Boolean

}