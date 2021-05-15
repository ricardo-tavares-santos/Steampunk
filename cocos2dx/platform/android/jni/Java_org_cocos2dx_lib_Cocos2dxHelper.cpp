#include <stdlib.h>
#include <jni.h>
#include <android/log.h>
#include <string>
#include "JniHelper.h"
#include "cocoa/CCString.h"
#include "platform/CCCommon.h"
#include "Java_org_cocos2dx_lib_Cocos2dxHelper.h"
#include "sk_game_services.h"


#define  LOG_TAG    "Java_org_cocos2dx_lib_Cocos2dxHelper.cpp"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define  CLASS_NAME "org/cocos2dx/lib/Cocos2dxHelper"

// small trick to support several native things without change CCImage interface and in order to avoid memcpy
#define protected public
#include "platform/CCImage.h"
#undef private

static EditTextCallback s_pfEditTextCallback = NULL;
static void* s_ctx = NULL;

using namespace cocos2d;
using namespace std;

string g_apkPath;

namespace sk
{
namespace game_services
{

	const char * android_get_device_model()
	{
		static string res;
		if (0 == res.length())
		{
			JniMethodInfo methodInfo;
			jstring jstr;
			if (JniHelper::getStaticMethodInfo(methodInfo, CLASS_NAME, "getDeviceModel", "()Ljava/lang/String;"))
			{
				jstr = (jstring)methodInfo.env->CallStaticObjectMethod(methodInfo.classID, methodInfo.methodID);
			}
			methodInfo.env->DeleteLocalRef(methodInfo.classID);
			res = std::string(methodInfo.env->GetStringUTFChars(jstr, NULL));
		}
		return res.c_str();
	}
	bool is_galaxy_s2()
	{
		return 0 == strcmp("GT-I9100", android_get_device_model());
	}
}
}

extern "C" {

    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_Cocos2dxHelper_nativeSetApkPath(JNIEnv*  env, jobject thiz, jstring apkPath)
	{
        g_apkPath = JniHelper::jstring2string(apkPath);
    }

    JNIEXPORT void JNICALL Java_org_cocos2dx_lib_Cocos2dxHelper_nativeSetEditTextDialogResult(JNIEnv * env, jobject obj, jbyteArray text)
	{
        jsize  size = env->GetArrayLength(text);

        if (size > 0) {
            jbyte * data = (jbyte*)env->GetByteArrayElements(text, 0);
            char* pBuf = (char*)malloc(size+1);
            if (pBuf != NULL) {
                memcpy(pBuf, data, size);
                pBuf[size] = '\0';
                // pass data to edittext's delegate
                if (s_pfEditTextCallback) s_pfEditTextCallback(pBuf, s_ctx);
                free(pBuf);
            }
            env->ReleaseByteArrayElements(text, data, 0);
        } else {
            if (s_pfEditTextCallback) s_pfEditTextCallback("", s_ctx);
        }
    }

}

