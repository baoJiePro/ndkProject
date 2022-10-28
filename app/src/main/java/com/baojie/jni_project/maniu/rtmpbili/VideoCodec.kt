package com.baojie.jni_project.maniu.rtmpbili

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Bundle

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/28 17:00
 */
class VideoCodec(private val screenLive: ScreenLive) : Thread() {

    private var mediaCodec: MediaCodec? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var isLiving = false
    private var timeStamp: Long = 0
    //开始时间
    private var startTime: Long = 0

    //    开启编码层   初始化编码层
    fun startLive(media: MediaProjection) {
        mediaProjection = media
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
        format.apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, 400_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        }

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec?.createInputSurface()
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "screen-codec",
            720,
            1280,
            1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            surface,
            null,
            null
        )
        start()

    }

    override fun run() {
        isLiving = true
        mediaCodec ?: return
        mediaCodec!!.start()
        val bufferInfo = MediaCodec.BufferInfo()
        //手动触发I帧
        while (isLiving){
            if (System.currentTimeMillis() - timeStamp >= 2000){
                val params = Bundle()
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                //dsp芯片触发I帧
                mediaCodec!!.setParameters(params)
                timeStamp = System.currentTimeMillis()
            }
            val index = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 100000)
            if (index >= 0){
                if (startTime == 0L){
                    startTime = bufferInfo.presentationTimeUs / 1000
                }
                val buffer = mediaCodec!!.getOutputBuffer(index)
                val mediaFormat = mediaCodec!!.getOutputFormat(index)
                val outData = ByteArray(bufferInfo.size)
                buffer?.get(outData)
                val rtmpPackage = RTMPPackage()
                rtmpPackage.buffer = outData
                rtmpPackage.tms = bufferInfo.presentationTimeUs / 1000 - startTime
                screenLive.addPackage(rtmpPackage)
                mediaCodec!!.releaseOutputBuffer(index, false)
            }
        }
        isLiving = false
        mediaCodec!!.stop()
        mediaCodec!!.release()
        mediaCodec = null
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
        startTime = 0
    }
}