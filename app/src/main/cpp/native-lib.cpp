#include <jni.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_CVD_MainActivity_ConvertRGBtoHSV(JNIEnv *env, jobject thiz,
                                                                 jlong mat_addr_input,
                                                                 jlong mat_addr_result){

}