extern "C"
{
	// NOT used at the moment, but kept for profit and reference!!!
    void Java_org_cocos2dx_lib_Cocos2dxActivity_nativeOnChooseImageFromLibrary(JNIEnv*  env, jobject thiz, jstring imagePath)
    {
		std::string path = JniHelper::jstring2string(imagePath);
		if (!path.length())
		{
			LOGD("if (!path.length())");
			sk::game_services::on_take_image(0);
		}
		else
		{
			CCImage * res = new CCImage();
			LOGD("path is %s", path.c_str());
			if (!res->initWithImageFileThreadSafe(path.c_str(), cocos2d::CCImage::kFmtUnKnown))
			{
				LOGD("delete res");
				delete res;
				res = 0;
			}
			sk::game_services::on_take_image(res);
		}
    }

    void Java_org_cocos2dx_lib_Cocos2dxActivity_nativeOnTakePhoto(JNIEnv*  env, jobject thiz, int width, int height, jbyteArray pixels)
    {
		if (width < 0 || height < 0)
		{
			sk::game_services::on_take_image(0);
		}
		else
		{
			CCImage * res = new CCImage();
			int size = width * height * 4;
			res->m_nWidth = width;
			res->m_nHeight = height;
			res->m_pData = new unsigned char[size];
			res->m_nBitsPerComponent = 8;
			res->m_bHasAlpha = true;
			res->m_bPreMulti = true;
	        env->GetByteArrayRegion(pixels, 0, size, (jbyte*)res->m_pData);
			sk::game_services::on_take_image(res);
		}
    }

    void Java_org_cocos2dx_lib_Cocos2dxHelper_nativeSetYesNoDialogResult(JNIEnv * env, jobject thiz, int result)
	{
		sk::game_services::set_yes_no_dialog_result(0 != result);
    }

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_cbAppId(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_cb_app_id();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_cbSignature(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_cb_app_signature();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_tapjoyAppId(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::tapjoy_app_id();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_tapjoyAppKey(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::tapjoy_app_key();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_developerTitle(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::android_store_developer_title();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_tapjoyCurrencyId(JNIEnv * env, jobject thiz, int type)
	{
		return env->NewStringUTF("we do not use it right now");
		//const char * pszText = sk::game_services::tapjoy_currency_id(type);
		//return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_adcolonyAppId(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_adcolony_app_id();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_adcolonyZoneId(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_adcolony_zone();
		return env->NewStringUTF(pszText);
	}

    jstring Java_org_cocos2dx_lib_Cocos2dxActivity_sponsorpayAppId(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::sponsorpay_app_id();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_sponsorpayAppKey(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::sponsorpay_app_key();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_mediationId(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_admob_mediation_id();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_flurryApiKey(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_flurry_app_key();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_flurryAdKey(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::get_flurry_ad_key();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_InAppKey(JNIEnv * env, jobject thiz)
	{
		const char * pszText = sk::game_services::android_base64_encoded_public_key();
		return env->NewStringUTF(pszText);
	}

	jstring Java_org_cocos2dx_lib_Cocos2dxActivity_getInAppStoreId(JNIEnv * env, jobject thiz, int index)
	{
		const char * pszText = sk::game_services::get_inapp_store_id(index);
		return env->NewStringUTF(pszText);
	}

	int Java_org_cocos2dx_lib_Cocos2dxActivity_getTotalInApps(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::get_total_inapps();
	}

	int Java_org_cocos2dx_lib_Cocos2dxActivity_getPurchaseMode(JNIEnv * env, jobject thiz)
	{
		return (int)sk::game_services::get_android_purchase_mode();
	}

	int Java_org_cocos2dx_lib_Cocos2dxActivity_getBannerHeight(JNIEnv * env, jobject thiz)
	{
		int res = sk::game_services::get_banner_height();
		return res;
	}

    int Java_org_cocos2dx_lib_Cocos2dxActivity_getOfferWallProvider(JNIEnv * env, jobject thiz)
	{
		int res = sk::game_services::get_offer_wall_provider();
		return res;
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_reportInApp(JNIEnv * env, jobject thiz, jstring st_id)
	{
		std::string store_id = JniHelper::jstring2string(st_id);
		sk::game_services::on_inapp_confirmed_by_store(store_id.c_str());
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onPurchase(JNIEnv * env, jobject thiz, jstring st_id, int result)
	{
		std::string store_id = JniHelper::jstring2string(st_id);
		sk::game_services::on_purchase(store_id.c_str(), result != 0);
	}

	jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isInAppConsumable(JNIEnv * env, jobject thiz, jstring sku)
	{
		std::string store_id = JniHelper::jstring2string(sku);
		return sk::game_services::is_inapp_consumable(store_id.c_str()) ? JNI_TRUE : JNI_FALSE;
	}



	void Java_org_cocos2dx_lib_Cocos2dxActivity_onGameCenterAuth(JNIEnv * env, jobject thiz)
	{
		sk::game_services::on_gamecenter_auth();
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onOfferWallClose(JNIEnv * env, jobject thiz)
	{
		sk::game_services::on_offer_wall_closed();
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onOfferWallEarn(JNIEnv * env, jobject thiz, int amount)
	{
		sk::game_services::on_offer_earned(amount);
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onGameCenterAuthFailed(JNIEnv * env, jobject thiz)
	{
		sk::game_services::on_gamecenter_auth_failed();
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_setAdViewHeight(JNIEnv * env, jobject thiz, int height)
	{
		sk::game_services::on_android_adview_height_changed(height);
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onAchievementReported(JNIEnv * env, jobject thiz, jstring st_id, int result)
	{
		std::string store_id = JniHelper::jstring2string(st_id);
		sk::game_services::on_achievement_reported(store_id.c_str(), float(result));
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onScoreSubmitted(JNIEnv * env, jobject thiz, jstring st_id, int result)
	{
		std::string store_id = JniHelper::jstring2string(st_id);
		sk::game_services::on_leaderboard_score_submitted(store_id.c_str(), result);
	}

	 jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isAdmobInTestMode(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::is_admob_in_test_mode() ? JNI_TRUE : JNI_FALSE;
	}

	 jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isAdsRemoved(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::is_ads_removed() ? JNI_TRUE : JNI_FALSE;
	}

	jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isBannerAdsDisabled(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::disable_banner_ads() ? JNI_TRUE : JNI_FALSE;
	}

	jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isGameCenterDisabled(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::is_game_center_disabled() ? JNI_TRUE : JNI_FALSE;
	}

	jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isOfferWallEnabled(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::is_offer_wall_enabled() ? JNI_TRUE : JNI_FALSE;
	}

	jboolean Java_org_cocos2dx_lib_Cocos2dxActivity_isVideoAdEnabled(JNIEnv * env, jobject thiz)
	{
		return sk::game_services::is_video_ad_enabled() ? JNI_TRUE : JNI_FALSE;
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onVideoAdAward(JNIEnv * env, jobject thiz, int amount)
	{
		sk::game_services::on_video_ad_award(amount);
	}

	void Java_org_cocos2dx_lib_Cocos2dxActivity_onFacebookCheckPageLikeCallback(JNIEnv * env, jobject thiz, bool isLiked)
	{
		sk::game_services::facebook_check_page_like_callback(isLiked);
	}
};

void android_gc_submit_score(const char * cat, unsigned int value)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "submitScore", "(Ljava/lang/String;I)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(cat);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, value);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_report_achievement(const char * identifier, float percent, bool show_banner)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "reportAchievement", "(Ljava/lang/String;I)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(identifier);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, int(percent));
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
}

bool android_device_can_take_photo()
{
    JniMethodInfo t;
    int ret = 0;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "isCameraSupported", "()I"))
 	{
        ret = t.env->CallStaticIntMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
    }
	return 0 != ret;
}

bool android_take_photo()
{
	if (!android_device_can_take_photo())
	{
		return false;
	}
	JniMethodInfo t;
    int ret = 0;
    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "takePhoto", "(III)I"))
 	{
		sk::game_services::image_info info = sk::game_services::get_image_info();
        ret = (int)t.env->CallStaticIntMethod(t.classID, t.methodID, info.width, info.height, info.source);
        t.env->DeleteLocalRef(t.classID);
    }
	return 0 != ret;
}

void android_start_yes_no_dialog(const std::string& message, const std::string& title, const std::string& yes_text, const std::string& no_text)
{
	if (!message.length())
	{
		return;
	}
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "showDialogYesNo", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(message.c_str());
		jstring stringArg2 = t.env->NewStringUTF(title.c_str());
		jstring stringArg3 = t.env->NewStringUTF(yes_text.c_str());
		jstring stringArg4 = t.env->NewStringUTF(no_text.c_str());

		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2, stringArg3, stringArg4);

		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(stringArg2);
		t.env->DeleteLocalRef(stringArg3);
		t.env->DeleteLocalRef(stringArg4);

		t.env->DeleteLocalRef(t.classID);
	}
}

// static public void facebookPostWall(final String message, final String url, final String title, final String subtitle, final String description, final String pictureUrl)
void android_facebook_post_wall(const char * message, const char * url, const char * title, const char * subtitle, const char * description, const char * pictureUrl)
{
	JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "facebookPostWall", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(message ? message : "");
        jstring stringArg2 = t.env->NewStringUTF(url ? url : "");
        jstring stringArg3 = t.env->NewStringUTF(title ? title : "");
		jstring stringArg4 = t.env->NewStringUTF(subtitle ? subtitle : "");
        jstring stringArg5 = t.env->NewStringUTF(description ? description : "");
        jstring stringArg6 = t.env->NewStringUTF(pictureUrl ? pictureUrl : "");

        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2, stringArg3, stringArg4, stringArg5, stringArg6);

        t.env->DeleteLocalRef(stringArg1);
        t.env->DeleteLocalRef(stringArg2);
        t.env->DeleteLocalRef(stringArg3);
        t.env->DeleteLocalRef(stringArg4);
        t.env->DeleteLocalRef(stringArg5);
        t.env->DeleteLocalRef(stringArg6);

        t.env->DeleteLocalRef(t.classID);
    }
}

void android_share_facebook_score(const char * message)
{
	android_facebook_post_wall(message,
	                           sk::game_services::get_app_store_url(),
	                           sk::game_services::game_title(),
	                           sk::game_services::android_share_score_subtitle(),
	                           sk::game_services::android_share_score_description(),
	                           sk::game_services::android_share_score_picture_url());
}

bool android_share_image_facebook(cocos2d::CCImage * image, const char * message)
{
	JniMethodInfo t;
	std::string ret;

	if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getSaveImagePath", "(Ljava/lang/String;)Ljava/lang/String;"))
	{
		jstring stringArg1 = t.env->NewStringUTF(sk::game_services::game_title());
		jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID, stringArg1);
		ret = JniHelper::jstring2string(str);
		t.env->DeleteLocalRef(str);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}

	if (!ret.length())
	{
		return false;
	}
	else
	{
		if (image->saveToFile(ret.c_str(), true))
		{
			if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "facebookPostImage", "(Ljava/lang/String;Ljava/lang/String;)V"))
			{
				jstring stringArg1 = t.env->NewStringUTF(ret.c_str());
				jstring stringArg2 = t.env->NewStringUTF(message);
				t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2);
				t.env->DeleteLocalRef(stringArg1);
				t.env->DeleteLocalRef(stringArg2);
				t.env->DeleteLocalRef(t.classID);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
}

void android_facebook_open_page(const char* page_id)
{
	JniMethodInfo t;
	int res = 0;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "facebookOpenPage", "(Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(page_id);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
	else
	{
		cocos2d::CCLog("can not find java method: facebookOpenPage");
	}
}

void android_facebook_check_page_like(const char* page_id)
{
	JniMethodInfo t;
	int res = 0;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "facebookCheckPageLike", "(Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(page_id);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
	else
	{
		cocos2d::CCLog("can not find java method: android_facebook_check_page_like");
	}
}


//	public static void sendEmail(String to, String subject, String message, String photoPath)
void android_share_email(const sk::game_services::email_info& info)
{
	JniMethodInfo t;
	std::string ret;

	if (info.photo)
	{
		if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getSaveImagePath", "(Ljava/lang/String;)Ljava/lang/String;"))
		{
			jstring stringArg1 = t.env->NewStringUTF(sk::game_services::game_title());
			jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID, stringArg1);
			ret = JniHelper::jstring2string(str);
			t.env->DeleteLocalRef(str);
			t.env->DeleteLocalRef(stringArg1);
			t.env->DeleteLocalRef(t.classID);
		}
		if (ret.length())
		{
			if (!info.photo->saveToFile(ret.c_str(), true))
			{
				ret.clear();
			}
			else
			{
			}
		}
	}

	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "sendEmail", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(info.to.length()      ? info.to.c_str()      : "");
		jstring stringArg2 = t.env->NewStringUTF(info.subject.length() ? info.subject.c_str() : "");
		jstring stringArg3 = t.env->NewStringUTF(info.message.length() ? info.message.c_str() : "");
		jstring stringArg4 = t.env->NewStringUTF(ret.c_str());
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2, stringArg3, stringArg4);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(stringArg2);
		t.env->DeleteLocalRef(stringArg3);
		t.env->DeleteLocalRef(stringArg4);
		t.env->DeleteLocalRef(t.classID);
	}
}

bool android_can_send_email()
{
	return true;
}

bool android_save_image_to_photos(cocos2d::CCImage * image)
{
	JniMethodInfo t;
	std::string ret;

	if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getSaveImagePath", "(Ljava/lang/String;)Ljava/lang/String;"))
	{
		jstring stringArg1 = t.env->NewStringUTF(sk::game_services::game_title());
		jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID, stringArg1);
		ret = JniHelper::jstring2string(str);
		t.env->DeleteLocalRef(str);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}

	if (!ret.length())
	{
		return false;
	}
	else
	{
		if (image->saveToFile(ret.c_str()), true)
		{
			if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "informGalleryDBthatImageInserted", "(Ljava/lang/String;)V"))
			{
				jstring stringArg1 = t.env->NewStringUTF(ret.c_str());
				t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
				t.env->DeleteLocalRef(stringArg1);
				t.env->DeleteLocalRef(t.classID);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
}

void android_open_url(const char * url)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "openUrl", "(Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(url);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
}

bool android_gc_show_leaderboard(const char * leaderboard)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "showLeaderboard", "(Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(leaderboard);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
		return true;
	}
	else
	{
		return false;
	}
}

void android_log_event(const char * event)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "logEvent", "(Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(event);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
}

bool android_gc_show_achievements()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "showAchievements", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
		return true;
	}
	return false;
}

