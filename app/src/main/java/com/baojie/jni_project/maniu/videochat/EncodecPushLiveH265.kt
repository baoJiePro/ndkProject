package com.baojie.jni_project.maniu.videochat

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.baojie.jni_project.maniu.touping.player.SocketLive
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/21 09:58
 */
class EncodecPushLiveH265(socketCallback: SocketLive.SocketCallback, val width: Int, val height: Int) {

    private val socketLive = SocketLive("ws://192.168.18.52", 40002)

    private var mediaCodec: MediaCodec? = null
    //    nv21转换成nv12的数据
    private var nv12: ByteArray ?= null
    //    旋转之后的yuv数据
    private var yuv: ByteArray? = null
    
    private var frameIndex = 0

    val NAL_I = 19
    val NAL_VPS = 32
    private var vps_sps_pps_buf: ByteArray? = null

    init {
        socketLive.setSocketCallBack(socketCallback)
        //建立连接
        socketLive.start()
    }

    fun startLive(){
        //实例化编码器
        //创建对应编码器
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, height, width)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, height * width)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec?.start()
        val bufferLength = width * height * 3/2
        nv12 = ByteArray(bufferLength)
        yuv = ByteArray(bufferLength)

    }

    //摄像头调用
    fun encodeFrame(input: ByteArray){
        //        旋转
        //        nv21-nv12
        nv12 = YuvUtils.nv21toNV12(input)
        //        旋转
        YuvUtils.portraitData2Raw(nv12, yuv, width, height)
        val inputBufferIndex = mediaCodec?.dequeueInputBuffer(100000) ?: -1
        if (inputBufferIndex >= 0){
            val inputBuffer = mediaCodec?.getInputBuffer(inputBufferIndex)
            inputBuffer?.clear()
            yuv?.let { inputBuffer?.put(it) }
            val presentationTimeUs = computerPresentationTime(frameIndex)
            mediaCodec?.queueInputBuffer(inputBufferIndex, 0, yuv!!.size, presentationTimeUs, 0)
            frameIndex++
        }
        val bufferInfo = MediaCodec.BufferInfo()
        var outBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 100000) ?: -1
        while (outBufferIndex >= 0){
            //存放的H265编码的数据
            val outBuffer = mediaCodec?.getOutputBuffer(outBufferIndex)
            //发送数据
            dealFrame(outBuffer!!, bufferInfo)
            mediaCodec?.releaseOutputBuffer(outBufferIndex, false)
            outBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
        }
    }

    private fun dealFrame(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4
        if (bb.get(2) == 0x01.toByte()){
            offset = 3
        }
        when ((bb[offset] and 0x7E.toByte()).toInt() shr 1) {
            NAL_VPS -> {
                vps_sps_pps_buf = ByteArray(bufferInfo.size)
                bb.get(vps_sps_pps_buf!!)
            }
            NAL_I -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                val newBuf = ByteArray(vps_sps_pps_buf!!.size + bytes.size)
                vps_sps_pps_buf?.let { System.arraycopy(it, 0, newBuf, 0, vps_sps_pps_buf!!.size) }
                System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf!!.size, bytes.size)
                socketLive.sendData(newBuf)
            }
            else -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                socketLive.sendData(bytes)
            }
        }
    }

    private fun computerPresentationTime(frameIndex: Int): Long {
        return (140 + frameIndex * 1000000 / 15).toLong()
    }
}