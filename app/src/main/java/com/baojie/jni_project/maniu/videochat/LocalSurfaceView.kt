package com.baojie.jni_project.maniu.videochat

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.baojie.jni_project.maniu.touping.player.SocketLive

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/20 13:38
 */
class LocalSurfaceView(context: Context, attrs: AttributeSet): SurfaceView(context, attrs), SurfaceHolder.Callback, Camera.PreviewCallback {

    private var size: Camera.Size? = null
    private var camera: Camera? = null
    private var buffers: ByteArray ?= null
    private var encodecPushLiveH265: EncodecPushLiveH265 ?= null

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startPreView()
    }

    private fun startPreView() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        val parameters = camera?.parameters
        size = parameters?.previewSize
        camera?.setPreviewDisplay(holder)
        camera?.setDisplayOrientation(90)
        buffers = ByteArray(size!!.width * size!!.height * 3 / 2)
        camera?.addCallbackBuffer(buffers)
        camera?.setPreviewCallbackWithBuffer(this)
        camera?.startPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    fun startCapture(socketCallback: SocketLive.SocketCallback){
        encodecPushLiveH265 = EncodecPushLiveH265(socketCallback, size!!.width, size!!.height)
        encodecPushLiveH265?.startLive()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        //        获取到摄像头的原始数据yuv
        //        开始    视频通话
        data?.let {
            encodecPushLiveH265?.encodeFrame(it)
        }

        camera?.addCallbackBuffer(data)
    }
}