bool android_open_offer_wall()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "openOfferWall", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
		return true;
	}
	return false;
}

void android_show_cb()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "showChartboost", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_show_video_ad(bool reward, bool show_fail_dialog)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", reward ? "showAdColonyReward" : "showAdColonyNoReward", "(Z)V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID, show_fail_dialog);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_show_more_games()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "showMoreGames", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_show_ads(bool isVisible)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "showAds", "(Z)V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID, isVisible);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_on_rate_me()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "rateMe", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
	}
}



void android_share_twitter (const char * message)
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "postTwitter", "(Ljava/lang/String;Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(message);
		jstring stringArg2 = t.env->NewStringUTF(sk::game_services::get_app_store_url());
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(stringArg2);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_gameservices_init()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "initGameServices", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
	}
}

void android_inapps_init()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "initInApps", "(Ljava/lang/String;)V"))
	{
		jstring stringArg1 = t.env->NewStringUTF(sk::game_services::android_base64_encoded_public_key());
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
}

bool android_buy_product(const char * store_id)
{
	JniMethodInfo t;
	int res = 0;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "buyProduct", "(Ljava/lang/String;)I"))
	{
		jstring stringArg1 = t.env->NewStringUTF(store_id);
		res = t.env->CallStaticIntMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
	return res != 0;
}

