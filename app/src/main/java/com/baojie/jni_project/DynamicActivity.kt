package com.baojie.jni_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.baojie.jni_project.constants.TAG
import com.baojie.jni_project.databinding.ActivityDynamicBinding
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils

class DynamicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDynamicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun dynamic01(view: View) {
        dynamicJavaMethod01();
    }
    fun dynamic02(view: View) {
        val result = dynamicJavaMethod02("aa")
        ToastUtils.showShort("$result")
    }
    fun nativeCallJava(view: View) {
        nativeThread()
    }
    fun clickMethod4(view: View) {
        val arr = intArrayOf(11, 22, 5, 8, 3, 44, -7)
        sort(arr)
        LogUtils.dTag(TAG, arr)
    }
    fun clickMethod5(view: View) {

    }

    fun updateActivityUI(){
        if (Looper.getMainLooper() == Looper.myLooper()){
            AlertDialog.Builder(this).setTitle("ui")
                .setMessage("是主线程调用")
                .setPositiveButton("知道了", null)
                .show()
        }else{
            runOnUiThread {
                AlertDialog.Builder(this).setTitle("ui")
                    .setMessage("是子线程调用")
                    .setPositiveButton("知道了", null)
                    .show()
            }
        }
    }

    private external fun dynamicJavaMethod01()
    private external fun dynamicJavaMethod02(valueStr: String): Int
    private external fun nativeThread()
    private external fun sort(arr: IntArray)
    private external fun exception()


}