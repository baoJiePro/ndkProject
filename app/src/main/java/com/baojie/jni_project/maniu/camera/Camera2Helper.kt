package com.baojie.jni_project.maniu.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import java.util.*

import kotlin.math.abs

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/10/21 11:33
 */
class Camera2Helper(val context: Context) {

    private var textureView: TextureView? = null
    private var previewViewSize: Point? = null
    private var previewSize: Size? = null
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private var imageReader: ImageReader? = null
    private var camera2Listener: Camera2Listener? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureSession: CameraCaptureSession? = null


    //    开启摄像头
    @SuppressLint("MissingPermission")
    @Synchronized
    fun start(view: TextureView) {
        textureView = view
        //摄像头的管理类
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        //这个摄像头的配置信息
        val characteristics = cameraManager.getCameraCharacteristics("0")
        // 以及获取图片输出的尺寸和预览画面输出的尺寸
        // 支持哪些格式 获取到的  摄像预览尺寸    textView
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        //寻找一个 最合适的尺寸     ---》 一模一样
        val data = map?.getOutputSizes(SurfaceTexture::class.java)?.toList()
        previewSize = getBestSupportedSize(data!!)
        //nv21      420   保存到文件
        imageReader = ImageReader.newInstance(
            previewSize!!.width,
            previewSize!!.height,
            ImageFormat.YUV_420_888,
            2
        )
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)

        imageReader?.setOnImageAvailableListener(OnImageAvailableListenerImpl(), mBackgroundHandler)
        cameraManager.openCamera("0", mDeviceStateCallback, mBackgroundHandler)
    }

    private val mDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            // 成功     前提       绝对
            mCameraDevice = camera
            //建立会话
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            mCameraDevice = null
        }

    }

    private fun createCameraPreviewSession() {
        val texture = textureView?.surfaceTexture ?: return
        // 设置预览宽高
        texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
        // 创建有一个Surface   画面  ---》1
        val surface = Surface(texture)
        // 预览 还不够
        mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewRequestBuilder?.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        //预览的textureView
        mPreviewRequestBuilder?.addTarget(surface)
        //必须设置  不然  文件   -----》
        mPreviewRequestBuilder?.addTarget(imageReader!!.surface)

        // 保存摄像头   数据  ---H264码流
        // 各种回调了
        //建立 链接     目的  几路 数据出口
        mCameraDevice?.createCaptureSession(
            listOf(surface, imageReader!!.surface),
            mCaptureStateCallback,
            mBackgroundHandler
        )
    }

    private val mCaptureStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            //            系统的相机
            // The camera is already closed
            mCameraDevice ?: return
            mPreviewRequestBuilder ?: return
            mCaptureSession = session
            mCaptureSession?.setRepeatingRequest(
                mPreviewRequestBuilder!!.build(),
                object : CameraCaptureSession.CaptureCallback() {

                },
                mBackgroundHandler
            )

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            TODO("Not yet implemented")
        }

    }

    fun setCamera2Listener(listener: Camera2Listener){
        camera2Listener = listener
    }

    interface Camera2Listener {
        /**
         * 预览数据回调
         * @param y 预览数据，Y分量
         * @param u 预览数据，U分量
         * @param v 预览数据，V分量
         * @param previewSize  预览尺寸
         * @param stride    步长
         */
        fun onPreview(y: ByteArray, u: ByteArray, v: ByteArray, preSize: Size, stride: Int)
    }

    inner class OnImageAvailableListenerImpl : ImageReader.OnImageAvailableListener {
        private var y: ByteArray? = null
        private var u: ByteArray? = null
        private var v: ByteArray? = null

        //        摄像 回调应用层  onPreviewFrame(byte[] )  这里 拿哪里
        override fun onImageAvailable(reader: ImageReader?) {
            //            不是设置回调了
            val image = reader?.acquireNextImage() ?: return
            //            搞事情           image 内容转换成
            //           yuv  H264
            val planes = image.planes ?: return
            if (y == null) {
                //                new  了一次
                //                limit  是 缓冲区 所有的大小     position 起始大小
                y = ByteArray(planes[0].buffer.limit() - planes[0].buffer.position())
                u = ByteArray(planes[1].buffer.limit() - planes[1].buffer.position())
                v = ByteArray(planes[2].buffer.limit() - planes[2].buffer.position())
            }
            if (image.planes[0].buffer.remaining() == y?.size) {
                //                分别填到 yuv
                planes[0].buffer.get(y)
                planes[1].buffer.get(u)
                planes[2].buffer.get(v)
                // yuv 420
            }
            camera2Listener?.onPreview(y!!, u!!, v!!, previewSize!!, planes[0].rowStride)
            image.close()
        }

    }

    private fun getBestSupportedSize(sizes: List<Size>): Size {
        val maxPreviewSize = Point(1920, 1080)
        val minPreviewSize = Point(1280, 720)
        val defaultSize = sizes[0]
        val tempSizes = sizes.toTypedArray()
        Arrays.sort(
            tempSizes
        ) { o1, o2 ->
            if (o1.width > o2.width) {
                -1
            } else if (o1.width == o2.width) {
                if (o1.height > o2.height) {
                    -1
                } else {
                    1
                }
            } else {
                1
            }
        }
        val tempSizesList = tempSizes.toMutableList()
        for (i in tempSizesList.size - 1 downTo 0) {
            if (tempSizesList[i].width > maxPreviewSize.x || tempSizesList[i].height > maxPreviewSize.y) {
                tempSizesList.removeAt(i)
                continue
            }
            if (tempSizesList[i].width < minPreviewSize.x || tempSizesList[i].height < minPreviewSize.y) {
                tempSizesList.removeAt(i)
                continue
            }
        }
        if (tempSizesList.size == 0) {
            return defaultSize
        }
        var bestSize = tempSizesList[0]
        var previewViewRatio: Float
        previewViewRatio = if (previewViewSize != null) {
            previewViewSize!!.x.toFloat() / previewViewSize!!.y.toFloat()
        } else {
            bestSize.width.toFloat() / bestSize.height.toFloat()
        }
        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio
        }
        tempSizesList.forEach {
            val left = it.height.toFloat() / it.width.toFloat() - previewViewRatio
            val leftAbs = abs(left)
            val right = bestSize.height.toFloat() / bestSize.width.toFloat() - previewViewRatio
            val rightAbs = abs(right)
            if (leftAbs < rightAbs) {
                bestSize = it
            }
        }
        return bestSize
    }
}