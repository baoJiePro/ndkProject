package com.baojie.jni_project.maniu.x264

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baojie.jni_project.R
import com.baojie.jni_project.databinding.ActivityX264Binding
import com.baojie.jni_project.maniu.x264.live.LivePusher

class X264Activity : AppCompatActivity() {

    private lateinit var binding: ActivityX264Binding

    private val url = ""

    private lateinit var livePusher: LivePusher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityX264Binding.inflate(layoutInflater)
        setContentView(binding.root)

        livePusher =
            LivePusher(this, 800, 480, 800_000, 10, Camera.CameraInfo.CAMERA_FACING_BACK)
        livePusher.setPreviewDisplay(binding.sv.holder)

        initClick()
    }

    private fun initClick() {
        binding.btn1.setOnClickListener {
            livePusher.switchCamera()
        }
        binding.btn2.setOnClickListener {
            livePusher.startLive(url)
        }
        binding.btn3.setOnClickListener {
            livePusher.stopLive()
        }
    }
}