bool android_kill_product(const char * store_id)
{
	JniMethodInfo t;
	int res = 0;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "killProduct", "(Ljava/lang/String;)I"))
	{
		jstring stringArg1 = t.env->NewStringUTF(store_id);
		res = t.env->CallStaticIntMethod(t.classID, t.methodID, stringArg1);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(t.classID);
	}
	return res != 0;
}

void android_restore_purchases()
{
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "restorePurchases", "()V"))
	{
		t.env->CallStaticVoidMethod(t.classID, t.methodID);
		t.env->DeleteLocalRef(t.classID);
	}
}

std::string android_get_gs_player_id()
{
	JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "getGameservicesPlayerId", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);
        ret = JniHelper::jstring2string(str);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(str);
    }

    return ret;
}

bool android_is_gs_authenticated()
{
	JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, "org/cocos2dx/lib/Cocos2dxActivity", "isGameservicesAuthenticated", "()Z")) {
        jboolean ret = t.env->CallStaticBooleanMethod(t.classID, t.methodID);

        t.env->DeleteLocalRef(t.classID);

        return ret;
    }

    return false;
}

const char * getApkPath()
{
	return g_apkPath.c_str();
}

void showDialogJNI(const char * pszMsg, const char * pszTitle)
{
	if (!pszMsg)
	{
		return;
	}
	JniMethodInfo t;
	if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "showDialog", "(Ljava/lang/String;Ljava/lang/String;)V"))
	{
		jstring stringArg1;
		if (!pszTitle)
		{
			stringArg1 = t.env->NewStringUTF("");
		}
		else
		{
			stringArg1 = t.env->NewStringUTF(pszTitle);
		}
		jstring stringArg2 = t.env->NewStringUTF(pszMsg);
		t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2);
		t.env->DeleteLocalRef(stringArg1);
		t.env->DeleteLocalRef(stringArg2);
		t.env->DeleteLocalRef(t.classID);
	}
}

