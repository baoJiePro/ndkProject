#include <jni.h>
#include <string>

#include <android/log.h>
#include <unistd.h>
#include <pthread.h>
#include "fmod.hpp"
using namespace FMOD;
#define TAG "anJin"
// ... 我都不知道传入什么  借助JNI里面的宏来自动帮我填充
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

extern "C"{
#include "librtmp/rtmp.h"
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_baojie_jni_1project_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_MainActivity_changeName(JNIEnv *env, jobject thiz) {
    // 获取class
//    jclass j_cls = env->GetObjectClass(thiz);
//    // 获取属性  L对象类型 都需要L
//    // jfieldID GetFieldID(MainActivity.class, 属性名, 属性的签名)
//    jfieldID jfieldId = env->GetFieldID(j_cls, "name", "Ljava/lang/String;");
//    // 转换工作
//    jstring j_str = static_cast<jstring>(env->GetObjectField(thiz, jfieldId));
//    // 打印字符串  目标
//    char * c_str = const_cast<char *>(env->GetStringUTFChars(j_str, NULL));
//    LOGD("native: %s\n", c_str);
//
//    // 修改成 bbb
//    jstring jName = env->NewStringUTF("bbb");
//    env->SetObjectField(thiz, jfieldId, jName);


    jclass j_cls = env->GetObjectClass(thiz);
    jfieldID j_fid = env->GetFieldID(j_cls, "name", "Ljava/lang/String;");

    jstring j_str = static_cast<jstring>(env->GetObjectField(thiz, j_fid));
    char * c_str = const_cast<char *>(env->GetStringUTFChars(j_str, NULL));
    LOGD("native: %s\n", c_str);

    jstring jName = env->NewStringUTF("bbb");
    env->SetObjectField(thiz, j_fid, jName);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_MainActivity_changeAge(JNIEnv *env, jobject thiz) {
    jclass j_cls = env->GetObjectClass(thiz);
    jfieldID j_fid = env->GetStaticFieldID(j_cls, "age", "I");
    jint age = env->GetStaticIntField(j_cls, j_fid);
    age += 10;
    env->SetStaticIntField(j_cls, j_fid, age);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_MainActivity_callAdd(JNIEnv *env, jobject thiz) {
    jclass j_cls = env->GetObjectClass(thiz);
    jmethodID j_mid = env->GetMethodID(j_cls, "add", "(II)I");
    jint result = env->CallIntMethod(thiz, j_mid, 2, 3);
    LOGD("result: %d\n", result);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_ObjectActivity_testArrayAction(JNIEnv *env, jobject thiz, jint count,
                                                            jstring text_info, jintArray ints,
                                                            jobjectArray strs) {
    // ① 基本数据类型  jint count， jstring text_info， 最简单的
    int countInt = count;
    LOGI("参数1：countInt： %d\n", countInt);

    const char* textInfo = env->GetStringUTFChars(text_info, NULL);
    LOGI("参数2：textInfo: %s\n", textInfo);

    jint* jintArray = env->GetIntArrayElements(ints, NULL);

    jsize size = env->GetArrayLength(ints);
    for (int i = 0; i < size; ++i) {
        *(jintArray + i) += 100;
        LOGI("参数3：%d\n", *jintArray + i);
    }

    /**
     * 0:           刷新Java数组，并 释放C++层数组
     * JNI_COMMIT:  只提交 只刷新Java数组，不释放C++层数组
     * JNI_ABORT:   只释放C++层数组
     */
    env->ReleaseIntArrayElements(ints, jintArray, 0);

    jsize strSize = env->GetArrayLength(strs);
    for (int i = 0; i < strSize; ++i) {
       jstring jObj = static_cast<jstring>(env->GetObjectArrayElement(strs, i));
        const char* item = env->GetStringUTFChars(jObj, NULL);
        LOGI("参数四 引用类型String 具体的：%s\n", item);
        env->ReleaseStringUTFChars(jObj, item);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_ObjectActivity_putObject(JNIEnv *env, jobject thiz, jobject student,
                                                      jstring name) {
    const char* jName = env->GetStringUTFChars(name, NULL);
    LOGI("name: %s\n", jName);

    jclass studentClass = env->GetObjectClass(student);
    jmethodID setName = env->GetMethodID(studentClass, "setName", "(Ljava/lang/String;)V");
    jmethodID getName = env->GetMethodID(studentClass, "getName", "()Ljava/lang/String;");
    jstring value = env->NewStringUTF("ccc");
    env->CallVoidMethod(student, setName, value);
    jstring getNameValue = static_cast<jstring>(env->CallObjectMethod(student, getName));
    const char* getNameResult = env->GetStringUTFChars(getNameValue, NULL);
    LOGI("调用到getName方法，值是:%s\n", getNameResult);
}

typedef struct {
    char name[50];
    int age;
} Student;


extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_ObjectActivity_putListObject(JNIEnv *env, jobject thiz, jobject list) {

//    jclass studentCls = env->FindClass("com/baojie/jni_project/bean/Student");
    jclass listClass = env->GetObjectClass(list);
    jmethodID getMethodId = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
    jmethodID sizeMethodId = env->GetMethodID(listClass, "size", "()I");
    jint size = env->CallIntMethod(list, sizeMethodId);
    for (int i = 0; i < size; ++i) {
        //获取student对象
        jobject stuObj = env->CallObjectMethod(list, getMethodId, i);
        jclass stuClass = env->GetObjectClass(stuObj);
        jmethodID getNameMethodId = env->GetMethodID(stuClass, "getName", "()Ljava/lang/String;");
        jstring nameString = static_cast<jstring>(env->CallObjectMethod(stuObj, getNameMethodId));
        const char* name =  env->GetStringUTFChars(nameString, nullptr);
        LOGI("name=%s\n", name);
        jmethodID getAgeMethodId = env->GetMethodID(stuClass, "getAge", "()I");
        jint age = env->CallIntMethod(stuObj, getAgeMethodId);
        LOGI("age=%d\n", age);
    }
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_baojie_jni_1project_ObjectActivity_getStudent(JNIEnv *env, jobject thiz) {
    jclass  stu_class = env->FindClass("com/baojie/jni_project/bean/Student");
    jmethodID stu_init = env->GetMethodID(stu_class, "<init>", "()V");
    jobject stu_obj = env->NewObject(stu_class, stu_init);
    jmethodID stu_set_name = env->GetMethodID(stu_class, "setName", "(Ljava/lang/String;)V");
    jmethodID stu_set_age = env->GetMethodID(stu_class, "setAge", "(I)V");
    jstring name = env->NewStringUTF("anJin");
    env->CallVoidMethod(stu_obj, stu_set_name, name);
    env->CallVoidMethod(stu_obj, stu_set_age, 28);
    return stu_obj;
}











































extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_QqActivity_voiceChangeNative(JNIEnv *env, jobject thiz, jint mode,
                                                          jstring path) {

    char * content_ = "默认 播放完毕";

    // C认识的字符串
    const char * path_ = env->GetStringUTFChars(path, NULL);

    // Java  对象
    // C     指针
    // Linux 文件

    // 音效引擎系统 指针
    System * system = 0;

    // 声音 指针
    Sound * sound = 0;

    // 通道，音轨，声音在上面跑 跑道 指针
    Channel * channel = 0;

    // DSP：digital signal process  == 数字信号处理  指针
    DSP * dsp = 0;

    // Java思想 去初始化
    // system = xxxx();

    // C的思想 初始化
    // xxxx(&system);

    // TODO 第一步 创建系统
    System_Create(&system);

    // TODO 第二步 系统的初始化 参数1：最大音轨数，  参数2：系统初始化标记， 参数3：额外数据
    system->init(32, FMOD_INIT_NORMAL, 0);

    // TODO 第三步 创建声音  参数1：路径，  参数2：声音初始化标记， 参数3：额外数据， 参数4：声音指针
    system->createSound(path_, FMOD_DEFAULT, 0, &sound);

    // TODO 第四步：播放声音  音轨 声音
    // 参数1：声音，  参数2：分组音轨， 参数3：控制， 参数4：通道
    system->playSound(sound, 0, false, &channel);

    switch(mode){
        case 0:
            content_ = "原生 播放完毕";
            break;
        case 1:
            content_ = "萝莉 播放完毕";
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 2.0f);
            channel->addDSP(0, dsp);
            break;
        case 2:
            content_ = "大叔 播放完毕";
            // 音调低 -- 大叔 0.7
            // 1.创建DSP类型的Pitch 音调条件
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            // 2.设置Pitch音调调节2.0
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.7f);
            // 3.添加音效进去 音轨
            channel->addDSP(0, dsp);
            break;
        case 3:
            content_ = "搞怪 小黄人 播放完毕";

            // 小黄人声音 频率快

            // 从音轨拿 当前 频率
            float mFrequency;
            channel->getFrequency(&mFrequency);

            // 修改频率
            channel->setFrequency(mFrequency * 1.5f); // 频率加快  小黄人的声音
            break;
        case 4:
            content_ = "惊悚 播放完毕";

            // 惊悚音效：特点： 很多声音的拼接

            // TODO 音调低
            // 音调低 -- 大叔 0.7
            // 1.创建DSP类型的Pitch 音调条件
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            // 2.设置Pitch音调调节2.0
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.7f);
            // 3.添加音效进去 音轨
            channel->addDSP(0, dsp); // 第一个音轨

            // TODO 搞点回声
            // 回音 ECHO
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 200); // 回音 延时    to 5000.  Default = 500.
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10); // 回音 衰减度 Default = 50   0 完全衰减了
            channel->addDSP(1,dsp); // 第二个音轨

            // TODO 颤抖
            // Tremolo 颤抖音 正常5    非常颤抖  20
            system->createDSPByType(FMOD_DSP_TYPE_TREMOLO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_TREMOLO_FREQUENCY, 20); // 非常颤抖
            dsp->setParameterFloat(FMOD_DSP_TREMOLO_SKEW, 0.8f); // ？？？
            channel->addDSP(2, dsp); // 第三个音轨

            // 调音师：才能跳出来  同学们自己去调
            break;
        case 5:
            content_ = "空灵 播放完毕";

            // 回音 ECHO
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 200); // 回音 延时    to 5000.  Default = 500.
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10); // 回音 衰减度 Default = 50   0 完全衰减了
            channel->addDSP(0,dsp);
            break;
    }

    // 等待播放完毕 再回收
    bool isPlayer = true; // 你用不是一级指针  我用一级指针接收你，可以修改给你
    while (isPlayer) {
        channel->isPlaying(&isPlayer); // 如果真的播放完成了，音轨是知道的，内部会修改isPlayer=false
        usleep(1000 * 1000); // 每个一秒
    }

    // 时时刻刻记得回收
    sound->release();
    system->close();
    system->release();
    env->ReleaseStringUTFChars(path, path_);


    jclass qqClass = env->GetObjectClass(thiz);
    jmethodID playEndMethodId =env->GetMethodID(qqClass, "playEnd", "(Ljava/lang/String;)V");
    jstring content = env->NewStringUTF(content_);
    env->CallVoidMethod(thiz, playEndMethodId, content);
}


















