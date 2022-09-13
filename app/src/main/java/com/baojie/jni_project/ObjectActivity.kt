package com.baojie.jni_project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.baojie.jni_project.databinding.ActivityObjectBinding

class ObjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityObjectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }

    /**
     * 下面是 native 区域
     */


    private external fun testArrayAction(
        count: Int,
        textInfo: String?,
        ints: IntArray?,
        strs: Array<String?>?
    ) // String引用类型，玩数组


    fun test01(view: View) {

    }
    fun test02(view: View) {

    }
    fun test03(view: View) {

    }
    fun test04(view: View) {

    }
    fun test05(view: View) {

    }
}