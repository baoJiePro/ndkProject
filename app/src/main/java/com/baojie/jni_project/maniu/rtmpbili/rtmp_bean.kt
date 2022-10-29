package com.baojie.jni_project.maniu.rtmpbili

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/28 21:13
 */

const val RTMP_PACKET_TYPE_VIDEO = 0
const val RTMP_PACKET_TYPE_AUDIO_HEAD = 1
const val RTMP_PACKET_TYPE_AUDIO_DATA = 2

data class RTMPPackage(
    var buffer: ByteArray? = null,
    var tms: Long = 0,
    var type: Int = RTMP_PACKET_TYPE_VIDEO
)