package com.baojie.jni_project.maniu.videochat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import com.baojie.jni_project.databinding.ActivityVideoChatBinding
import com.baojie.jni_project.maniu.touping.player.SocketLive

class VideoChatActivity : AppCompatActivity(), SocketLive.SocketCallback {

    private lateinit var binding: ActivityVideoChatBinding
    private var decodecPlayerLiveH265: DecodecPlayerLiveH265 ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.sfv.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                decodecPlayerLiveH265 = DecodecPlayerLiveH265()
                decodecPlayerLiveH265?.initDecoder(holder.surface)
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

    fun connect(view: View) {
        binding.lsv.startCapture(this)
    }

    override fun callBack(data: ByteArray) {
        decodecPlayerLiveH265?.callBack(data)
    }
}