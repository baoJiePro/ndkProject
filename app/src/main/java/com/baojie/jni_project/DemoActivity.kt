package com.baojie.jni_project

import android.content.Context
import android.media.MediaCodecList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import com.baojie.jni_project.databinding.ActivityDemoBinding
import com.baojie.jni_project.maniu.play.H264Player
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils

class DemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDemoBinding
    private lateinit var context: Context
    private val path = "${Environment.getExternalStorageDirectory()}" + "/out.h264"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        checkWritePermission()

        doInit()
    }

    private fun initSurface() {
        val surface = binding.preview
        val surfaceHolder = surface.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                val h264Player = H264Player(context, path, surfaceHolder.surface)
//                h264Player.play()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

            }

        })
    }

    private fun checkWritePermission() {
        PermissionUtils.permission(PermissionConstants.STORAGE)
            .callback(object : PermissionUtils.FullCallback{
                override fun onGranted(granted: MutableList<String>) {
                    LogUtils.d("permission onGranted")
                    initSurface()
                }

                override fun onDenied(
                    deniedForever: MutableList<String>,
                    denied: MutableList<String>
                ) {
                    LogUtils.d("permission onDenied")
                }

            })
            .request()
    }

    private fun doInit() {
//        MediaCodec.createEncoderByType("avc")
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codes = list.codecInfos
        val decoderList = codes.filter {
            !it.isEncoder
        }.map {
            it.name
        }
        LogUtils.d("decoders: ", decoderList)

        val encoderList = codes.filter {
            it.isEncoder
        }.map {
            it.name
        }
        LogUtils.d("encoders: ", encoderList)

    }
}