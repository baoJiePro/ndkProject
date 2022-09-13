package com.baojie.jni_project

import android.app.Application
import com.blankj.utilcode.util.LogUtils

/**
 * @Description:
 * @Author baoJie
 * @Date 2022/9/12 14:29
 */
class Capplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initLog()
    }

    private fun initLog() {
        val logConfig = LogUtils.getConfig()
            .setLogSwitch(true)// 设置log总开关，包括输出到控制台和文件，默认开
            .setConsoleSwitch(true)// 设置是否输出到控制台开关，默认开
            // 设置log全局标签，默认为空, 当全局标签不为空时，我们输出的log全部为该tag，为空时，如果传入的tag为空那就显示类名，否则显示tag
            .setGlobalTag("anJin")
            .setLogHeadSwitch(true)// 设置log头信息开关，默认为开
            .setLog2FileSwitch(false)// 打印log时是否存到文件的开关，默认关
            .setDir("")// 当自定义路径为空时，写入应用的/cache/log/目录中
            .setFilePrefix("")// 当文件前缀为空时，默认为"util"，即写入文件为"util-yyyy-MM-dd$fileExtension"
            .setFileExtension(".log")// 设置日志文件后缀
            .setBorderSwitch(true)// 输出日志是否带边框开关，默认开
            .setSingleTagSwitch(true)// 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
            .setConsoleFilter(LogUtils.V)// log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
            .setFileFilter(LogUtils.V)// log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
            .setStackDeep(1)// log 栈深度，默认为 1
            .setStackOffset(0)// 设置栈偏移，比如二次封装的话就需要设置，默认为 0
            .setSaveDays(3)// 设置日志可保留天数，默认为 -1 表示无限时长
            // 新增 ArrayList 格式化器，默认已支持 Array, Throwable, Bundle, Intent 的格式化输出
            .addFormatter(object : LogUtils.IFormatter<ArrayList<String>>() {
                override fun format(t: ArrayList<String>?): String {
                    return "LogUtils Formatter ArrayList { " + t.toString() + " }"
                }
            })
            .addFileExtraHead("ExtraKey", "ExtraValue")

//        LogUtils.i(logConfig.toString())
    }
}