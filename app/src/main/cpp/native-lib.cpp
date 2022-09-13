#include <jni.h>
#include <string>

#include <android/log.h>

#define TAG "anJin"
// ... 我都不知道传入什么  借助JNI里面的宏来自动帮我填充
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

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

}