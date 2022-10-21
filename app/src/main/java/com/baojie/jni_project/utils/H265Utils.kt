package com.baojie.jni_project.utils

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/21 11:09
 */
object H265Utils {

    fun initMediaCode(
        mediaCodec: MediaCodec?,
        surface: Surface? = null,
        formatType: String,
        width: Int,
        height: Int
    ) {
        val mediaFormat = MediaFormat.createVideoFormat(formatType, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        mediaCodec?.configure(mediaFormat, surface, null, 0)
    }
}