JavaVM *jvm = nullptr;
const char *dynamicClassName = "com/baojie/jni_project/DynamicActivity";

void dynamicMethod01(){
    LOGD("我是动态注册的函数 dynamicMethod01...");
}

int dynamicMethod02(JNIEnv *env, jobject thiz, jstring valueStr) { // 也OK
    const char *text = env->GetStringUTFChars(valueStr, nullptr);
    LOGD("我是动态注册的函数 dynamicMethod02... %s", text);
    env->ReleaseStringUTFChars(valueStr, text);
    return 200;
}

static const JNINativeMethod jniNativeMethod[] = {
        {"dynamicJavaMethod01", "()V", (void *)(dynamicMethod01)},
        {"dynamicJavaMethod02", "(Ljava/lang/String;)I", (int *) (dynamicMethod02)},
};

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *javaVm, void *){
    ::jvm = javaVm;
    LOGE("System.loadLibrary ---》 JNI Load init");
    JNIEnv *jniEnv = nullptr;
    int result = javaVm->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6);
    if (result != JNI_OK){
        return -1;
    }

    jclass dynamicClass = jniEnv->FindClass(dynamicClassName);
    jniEnv->RegisterNatives(dynamicClass, jniNativeMethod, sizeof(jniNativeMethod) / sizeof(JNINativeMethod));
    LOGE("动态 注册没有毛病");
    return JNI_VERSION_1_6;
}






