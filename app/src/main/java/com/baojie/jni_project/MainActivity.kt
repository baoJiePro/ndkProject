package com.baojie.jni_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.baojie.jni_project.databinding.ActivityMainBinding
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var name = "aaa"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()
        changeName()
        binding.tvName.text = name
        changeAge()
        binding.tvAge.text = age.toString()
        callAdd()

        binding.goTo.setOnClickListener {
            ActivityUtils.startActivity(ObjectActivity::class.java)
        }
    }

    /**
     * A native method that is implemented by the 'jni_project' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): String

    private external fun changeName()

    private external fun changeAge()

    private external fun callAdd()

    fun add(a:Int, b: Int): Int {
        LogUtils.d(a,b)
        return a + b
    }

    companion object {
        private val age = 18
        // Used to load the 'jni_project' library on application startup.
        init {
            System.loadLibrary("jni_project")
        }
    }
}