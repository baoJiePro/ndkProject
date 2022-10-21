package com.baojie.jni_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.baojie.jni_project.constants.*
import com.baojie.jni_project.databinding.ActivityQqBinding
import com.blankj.utilcode.util.ToastUtils
import org.fmod.FMOD

class QqActivity : AppCompatActivity() {

    private lateinit var path: String

    private lateinit var binding: ActivityQqBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FMOD.init(this)

        path =  "file:///android_asset/derry.mp3"
    }

    fun onFix(view: View) {

        when(view.id){
            R.id.btn_normal -> {
                voiceChangeNative(MODE_NORMAL, path)
            }
            R.id.btn_luoli -> {
                voiceChangeNative(MODE_LUOLI, path)
            }
            R.id.btn_dashu -> {
                voiceChangeNative(MODE_DASHU, path)
            }
            R.id.btn_jingsong -> {
                voiceChangeNative(MODE_JINGSONG, path)
            }
            R.id.btn_gaoguai -> {
                voiceChangeNative(MODE_GAOGUAI, path)
            }
            R.id.btn_kongling -> {
                voiceChangeNative(MODE_KONGLING, path)
            }
        }
    }

    private external fun voiceChangeNative(mode: Int,path: String)

    fun playEnd(msg: String){
        ToastUtils.showShort(msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        FMOD.close()
    }
}