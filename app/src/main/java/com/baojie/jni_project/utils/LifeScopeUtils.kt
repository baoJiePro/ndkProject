package com.baojie.jni_project.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

/**
 * @Description: TODO
 * @Author baojie@qding.me
 * @Date 2021/9/27 1:14 上午
 */
object LifeScopeUtils {

    const val TAG = "LifeScopeUtils"
    /**
     * 获取头部activity的lifeScope，没有时返回GlobalScope
     */
    fun getLifeScope(): CoroutineScope {
        val topActivity = ActivityUtils.getTopActivity()
        if (topActivity is AppCompatActivity){
            return topActivity.lifecycleScope
        }
        return GlobalScope
    }
}