package com.baojie.jni_project.maniu.touping.player

import android.media.MediaCodec
import android.media.MediaFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import com.baojie.jni_project.R
import com.baojie.jni_project.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private var surface: Surface? = null
    private var mediaCodec: MediaCodec? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSurface()
    }

    private fun initSurface() {
        binding.surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surface = holder.surface
                initSocket()
                initDecoder(surface)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun initDecoder(surface: Surface?) {
        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 720, 1280)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 720 * 1280)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        mediaCodec?.configure(format, surface, null, 0)
        mediaCodec?.start()
    }

    private fun initSocket() {
        val screenLive = SocketLive("ws://192.168.31.141", 13001)
        screenLive.setSocketCallBack(object : SocketLive.SocketCallback {
            override fun callBack(data: ByteArray) {
                val index = mediaCodec?.dequeueInputBuffer(100000) ?: -1
                if (index >= 0){
                    val inputBuffer = mediaCodec?.getInputBuffer(index)
                    inputBuffer?.clear()
                    inputBuffer?.put(data, 0, data.size)
                    //       通知dsp芯片帮忙解码
                    mediaCodec?.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
                }
                //获取数据
                val bufferInfo = MediaCodec.BufferInfo()
                var outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 100000) ?: -1
                while (outputBufferIndex > 0){
                    mediaCodec?.releaseOutputBuffer(outputBufferIndex, true)
                    outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
                }
            }

        })
        screenLive.start()

    }
}