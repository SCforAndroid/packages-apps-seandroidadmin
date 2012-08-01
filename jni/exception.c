#include <string.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#define LOG_TAG "exception"

#include <cutils/log.h>

#include "klogctl.h"

/* Bionic does not define this*/
#ifndef ERESTARTSYS
#define ERESTARTSYS     512
#endif

static jint
throwNoClassDefError(JNIEnv *env, char *message) {

	jclass exClass;
	char *className = "java/lang/NoClassDefFoundError" ;

	exClass = (*env)->FindClass( env, className );

	if(exClass == NULL) {
            ALOGE("Could not throw java/lang/NoClassDefFoundError");
            /* If this happens, the program is out of control */
            exit(42);
        }
	return (*env)->ThrowNew( env, exClass, message );
}

static jint
throwIllegalArgumentException(JNIEnv *env, char *message) {

	jclass exClass;
	char *className = "java/lang/IllegalArgumentException" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
        return throwNoClassDefError(env, className);
	}

	return (*env)->ThrowNew(env, exClass, message);
}

static jint
throwAccessControlException(JNIEnv *env, char *message) {

	jclass exClass;
	char *className = "java/security/AccessControlException" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
        return throwNoClassDefError(env, className);
	}

	return (*env)->ThrowNew(env, exClass, message);
}

static jint
throwInterruptedException(JNIEnv *env, char *message) {

	jclass exClass;
	char *className = "java/lang/InterruptedException" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
        return throwNoClassDefError(env, className);
	}

	return (*env)->ThrowNew(env, exClass, message);
}

static jint
throwUnsupportedOperationException(JNIEnv *env, char *message) {

	jclass exClass;
	char *className = "java/lang/UnsupportedOperationException" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
        return throwNoClassDefError(env, className);
	}

	return (*env)->ThrowNew(env, exClass, message);
}

static jint
throwException(JNIEnv *env, char *message) {

	jclass exClass;
	char *className = "java/lang/Exception" ;

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
        return throwNoClassDefError(env, className);
	}

	return (*env)->ThrowNew(env, exClass, message);
}

void exception_throw(int error, JNIEnv *env) {

	char message[128];
	snprintf(message, 128, "Error reading kernel buffer : %s", strerror(error));

	switch(error) {
		case ENOSYS:
			throwUnsupportedOperationException(env, message);
			break;

		case EPERM:
			throwAccessControlException(env, message);
			break;

		case ERESTARTSYS:
			throwInterruptedException(env, message);
			break;

		case EINVAL:
			throwIllegalArgumentException(env, message);
			break;

		default:
			throwException(env, message);
			break;
	}
	return;
}
