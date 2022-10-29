package com.baojie.jni_project.maniu.rtmpbili

import android.annotation.SuppressLint
import android.media.*
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import java.lang.Exception

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/28 23:34
 */
class AudioCodec(val screenLive: ScreenLive) : Thread() {

    private val TAG = this.javaClass.simpleName
    private var mediaCodec: MediaCodec? = null
    private var minBufferSize: Int = 0
    private var audioRecord: AudioRecord? = null
    private var isRecoding: Boolean = false
    private var startTime: Long = 0


    @SuppressLint("MissingPermission")
    fun startLive() {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
        format.apply {
            //录音质量
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            //1s码率 aac
            setInteger(MediaFormat.KEY_BIT_RATE, 64_000)
        }
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            //录音工具类  采样位数 通道数   采样评率   固定了   设备没关系  录音 数据一样的
            minBufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
            )
        }catch (e: Exception){
            e.printStackTrace()
        }
        ThreadUtils.getCpuPool().execute(this)
    }

    override fun run() {
        isRecoding = true
        mediaCodec ?: return
        mediaCodec!!.start()
        val bufferInfo = MediaCodec.BufferInfo()
        var rtmpPackage = RTMPPackage()
        // 发音频  另外一段准备
        val audioDecoderSpecificInfo = byteArrayOf(0x12, 0x08)
        rtmpPackage.buffer = audioDecoderSpecificInfo
        rtmpPackage.type = RTMP_PACKET_TYPE_AUDIO_HEAD
        screenLive.addPackage(rtmpPackage)
        LogUtils.dTag(TAG, "开始录音 minBufferSize: $minBufferSize")
        audioRecord ?: return
        audioRecord!!.startRecording()
        val buffer = ByteArray(minBufferSize)
        while (isRecoding){
            // 麦克风的数据读取出来   pcm   buffer  aac
            val len = audioRecord!!.read(buffer, 0, buffer.size)
            if (len <= 0){
                continue
            }
            //立即得到有效输入缓冲区
            var index = mediaCodec!!.dequeueInputBuffer(0)
            if (index >= 0){
                val inputBuffer = mediaCodec!!.getInputBuffer(index)
                inputBuffer?.clear()
                inputBuffer?.put(buffer, 0, len)
                //填充数据后再加入队列
                mediaCodec!!.queueInputBuffer(index, 0, len, System.nanoTime() / 1000, 0)
            }
            index = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
            while (index >= 0 && isRecoding){
                val outBuffer = mediaCodec!!.getOutputBuffer(index)
                val outData = ByteArray(bufferInfo.size)
                //编码好的数据aac
                outBuffer?.get(outData)
                if (startTime == 0L){
                    startTime = bufferInfo.presentationTimeUs / 1000
                }
                rtmpPackage = RTMPPackage()
                rtmpPackage.buffer = outData
                rtmpPackage.type = RTMP_PACKET_TYPE_AUDIO_DATA
                rtmpPackage.tms = bufferInfo.presentationTimeUs / 1000 - startTime
                screenLive.addPackage(rtmpPackage)
                mediaCodec!!.releaseOutputBuffer(index, false)
                index = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
        startTime = 0
        isRecoding = false
    }
}