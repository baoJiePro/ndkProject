package com.baojie.jni_project.maniu.rtmpbili

import android.Manifest
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.baojie.jni_project.R
import com.baojie.jni_project.databinding.ActivityRtmpBiliBinding

class RtmpBiliActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRtmpBiliBinding

    private lateinit var mediaProjectionManager: MediaProjectionManager

    private lateinit var startActivity: ActivityResultLauncher<Intent>

    private var mediaProjection: MediaProjection? = null

    private val rtmpUrl = ""
    private var screenLive: ScreenLive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRtmpBiliBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        registerActivityResult()

        initClick()

    }

    private fun registerActivityResult() {
        //StartActivityForResult
        startActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            result?.let {
                it.data?.let { data ->
                    //mediaProjection--->产生录屏数据
                    mediaProjection = mediaProjectionManager.getMediaProjection(it.resultCode, data)
                    mediaProjection ?: return@registerForActivityResult
                    screenLive = ScreenLive()
                    screenLive?.startLive(rtmpUrl, mediaProjection!!)

                }

            }
        }

        //通过MediaStore.ACTION_IMAGE_CAPTURE拍照并保存 保存文件的Uri 是否保存成功
        val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()){

        }

        //通过MediaStore.ACTION_IMAGE_CAPTURE拍照 null(Void) 图片的Bitmap
        val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){

        }

        //通过MediaStore.ACTION_VIDEO_CAPTURE拍摄视频并保存 保存文件的Uri 是否保存成功
        val captureVideo = registerForActivityResult(ActivityResultContracts.CaptureVideo()){

        }

        //请求单个权限 Manifest.permission.* 用户是否授予该权限
        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){

        }
        // 请求多个权限 Array<Manifest.permission.*> 回调为map, key为请求的权限，value为用户是否授予该权限
        val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){

        }

//        requestMultiplePermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        //通过Intent.ACTION_PICK从系统通讯录中获取联系人 null(Void) 联系人Uri
        val pickContact = registerForActivityResult(ActivityResultContracts.PickContact()){

        }
    }

    private fun initClick() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivity.launch(captureIntent)
    }
}