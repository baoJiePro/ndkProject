package com.baojie.jni_project.maniu.play

import android.content.Context
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.baojie.jni_project.utils.LifeScopeUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/5 15:11
 */
class H264Player(context: Context, val path: String, surface: Surface) {

    private var mediaCodec: MediaCodec = MediaCodec.createDecoderByType("video/avc")

    init {
        val mediaFormat = MediaFormat.createVideoFormat("video/avc", 368, 384)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaCodec.configure(mediaFormat, surface, null, 0)
    }

    fun play(){
        mediaCodec.start()
        LifeScopeUtils.getLifeScope().launch(Dispatchers.IO) {
            decodeH264()
        }
    }

    private fun decodeH264() {
        val bytes = FileIOUtils.readFile2BytesByStream(path
        ) {
            LogUtils.d("readFile2BytesByStream: $it")
        }

        val inputBuffers = mediaCodec.inputBuffers
        var startIndex = 0
        val totalSize = bytes.size
        while (true){
            if (totalSize == 0 || startIndex >= totalSize){
                break
            }
            //寻找索引
            val nextFrameStart = findByFrame(bytes, startIndex+2, totalSize)
            val info = MediaCodec.BufferInfo()
            //查询哪一个bytebuffer能够用
            val inIndex = mediaCodec.dequeueInputBuffer(10000)
            if (inIndex >= 0){
                val byteBuffer = inputBuffers[inIndex]
                byteBuffer.clear()
                byteBuffer.put(bytes, startIndex, nextFrameStart - startIndex)
                mediaCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0)
                startIndex = nextFrameStart
            }else{
                continue
            }
            val outIndex = mediaCodec.dequeueOutputBuffer(info, 10000)
            if (outIndex >= 0){
                Thread.sleep(33)
                mediaCodec.releaseOutputBuffer(outIndex, true)
            }

        }

    }

    private fun findByFrame(bytes: ByteArray, start: Int, totalSize: Int): Int {
        (start..totalSize-4).forEach { i ->
            if (bytes[i].toInt() == 0x00 && bytes[i + 1].toInt() == 0x00 && bytes[i + 2].toInt() == 0x00 && bytes[i + 3].toInt() == 0x01){
                return i
            }
        }
        return -1
    }
}