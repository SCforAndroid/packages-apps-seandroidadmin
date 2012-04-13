LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libjni_klogctl
LOCAL_SRC_FILES := \
		klogctl.c \
		exception.c

LOCAL_MODULE_TAGS := optional

LOCAL_SHARED_LIBRARIES := liblog

include $(BUILD_SHARED_LIBRARY)