void showEditTextDialogJNI(const char* pszTitle, const char* pszMessage, int nInputMode, int nInputFlag, int nReturnType, int nMaxLength, EditTextCallback pfEditTextCallback, void* ctx) {
    if (pszMessage == NULL) {
        return;
    }

    s_pfEditTextCallback = pfEditTextCallback;
    s_ctx = ctx;

    JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "showEditTextDialog", "(Ljava/lang/String;Ljava/lang/String;IIII)V")) {
        jstring stringArg1;

        if (!pszTitle) {
            stringArg1 = t.env->NewStringUTF("");
        } else {
            stringArg1 = t.env->NewStringUTF(pszTitle);
        }

        jstring stringArg2 = t.env->NewStringUTF(pszMessage);

        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2, nInputMode, nInputFlag, nReturnType, nMaxLength);

        t.env->DeleteLocalRef(stringArg1);
        t.env->DeleteLocalRef(stringArg2);
        t.env->DeleteLocalRef(t.classID);
    }
}

void terminateProcessJNI() {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "terminateProcess", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
    }
}

std::string getPackageNameJNI() {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getCocos2dxPackageName", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
        ret = JniHelper::jstring2string(str);
        t.env->DeleteLocalRef(str);
    }
    return ret;
}

std::string getFileDirectoryJNI() {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getCocos2dxWritablePath", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
        ret = JniHelper::jstring2string(str);
        t.env->DeleteLocalRef(str);
    }

    return ret;
}

