package com.baojie.jni_project.maniu.rtmpbili

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/28 21:13
 */

data class RTMPPackage(
    var buffer: ByteArray? = null,
    var tms: Long = 0
)