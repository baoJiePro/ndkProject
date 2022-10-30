package com.baojie.jni_project.camerax

import android.Manifest
import android.content.ContentValues
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Rational
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.baojie.jni_project.R
import com.baojie.jni_project.databinding.ActivityCameraxBinding
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import java.util.concurrent.ExecutorService

class CameraxActivity : AppCompatActivity() {

    private val TAG = this.javaClass.simpleName

    private lateinit var binding: ActivityCameraxBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var singePool: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPermission()

        initTexture()

        initClick()

        singePool = ThreadUtils.getSinglePool()
    }

    private fun initTexture() {
        binding.textureView.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->
            updateTransform()
        }

        binding.textureView.post {
            startCamera2()
        }
    }

    private fun startCamera2() {
        val preview = Preview.Builder()
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetResolution(Size(640, 640))
            .build()
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = binding.textureView.width / 2f
        val centerY = binding.textureView.height / 2f
        val rotations = arrayOf(0f, 90f, 180f, 270f)
        val rotationDegrees = rotations[binding.textureView.display.rotation]
        matrix.postRotate(-rotationDegrees, centerX, centerY)
        binding.textureView.setTransform(matrix)
    }

    private fun initClick() {
        binding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }
        binding.videoCaptureButton.setOnClickListener {
            captureVideo()
        }
    }

    private fun initPermission() {

        PermissionUtils.permission(
            PermissionConstants.STORAGE,
            PermissionConstants.CAMERA,
            PermissionConstants.MICROPHONE
        )
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(granted: MutableList<String>) {
                    LogUtils.d("permission onGranted")
                    startCamera()
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

    private fun startCamera() {
        //这用于将相机的生命周期绑定到生命周期所有者。这消除了打开和关闭相机的任务，因为 CameraX 具有生命周期感知能力
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            //它用于将相机的生命周期绑定到应用进程中的 LifecycleOwner
            val cameraProvider = cameraProviderFuture.get()
            //从取景器中获取 Surface 提供程序，然后在预览上进行设置
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
//            imageCapture = ImageCapture.Builder()
//                .build()
//            val imageAnalyzer = ImageAnalysis.Builder().build().also {
//                it.setAnalyzer(singePool, object : LuminosityAnalyzer(){
//                    override fun listener(luma: Double) {
//                        LogUtils.dTag(TAG, "Average luminosity: $luma")
//                    }
//
//                })
//            }
            val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                //在此块内，确保没有任何内容绑定到 cameraProvider，然后将 cameraSelector 和预览对象绑定到 cameraProvider
                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                LogUtils.dTag(TAG, "Use case binding failed $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return
        binding.videoCaptureButton.isEnabled = false
        val curRecording = recording
        //如果有正在进行的录制操作，请将其停止并释放当前的 recording。当所捕获的视频文件可供我们的应用使用时，我们会收到通知
        if (curRecording != null){
            curRecording.stop()
            recording = null
            return
        }
        //为了开始录制，我们会创建一个新的录制会话。首先，我们创建预定的 MediaStore 视频内容对象，将系统时间戳作为显示名（以便我们可以捕获多个视频）
        val name = "video_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }
        //使用外部内容选项创建 MediaStoreOutputOptions.Builder
        //将创建的视频 contentValues 设置为 MediaStoreOutputOptions.Builder，并构建我们的 MediaStoreOutputOptions 实例
        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        //将输出选项配置为 VideoCapture<Recorder> 的 Recorder 并启用录音：
        recording = videoCapture.output.prepareRecording(this, mediaStoreOutputOptions).apply {
            if (PermissionChecker.checkSelfPermission(this@CameraxActivity,
                    Manifest.permission.RECORD_AUDIO) ==
                PermissionChecker.PERMISSION_GRANTED)
            {
                withAudioEnabled()
            }
            //启动这项新录制内容，并注册一个 lambda VideoRecordEvent 监听器。
        }.start(ContextCompat.getMainExecutor(this)){ recordEvent ->
            when(recordEvent){
                //当相机设备开始请求录制时，将“Start Capture”按钮文本切换为“Stop Capture”
                is VideoRecordEvent.Start ->{
                    binding.videoCaptureButton.apply {
                        text = getString(R.string.stop_capture)
                        isEnabled = true
                    }
                }
                //完成录制后，用消息框通知用户，并将“Stop Capture”按钮切换回“Start Capture”，然后重新启用它
                is VideoRecordEvent.Finalize -> {
                    if (!recordEvent.hasError()) {
                        val msg = "Video capture succeeded: " +
                                "${recordEvent.outputResults.outputUri}"
                        ToastUtils.showShort(msg)
                        LogUtils.dTag(TAG, msg)
                    } else {
                        recording?.close()
                        recording = null
                        LogUtils.dTag(TAG, "Video capture ends with error: " +
                                "${recordEvent.error}")
                    }
                    binding.videoCaptureButton.apply {
                        text = getString(R.string.start_capture)
                        isEnabled = true
                    }
                }
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        //创建用于保存图片的 MediaStore 内容值。请使用时间戳，确保 MediaStore 中的显示名是唯一的
        val name = "img_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        //在该对象中，您可以指定所需的输出内容。我们希望将输出保存在 MediaStore 中，以便其他应用可以显示它，因此，请添加我们的 MediaStore 条目
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            .build()
        //对 imageCapture 对象调用 takePicture()。传入 outputOptions、执行器和保存图片时使用的回调。接下来，您需要填写回调
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${outputFileResults.savedUri}"
                    ToastUtils.showShort(msg)
                    LogUtils.dTag(TAG, msg)
                }

                override fun onError(exception: ImageCaptureException) {
                    LogUtils.dTag(TAG, "Photo capture failed: ${exception.message}")
                }

            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        singePool.shutdown()
    }
}