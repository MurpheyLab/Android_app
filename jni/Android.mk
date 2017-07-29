LOCAL_PATH := $(call my-dir)
 
 #ALWAYS USE FORWARD SLASHES FOR PATHS
 #This part is to build the pendulum files
include $(CLEAR_VARS)
 
LOCAL_MODULE    := SAC_pendulum

LOCAL_C_INCLUDES += C:/Users/Manolis/Android_project/workspace/SACGames/jni/SAC_pendulum/libs/src#NO SPACES HERE!!
LOCAL_C_INCLUDES += C:/Users/Manolis/Android_project/workspace/SACGames/jni/SAC_pendulum/libs/user
LOCAL_C_INCLUDES += C:/Users/Manolis/Desktop/libraries/eigen-eigen-1306d75b4a21
LOCAL_C_INCLUDES += C:/Users/Manolis/Desktop/libraries/boost_1_57_0

#LOCAL_C_INCLUDES += C:/Users/Manolis/Desktop/libraries/boost_1_53_0

#LOCAL_C_INCLUDES += C:/Users/Manolis/android-ndk-r10c/toolchains/arm-linux-androideabi-4.8/prebuilt/windows-x86_64/lib/gcc/arm-linux-androideabi/4.8/include
#LOCAL_C_INCLUDES += C:/Users/Manolis/android-ndk-r10c/platforms/android-21/arch-arm/usr/include
#LOCAL_C_INCLUDES += C:/Users/Manolis/android-ndk-r10c/sources/cxx-stl/stlport/stlport
#LOCAL_C_INCLUDES += C:/Users/Manolis/android-ndk-r10c/sources/cxx-stl/gnu-libstdc++/4.7/include

#LOCAL_CFLAGS += C:/Users/Manolis/Desktop/libraries/boost_1_53_0/build/include/boost-1_53
#LOCAL_LDLIBS += C:/Users/Manolis/Desktop/libraries/boost_1_53_0/build/lib -lboost_system

#LOCAL_CPPFLAGS += -fexceptions
#LOCAL_CPPFLAGS += -frtti

LOCAL_SRC_FILES := SAC_pendulum/SAC.cpp

LOCAL_LDLIBS := -llog
 
include $(BUILD_SHARED_LIBRARY)	 # this builds libSAC_pendulum.so


#This part is to build the hopper files ---------------------------------------------
 
include $(CLEAR_VARS)

LOCAL_MODULE := SAC_hopper

LOCAL_C_INCLUDES += C:/Users/Manolis/Android_project/workspace/SACGames/jni/SAC_hopper/libs/src#NO SPACES HERE!!
LOCAL_C_INCLUDES += C:/Users/Manolis/Android_project/workspace/SACGames/jni/SAC_hopper/libs/user
LOCAL_C_INCLUDES += C:/Users/Manolis/Desktop/libraries/eigen-eigen-1306d75b4a21
LOCAL_C_INCLUDES += C:/Users/Manolis/Desktop/libraries/boost_1_57_0

LOCAL_SRC_FILES := SAC_hopper/slip.cpp

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)    # this builds libSAC_hopper.so

