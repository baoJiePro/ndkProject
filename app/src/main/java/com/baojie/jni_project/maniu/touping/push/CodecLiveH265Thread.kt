package com.baojie.jni_project.maniu.touping.push

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/17 18:41
 */
class CodecLiveH265Thread(val socketServerLive: SocketServerLive,private val mediaProjection: MediaProjection) :
    Thread() {

    private val width = 720
    private val height = 1280

    private var mediaCodec: MediaCodec? = null
    private var virtualDisplay: VirtualDisplay? = null
    val NAL_I = 19
    val NAL_VPS = 32

    fun startLive() {
        //            mediacodec  中间联系人      dsp芯片   帧
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
        //1s多少帧
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        //i帧
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = mediaCodec?.createInputSurface()
        //创建场地
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "-display",
            width,
            height,
            1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            surface,
            null,
            null
        )
        start()
    }

    override fun run() {
        mediaCodec?.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (true){
            val outputBufferId = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000)
            outputBufferId?.let {
                if (it >= 0){
//                编码好的H265的数据
                    val byteBuffer = mediaCodec?.getOutputBuffer(outputBufferId)
                    dealFrame(byteBuffer!!, bufferInfo)
                    mediaCodec?.releaseOutputBuffer(outputBufferId, false)
                }
            }
        }
    }

    private var vps_sps_pps_buf: ByteArray? = null
    private fun dealFrame(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4
        if (bb.get(2) == 0x01.toByte()){
            offset = 3
        }
        val type = (bb[offset] and 0x7E.toByte()).toInt() shr 1
        if (type == NAL_VPS){
            vps_sps_pps_buf = ByteArray(bufferInfo.size)
            bb.get(vps_sps_pps_buf!!)
        }else if (type == NAL_I){
            val bytes = ByteArray(bufferInfo.size)
            bb.get(bytes)
            val newBuf = ByteArray(vps_sps_pps_buf!!.size + bytes.size)
            vps_sps_pps_buf?.let { System.arraycopy(it, 0, newBuf, 0, vps_sps_pps_buf!!.size) }
            System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf!!.size, bytes.size)
            socketServerLive.sendData(newBuf)
        }else{
            val bytes = ByteArray(bufferInfo.size)
            bb.get(bytes)
            socketServerLive.sendData(bytes)
        }
    }
}