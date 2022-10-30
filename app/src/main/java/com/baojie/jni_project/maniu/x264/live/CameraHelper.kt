package com.baojie.jni_project.maniu.x264.live

import android.graphics.ImageFormat
import android.hardware.Camera
import android.view.Surface
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.baojie.jni_project.maniu.videochat.YuvUtils
import com.baojie.jni_project.utils.ImageUtil
import com.blankj.utilcode.util.LogUtils
import java.io.IOException
import kotlin.math.abs

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/29 20:59
 */
class CameraHelper(
    var activity: AppCompatActivity,
    var cameraId: Int,
    var width: Int,
    var height: Int
): SurfaceHolder.Callback, Camera.PreviewCallback {

    private val TAG = this.javaClass.simpleName

    private var camera: Camera? = null
    private var rotation: Int = 0
    private var buffer: ByteArray? = null
    private var yuv: ByteArray? = null
    private var surfaceHolder: SurfaceHolder? = null

    private var onChangeSizeListener: OnChangedSizeListener? = null
    private var previewCallback: Camera.PreviewCallback? = null

    fun switchCamera(){
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
        }else{
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        stopPreview()
        startPreview()
    }

    private fun startPreview() {
        //获得camera对象
        camera = Camera.open(cameraId)
        //配置camera的属性
        val parameters = camera?.parameters
        parameters ?: return
        //设置预览数据格式为nv21
        parameters.previewFormat = ImageFormat.NV21
        //这是摄像头宽、高
        setPreviewSize(parameters)
        // 设置摄像头 图像传感器的角度、方向
        setPreviewOrientation(parameters)
        camera?.parameters = parameters
        buffer = ByteArray(width * height * 3 / 2)
        yuv = ByteArray(width * height * 3 / 2)
        //数据缓存区
        camera?.addCallbackBuffer(buffer)
        camera?.setPreviewCallbackWithBuffer(this)
        //设置预览画面
        try {
            camera?.setPreviewDisplay(surfaceHolder)
            onChangeSizeListener?.onChanged(width, height)
            camera?.startPreview()
        }catch (e: IOException){
            e.printStackTrace()
        }

    }

    private fun setPreviewOrientation(parameters: Camera.Parameters) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when(rotation){
            Surface.ROTATION_0 -> {
                degrees = 0
            }
            Surface.ROTATION_90 -> {
                // 横屏 左边是头部(home键在右边)
                degrees = 90
            }
            Surface.ROTATION_180 -> {
                degrees = 180
            }
            Surface.ROTATION_270 -> {
                // 横屏 头部在右边
                degrees = 270
            }
        }
        var result = 0
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        }else{
            result = (info.orientation - degrees + 360) % 360
        }
        camera?.setDisplayOrientation(result)

    }

    private fun setPreviewSize(parameters: Camera.Parameters) {
        val supportedPreviewSizes = parameters.supportedPreviewSizes
        var size = supportedPreviewSizes[0]
        LogUtils.dTag(TAG, "支持：${size.width} * ${size.height}")
        //选择一个与设置的差距最小的支持分辨率
        // 10x10 20x20 30x30
        // 12x12
        var m = abs(size.height * size.width - width * height)
        supportedPreviewSizes.removeAt(0)
        val iterator = supportedPreviewSizes.iterator()
        while (iterator.hasNext()){
            val next = iterator.next()
            LogUtils.dTag(TAG, "支持：${next.width} * ${next.height}")
            var n = abs(next.height * next.width - width * height)
            if (n < m){
                m = n
                size = next
            }
        }
        width = size.width
        height = size.height
        parameters.setPreviewSize(width, height)
        LogUtils.dTag(TAG, "预览分辨率：width: ${size.width}, height: ${size.height}")
    }

    fun setPreviewDisplay(sur: SurfaceHolder){
        surfaceHolder = sur
        surfaceHolder?.addCallback(this)
    }

    fun setPreviewCallback(callback: Camera.PreviewCallback){
        previewCallback = callback
    }

    private fun stopPreview() {

    }

    fun setOnChangedSizeListener(listener: OnChangedSizeListener){
        onChangeSizeListener = listener
    }
    interface OnChangedSizeListener{
        fun onChanged(w: Int, h: Int)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        //释放摄像头
        stopPreview()
        //开启摄像头
        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopPreview()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        // data数据依然是倒的
        ImageUtil.nv21_rotate_to_90(data, yuv, width, height)
        previewCallback?.onPreviewFrame(yuv, camera)
        camera?.addCallbackBuffer(buffer)
    }
}