std::string getCurrentLanguageJNI() {
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getCurrentLanguage", "()Ljava/lang/String;")) {
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
        ret = JniHelper::jstring2string(str);
        t.env->DeleteLocalRef(str);
    }

    return ret;
}

void enableAccelerometerJNI() {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "enableAccelerometer", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
    }
}

void setAccelerometerIntervalJNI(float interval) {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "setAccelerometerInterval", "(F)V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID, interval);
        t.env->DeleteLocalRef(t.classID);
    }
}

void disableAccelerometerJNI() {
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "disableAccelerometer", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
    }
}

// functions for CCUserDefault
bool getBoolForKeyJNI(const char* pKey, bool defaultValue)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getBoolForKey", "(Ljava/lang/String;Z)Z")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        jboolean ret = t.env->CallStaticBooleanMethod(t.classID, t.methodID, stringArg, defaultValue);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);

        return ret;
    }

    return defaultValue;
}

int getIntegerForKeyJNI(const char* pKey, int defaultValue)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getIntegerForKey", "(Ljava/lang/String;I)I")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        jint ret = t.env->CallStaticIntMethod(t.classID, t.methodID, stringArg, defaultValue);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);

        return ret;
    }

    return defaultValue;
}

float getFloatForKeyJNI(const char* pKey, float defaultValue)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getFloatForKey", "(Ljava/lang/String;F)F")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        jfloat ret = t.env->CallStaticFloatMethod(t.classID, t.methodID, stringArg, defaultValue);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);

        return ret;
    }

    return defaultValue;
}

double getDoubleForKeyJNI(const char* pKey, double defaultValue)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getDoubleForKey", "(Ljava/lang/String;D)D")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        jdouble ret = t.env->CallStaticDoubleMethod(t.classID, t.methodID, stringArg);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);

        return ret;
    }

    return defaultValue;
}

std::string getStringForKeyJNI(const char* pKey, const char* defaultValue)
{
    JniMethodInfo t;
    std::string ret("");

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "getStringForKey", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) {
        jstring stringArg1 = t.env->NewStringUTF(pKey);
        jstring stringArg2 = t.env->NewStringUTF(defaultValue);
        jstring str = (jstring)t.env->CallStaticObjectMethod(t.classID, t.methodID, stringArg1, stringArg2);
        ret = JniHelper::jstring2string(str);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg1);
        t.env->DeleteLocalRef(stringArg2);
        t.env->DeleteLocalRef(str);

        return ret;
    }

    return defaultValue;
}

void setBoolForKeyJNI(const char* pKey, bool value)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "setBoolForKey", "(Ljava/lang/String;Z)V")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg, value);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);
    }
}

void setIntegerForKeyJNI(const char* pKey, int value)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "setIntegerForKey", "(Ljava/lang/String;I)V")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg, value);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);
    }
}

void setFloatForKeyJNI(const char* pKey, float value)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "setFloatForKey", "(Ljava/lang/String;F)V")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg, value);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);
    }
}

void setDoubleForKeyJNI(const char* pKey, double value)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "setDoubleForKey", "(Ljava/lang/String;D)V")) {
        jstring stringArg = t.env->NewStringUTF(pKey);
        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg, value);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg);
    }
}

void setStringForKeyJNI(const char* pKey, const char* value)
{
    JniMethodInfo t;

    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "setStringForKey", "(Ljava/lang/String;Ljava/lang/String;)V")) {
        jstring stringArg1 = t.env->NewStringUTF(pKey);
        jstring stringArg2 = t.env->NewStringUTF(value);
        t.env->CallStaticVoidMethod(t.classID, t.methodID, stringArg1, stringArg2);

        t.env->DeleteLocalRef(t.classID);
        t.env->DeleteLocalRef(stringArg1);
        t.env->DeleteLocalRef(stringArg2);
    }
}
