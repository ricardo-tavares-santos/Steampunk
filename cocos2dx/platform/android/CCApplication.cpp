#include "jni/JniHelper.h"
#include "jni/Java_org_cocos2dx_lib_Cocos2dxHelper.h"
#include "CCApplication.h"
#include "CCDirector.h"
#include "CCEGLView.h"
#include <android/log.h>
#include <jni.h>
#include <cstring>

#define  LOG_TAG    "CCApplication_android Debug"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

NS_CC_BEGIN

// sharedApplication pointer
CCApplication * CCApplication::sm_pSharedApplication = 0;

CCApplication::CCApplication()
{
    CCAssert(! sm_pSharedApplication, "");
    sm_pSharedApplication = this;
}

CCApplication::~CCApplication()
{
    CCAssert(this == sm_pSharedApplication, "");
    sm_pSharedApplication = NULL;
}

int CCApplication::run()
{
    // Initialize instance and cocos2d.
    if (! applicationDidFinishLaunching())
    {
        return 0;
    }
    
    return -1;
}

void CCApplication::setAnimationInterval(double interval)
{
    JniMethodInfo methodInfo;
    if (! JniHelper::getStaticMethodInfo(methodInfo, "org/cocos2dx/lib/Cocos2dxRenderer", "setAnimationInterval", 
        "(D)V"))
    {
        CCLOG("%s %d: error to get methodInfo", __FILE__, __LINE__);
    }
    else
    {
        methodInfo.env->CallStaticVoidMethod(methodInfo.classID, methodInfo.methodID, interval);
    }
}

//////////////////////////////////////////////////////////////////////////
// static member function
//////////////////////////////////////////////////////////////////////////
CCApplication* CCApplication::sharedApplication()
{
    CCAssert(sm_pSharedApplication, "");
    return sm_pSharedApplication;
}

ccLanguageType CCApplication::getCurrentLanguage()
{
    std::string languageName = getCurrentLanguageJNI();
    const char* pLanguageName = languageName.c_str();
    ccLanguageType ret = kLanguageEnglish;

    if (0 == strcmp("zh", pLanguageName))
    {
        ret = kLanguageChinese;
    }
    else if (0 == strcmp("en", pLanguageName))
    {
        ret = kLanguageEnglish;
    }
    else if (0 == strcmp("fr", pLanguageName))
    {
        ret = kLanguageFrench;
    }
    else if (0 == strcmp("it", pLanguageName))
    {
        ret = kLanguageItalian;
    }
    else if (0 == strcmp("de", pLanguageName))
    {
        ret = kLanguageGerman;
    }
    else if (0 == strcmp("es", pLanguageName))
    {
        ret = kLanguageSpanish;
    }
    else if (0 == strcmp("ru", pLanguageName))
    {
        ret = kLanguageRussian;
    }
    else if (0 == strcmp("ko", pLanguageName))
    {
        ret = kLanguageKorean;
    }
    else if (0 == strcmp("ja", pLanguageName))
    {
        ret = kLanguageJapanese;
    }
    else if (0 == strcmp("hu", pLanguageName))
    {
        ret = kLanguageHungarian;
    }
    else if (0 == strcmp("pt", pLanguageName))
    {
        ret = kLanguagePortuguese;
    }
    else if (0 == strcmp("ar", pLanguageName))
    {
        ret = kLanguageArabic;
    }
    else if (0 == strcmp("ro", pLanguageName))
    {
        ret = kLanguageRomanian;
    }
    else if (0 == strcmp("pl", pLanguageName))
    {
        ret = kLanguagePolish;
    }
    else if (0 == strcmp("tr", pLanguageName))
    {
        ret = kLanguageTurkish;
    }
    else if (0 == strcmp("vi", pLanguageName))
    {
        ret = kLanguageVietnameze;
    }
    else if (0 == strcmp("hi", pLanguageName))
    {
        ret = kLanguageHindi;
    }
    else if (0 == strcmp("ms", pLanguageName))
    {
        ret = kLanguageMalay;
    }
    else if (0 == strcmp("jv", pLanguageName))
    {
        ret = kLanguageJavanese;
    }
    else if (0 == strcmp("jw", pLanguageName))
    {
        ret = kLanguageJavanese;
    }
    else if (0 == strcmp("bn", pLanguageName))
    {
        ret = kLanguageBengali;
    }
    else if (0 == strcmp("th", pLanguageName))
    {
        ret = kLanguageThai;
    }
    else if (0 == strcmp("fn", pLanguageName))
    {
        ret = kLanguagePersian;
    }
    
    return ret;
}

TargetPlatform CCApplication::getTargetPlatform()
{
    return kTargetAndroid;
}

NS_CC_END
