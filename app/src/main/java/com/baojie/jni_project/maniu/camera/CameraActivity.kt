package com.baojie.jni_project.maniu.camera

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.view.TextureView
import com.baojie.jni_project.databinding.ActivityCameraBinding
import com.baojie.jni_project.maniu.videochat.YuvUtils.writeBytes
import com.baojie.jni_project.maniu.videochat.YuvUtils.writeContent
import com.baojie.jni_project.utils.ImageUtil

class CameraActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private lateinit var binding: ActivityCameraBinding

    private var camera2Helper: Camera2Helper? = null
    private var mediaCodec: MediaCodec? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ttv.surfaceTextureListener = this

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        //        打开摄像头  先后顺序
        initCamera()
    }

    private fun initCamera() {
        camera2Helper = Camera2Helper(this)
        camera2Helper?.start(binding.ttv)
        camera2Helper?.setCamera2Listener(camera2Listener)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    //   先转成nv21   再转成  yuv420    n21 横着   1   竖着
    private var nv21: ByteArray? = null
    private var nv21_rotated: ByteArray? = null
    private var nv12: ByteArray? = null
    private val camera2Listener = object : Camera2Helper.Camera2Listener{
        override fun onPreview(
            y: ByteArray,
            u: ByteArray,
            v: ByteArray,
            preSize: Size,
            stride: Int
        ) {
            if (nv21 == null){
                nv21 = ByteArray(stride * preSize.height * 3 / 2)
                nv21_rotated = ByteArray(stride * preSize.height * 3 / 2)
            }
            if (mediaCodec == null){
                initCodec(preSize)
            }

            ImageUtil.yuvToNv21(y, u, v, nv21, stride, preSize.height)
            //对数据进行旋转   90度
            ImageUtil.nv21_rotate_to_90(nv21, nv21_rotated, stride, preSize.height)
            //Nv12     yuv420
            val temp = ImageUtil.nv21toNV12(nv21_rotated, nv12)
            val bufferInfo = MediaCodec.BufferInfo()
            val index = mediaCodec?.dequeueInputBuffer(100000) ?: -1
            if (index >= 0){
                val byteBuffer = mediaCodec?.getInputBuffer(index)
                byteBuffer?.clear()
                byteBuffer?.put(temp, 0, temp.size)
                mediaCodec?.queueInputBuffer(index, 0, temp.size, 0, 0)
            }
            val outIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 100000) ?: -1
            if (outIndex >= 0){
                val byteBuffer = mediaCodec?.getOutputBuffer(outIndex)
                val ba = ByteArray(byteBuffer!!.remaining())
                byteBuffer.get(ba)
                writeContent(ba)
                writeBytes(ba)
                mediaCodec?.releaseOutputBuffer(outIndex, false)
            }

        }

    }

    private fun initCodec(preSize: Size) {
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val mediaformat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, preSize.height, preSize.width)
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        mediaformat.setInteger(MediaFormat.KEY_BIT_RATE, 4000_000)
        mediaformat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        mediaCodec?.configure(mediaformat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec?.start()

    }
}