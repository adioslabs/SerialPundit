/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge */

#ifndef _Included_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
#define _Included_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setVidPid
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setVidPid
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getVidPid
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getVidPid
  (JNIEnv *, jobject);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    createDeviceInfoList
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_createDeviceInfoList
  (JNIEnv *, jobject);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getDeviceInfoList
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDeviceInfoList
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    getDeviceInfoDetail
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_getDeviceInfoDetail
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    open
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_open
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    close
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_close
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    read
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_read
  (JNIEnv *, jobject, jlong, jbyteArray, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    write
 * Signature: (J[BI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_write
  (JNIEnv *, jobject, jlong, jbyteArray, jint);

/*
 * Class:     com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge
 * Method:    setBaudRate
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_embeddedunveiled_serial_internal_SerialComFTDID2XXJNIBridge_setBaudRate
  (JNIEnv *, jobject, jlong, jint);

#ifdef __cplusplus
}
#endif
#endif
