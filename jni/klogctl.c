#include <string.h>
#include <jni.h>
#include <errno.h>

#define LOG_TAG "klogctl"

#include <cutils/log.h>

#include "klogctl.h"
#include "exception.h"

extern int klogctl(int type, char *buf, int length);

JNIEXPORT
jint
JNICALL
Java_com_android_seandroid_1manager_KLogCtl_kLogCtl(JNIEnv *env, jclass thiz, jint type, jbyteArray buf, jint length) {

	jint error;
	jboolean is_copy;
	jbyte *bytes = (buf) ? (*env)->GetByteArrayElements(env, buf, &is_copy) : NULL;

	error = klogctl(type, (char *)bytes, length);

	if(bytes) {
		(*env)->ReleaseByteArrayElements(env, buf, bytes, 0);
	}

	if(error == -1) {
		exception_throw(errno, env);
		return 0;
	}

	return error;
}

