package com.baojie.jni_project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.baojie.jni_project.bean.Student
import com.baojie.jni_project.databinding.ActivityObjectBinding
import com.blankj.utilcode.util.LogUtils

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
        ints: IntArray,
        strs: Array<String>
    ) // String引用类型，玩数组

    private external fun putObject(student: Student, name: String)

    private external fun putListObject(list: ArrayList<Student>)

    private external fun getStudent(): Student


    fun test01(view: View) {
//        val ints = arrayOf<Int>(1,2,3,4,5)
        val ints = intArrayOf(1,2,3,4,5)
        val strs = arrayOf<String>("a", "bb", "ccc")
        testArrayAction(99, "aa", ints, strs)
        ints.forEach {
            LogUtils.d(it)
        }
    }
    fun test02(view: View) {
        val student = Student()
        student.age = 18
        student.name = "aa"
        putObject(student, "bb")
    }
    fun test03(view: View) {
        val student = Student("aa", 18)
        val student2 = Student("bb", 20)
        val list = arrayListOf<Student>()
        list.add(student)
        list.add(student2)
        putListObject(list)
    }


    fun test04(view: View) {
        val student = getStudent()
        LogUtils.d(student)
    }
    fun test05(view: View) {

    }
}