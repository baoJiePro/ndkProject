package com.baojie.jni_project.maniu.videochat

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.baojie.jni_project.utils.H265Utils

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/21 10:51
 */
class DecodecPlayerLiveH265 {

    private var mediaCodec: MediaCodec? = null

    fun initDecoder(surface: Surface){
        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        H265Utils.initMediaCode(mediaCodec, surface, MediaFormat.MIMETYPE_VIDEO_HEVC, 1080, 1920)
    }

    fun callBack(data: ByteArray){
        val index = mediaCodec?.dequeueInputBuffer(100000) ?: -1
        if (index >= 0){
            val inputBuffer = mediaCodec?.getInputBuffer(index)
            inputBuffer?.clear()
            inputBuffer?.put(data, 0, data.size)
            //dsp芯片解码    解码 的 传进去的   只需要保证编码顺序就好了  1000
            mediaCodec?.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
            val bufferInfo = MediaCodec.BufferInfo()
            var outBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 100000) ?: -1
            while (outBufferIndex >= 0){
                mediaCodec?.releaseOutputBuffer(outBufferIndex, true)
                outBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 0) ?: -1
            }
        }
    }
}