// JNIEnv *env 不能跨越线程，否则奔溃，  他可以跨越函数 【解决方式：使用全局的JavaVM附加当前异步线程 得到权限env操作】
// jobject thiz 不能跨越线程，否则奔溃，不能跨越函数，否则奔溃 【解决方式：默认是局部引用，提升全局引用，可解决此问题】
// JavaVM 能够跨越线程，能够跨越函数

class MyContext{
public:
    JNIEnv *jniEnv = nullptr;
    jobject instance = nullptr;
};

// 当前是异步线程
void* myThreadTaskAction(void* pVoid){
    LOGE("myThreadTaskAction run");
    // 需求：有这样的场景，例如：下载完成 ，下载失败，等等，必须告诉Activity UI端，所以需要在子线程调用UI端

    // 这两个是必须要的
    // JNIEnv *env
    // jobject thiz   OK
    MyContext *myContext = static_cast<MyContext *>(pVoid);
    // jclass mainActivityClass = myContext->jniEnv->FindClass(mainActivityClassName); // 不能跨线程 ，会奔溃
    // mainActivityClass = myContext->jniEnv->GetObjectClass(myContext->instance); // 不能跨线程 ，会奔溃

    // TODO 解决方式 （安卓进程只有一个 JavaVM，是全局的，是可以跨越线程的）
    JNIEnv *jniEnv = nullptr;
    // 附加当前异步线程后，会得到一个全新的 env，此env相当于是子线程专用env
    jint attachResult = ::jvm->AttachCurrentThread(&jniEnv, nullptr);
    if (attachResult != JNI_OK) {
        return 0; // 附加失败，返回了
    }
    jclass dynamicClass = jniEnv->GetObjectClass(myContext->instance);
    jmethodID updateUi = jniEnv->GetMethodID(dynamicClass, "updateActivityUI", "()V");
    jniEnv->CallVoidMethod(myContext->instance, updateUi);
    ::jvm->DetachCurrentThread();
    LOGE("C++ 异步线程OK");
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_DynamicActivity_nativeThread(JNIEnv *env, jobject thiz) {
    MyContext *myContext = new MyContext;
    myContext->jniEnv = env;
    // 提升全局引用
    myContext->instance = env->NewGlobalRef(thiz);

    pthread_t pid;
    pthread_create(&pid, nullptr, myThreadTaskAction, myContext);
    pthread_join(pid, nullptr);
}

int compare(const jint * num1, const jint *num2){
    return *num1 - *num2;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_DynamicActivity_sort(JNIEnv *env, jobject thiz, jintArray arr) {
    jint *intArray = env->GetIntArrayElements(arr, nullptr);
    int length = env->GetArrayLength(arr);
    /**
     * 参数1：void * 数组的首地址
     * 参数2：数组的大小长度
     * 参数3：元素的大小
     * 参数4：对比的方法指针
     */
    qsort(intArray, length, sizeof(int),
          reinterpret_cast<int (*)(const void *, const void *)>(compare));
    // 0 操纵杆 更新KT的数组
    env->ReleaseIntArrayElements(arr, intArray, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_DynamicActivity_exception(JNIEnv *env, jobject thiz) {
    jclass dynamicClass = env->GetObjectClass(thiz);
    jmethodID updateUiId = env->GetMethodID(dynamicClass, "updateActivityUI", "()V");
    // 监测本次执行，到底有没有异常   JNI函数里面代码有问题
    jthrowable thr = env->ExceptionOccurred();
    if (thr){
        // 非0 进去，监测到有异常
        LOGD("C++层有异常 监测到了");
        // 此异常被清除
        env->ExceptionClear();
    }
}

typedef struct {
    RTMP *rtmp;
    int16_t sps_len;
    int8_t *sps;
    int16_t pps_len;
    int8_t *pps;
} Live;

Live *live = nullptr;
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_baojie_jni_1project_maniu_rtmpbili_ScreenLive_connect(JNIEnv *env, jobject thiz,
                                                               jstring url_) {
    char *url = const_cast<char *>(env->GetStringUTFChars(url_, nullptr));
    //    链接   服务器   重试几次
    int ret;
    do {
        live = static_cast<Live *>(malloc(sizeof(Live)));
        memset(live, 0, sizeof(Live));
        live->rtmp = RTMP_Alloc();
        RTMP_Init(live->rtmp);
        live->rtmp->Link.timeout = 10;
        LOGD("connect %s", url);
        if (!(ret= RTMP_SetupURL(live->rtmp, url))) break;
        RTMP_EnableWrite(live->rtmp);
        LOGD("rtmp_connect");
        if (!(ret = RTMP_Connect(live->rtmp, 0))) break;
        LOGD("rtmp_connectStream");
        if (!(ret = RTMP_ConnectStream(live->rtmp, 0))) break;
        LOGD("connect success");
    } while (0);

    if (!ret && live){
        free(live);
        live = nullptr;
    }

    env->ReleaseStringUTFChars(url_, url);
    return ret;
}

//传递第一帧      00 00 00 01 67 64 00 28ACB402201E3CBCA41408681B4284D4  0000000168  EE 06 F2 C0
void prepareVideo(int8_t *data, int len, Live *live) {

    for (int i = 0; i < len; i++) {
//        防止越界
        if (i + 4 < len) {
            if (data[i] == 0x00 && data[i + 1] == 0x00
                && data[i + 2] == 0x00
                && data[i + 3] == 0x01) {
                if (data[i + 4]  == 0x68) {
                    live->sps_len = i - 4;
//                    new一个数组
                    live->sps = static_cast<int8_t *>(malloc(live->sps_len));
//                    sps解析出来了
                    memcpy(live->sps, data + 4, live->sps_len);

//                    解析pps
                    live->pps_len = len - (4 + live->sps_len) - 4;
//                    实例化PPS 的数组
                    live->pps = static_cast<int8_t *>(malloc(live->pps_len));
//                    rtmp  协议

                    memcpy(live->pps, data + 4 + live->sps_len + 4, live->pps_len);
                    LOGI("sps:%d pps:%d", live->sps_len, live->pps_len);
                    break;
                }
            }

        }
    }
}

RTMPPacket *createVideoPackage(Live *live){
    //sps  pps 的 packaet
    int body_size = 16 + live->sps_len + live->pps_len;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, body_size);
    int i = 0;
    packet->m_body[i++] = 0x17;
    //AVC sequence header 设置为0x00
    packet->m_body[i++] = 0x00;
    //CompositionTime
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    packet->m_body[i++] = 0x00;
    //AVC sequence header
    packet->m_body[i++] = 0x01;
//    原始 操作

    packet->m_body[i++] = live->sps[1]; //profile 如baseline、main、 high

    packet->m_body[i++] = live->sps[2]; //profile_compatibility 兼容性
    packet->m_body[i++] = live->sps[3]; //profile level
    packet->m_body[i++] = 0xFF;//已经给你规定好了
    packet->m_body[i++] = 0xE1; //reserved（111） + lengthSizeMinusOne（5位 sps 个数） 总是0xe1
//高八位
    packet->m_body[i++] = (live->sps_len >> 8) & 0xFF;
//    低八位
    packet->m_body[i++] = live->sps_len & 0xff;
//    拷贝sps的内容
    memcpy(&packet->m_body[i], live->sps, live->sps_len);
    i +=live->sps_len;
//    pps
    packet->m_body[i++] = 0x01; //pps number
//rtmp 协议
    //pps length
    packet->m_body[i++] = (live->pps_len >> 8) & 0xff;
    packet->m_body[i++] = live->pps_len & 0xff;
//    拷贝pps内容
    memcpy(&packet->m_body[i], live->pps, live->pps_len);
//packaet
//视频类型
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
//
    packet->m_nBodySize = body_size;
//    视频 04
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

int sendPacket(RTMPPacket *packet) {
    int r = RTMP_SendPacket(live->rtmp, packet, 1);
    RTMPPacket_Free(packet);
    free(packet);
    return r;
}

RTMPPacket *createVideoPackage(int8_t *buf, int len, const long tms, Live *live) {
    buf += 4;
//长度
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    int body_size = len + 9;
//初始化RTMP内部的body数组
    RTMPPacket_Alloc(packet, body_size);



    if (buf[0] == 0x65) {
        packet->m_body[0] = 0x17;
        LOGI("发送关键帧 data");
    } else{
        packet->m_body[0] = 0x27;
        LOGI("发送非关键帧 data");
    }
//    固定的大小
    packet->m_body[1] = 0x01;
    packet->m_body[2] = 0x00;
    packet->m_body[3] = 0x00;
    packet->m_body[4] = 0x00;

    //长度
    packet->m_body[5] = (len >> 24) & 0xff;
    packet->m_body[6] = (len >> 16) & 0xff;
    packet->m_body[7] = (len >> 8) & 0xff;
    packet->m_body[8] = (len) & 0xff;

    //数据
    memcpy(&packet->m_body[9], buf, len);
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

int sendVideo(int8_t *buf, jint len, jlong tms) {
    int ret = 0;
    if (buf[4] == 0x67){
        //缓存sps 和pps 到全局遍历 不需要推流
        if (live && (!live->pps || !live->sps)){
            //缓存 不推流
            prepareVideo(buf, len, live);
        }
        return ret;
    }
    //I帧
    if (buf[4] == 0x65){
        //         推两个
        //sps 和 ppps 的paclet  发送sps pps
        RTMPPacket *packet = createVideoPackage(live);
        sendPacket(packet);
    }
    //    两个   I帧  0x17  B P 0x27
    RTMPPacket *packet2 = createVideoPackage(buf, len, tms, live);
    ret = sendPacket(packet2);
    return ret;
}

RTMPPacket *createAudioPacket(int8_t *buf, const int len, const int type, const long tms,
                              Live *live) {

//    组装音频包  两个字节    是固定的   af    如果是第一次发  你就是 01       如果后面   00  或者是 01  aac
    int body_size = len + 2;
    RTMPPacket *packet = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet, body_size);
//         音频头
    packet->m_body[0] = 0xAF;
    if (type == 1) {
//        头
        packet->m_body[1] = 0x00;
    }else{
        packet->m_body[1] = 0x01;
    }
    memcpy(&packet->m_body[2], buf, len);
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nChannel = 0x05;
    packet->m_nBodySize = body_size;
    packet->m_nTimeStamp = tms;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = live->rtmp->m_stream_id;
    return packet;
}

int sendAudio(int8_t *buf, int len, int type, int tms) {
//    创建音频包   如何组装音频包
    RTMPPacket *packet = createAudioPacket(buf, len, type, tms, live);
    int ret=sendPacket(packet);
    return ret;
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_baojie_jni_1project_maniu_rtmpbili_ScreenLive_sendData(JNIEnv *env, jobject thiz,
                                                                jbyteArray data_, jint len,
                                                                jlong tms, jint type) {
    int ret;
    jbyte *data = env->GetByteArrayElements(data_, nullptr);
    switch (type) {
        case 0:
            ret = sendVideo(data, len, tms);
            break;
        case 1:
            ret = sendAudio(data, len, type, tms);
            break;
    };
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}


#include "safe_queue.h"
#include "VideoChannel.h"
#include "maniulog.h"
#include "JavaCallHelper.h"
VideoChannel *videoChannel = 0;
JavaCallHelper *helper = 0;
int isStart = 0;
//记录子线程的对象
pthread_t pid;
//推流标志位
int readyPushing = 0;
//阻塞式队列
SafeQueue<RTMPPacket *> packets;

uint32_t start_time;


void releasePackets(RTMPPacket *&packet) {
    if (packet) {
        RTMPPacket_Free(packet);
        delete packet;
        packet = 0;
    }
}

void *start(void *args) {
    char *url = static_cast<char *>(args);
    RTMP *rtmp = 0;
    do {
        rtmp = RTMP_Alloc();
        if (!rtmp) {
            LOGE("rtmp创建失败");
            break;
        }
        RTMP_Init(rtmp);
        //设置超时时间 5s
        rtmp->Link.timeout = 5;
        int ret = RTMP_SetupURL(rtmp, url);
        if (!ret) {
            LOGE("rtmp设置地址失败:%s", url);
            break;
        }
        //开启输出模式
        RTMP_EnableWrite(rtmp);
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            LOGE("rtmp连接地址失败:%s", url);
            break;
        }
        ret = RTMP_ConnectStream(rtmp, 0);

        LOGE("rtmp连接成功----------->:%s", url);
        if (!ret) {
            LOGE("rtmp连接流失败:%s", url);
            break;
        }

        //准备好了 可以开始推流了
        readyPushing = 1;
        //记录一个开始推流的时间
        start_time = RTMP_GetTime();
        packets.setWork(1);
        RTMPPacket *packet = 0;
        //循环从队列取包 然后发送
        while (isStart) {
            packets.pop(packet);
            if (!isStart) {
                break;
            }
            if (!packet) {
                continue;
            }
            // 给rtmp的流id
            packet->m_nInfoField2 = rtmp->m_stream_id;
            //发送包 1:加入队列发送
            ret = RTMP_SendPacket(rtmp, packet, 1);
            releasePackets(packet);
            if (!ret) {
                LOGE("发送数据失败");
                break;
            }
        }
        releasePackets(packet);
    } while (0);
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
    delete url;
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_maniu_x264_live_LivePusher_native_1init(JNIEnv *env, jobject thiz) {
    //    回调  子线程
    helper = new JavaCallHelper(jvm, env, thiz);
    videoChannel = new VideoChannel;
    videoChannel->javaCallHelper = helper;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_maniu_x264_live_LivePusher_native_1start(JNIEnv *env, jobject thiz,
                                                                      jstring path_) {
    //     链接rtmp服务器   子线程
    if (isStart){
        return;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    char *url = new char[strlen(path) + 1];
    strcpy(url, path);
//    开始直播
    isStart = 1;
//开子线程链接B站服务器
    pthread_create(&pid, 0, start, url);
    env->ReleaseStringUTFChars(path_, path);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_maniu_x264_live_LivePusher_native_1stop(JNIEnv *env, jobject thiz) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_maniu_x264_live_LivePusher_native_1pushVideo(JNIEnv *env, jobject thiz,
                                                                          jbyteArray data_) {

    //    data
    //没有链接 成功
    if (!videoChannel || !readyPushing) {
        return;
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    videoChannel->encodeData(data);
    env->ReleaseByteArrayElements(data_, data, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_baojie_jni_1project_maniu_x264_live_LivePusher_native_1setVideoEncInfo(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jint width,
                                                                                jint height,
                                                                                jint fps,
                                                                                jint bitrate) {
    if (videoChannel){
        videoChannel->setVideoEncInfo(width, height, fps, bitrate);
    }
}