/****************************************************************************
Copyright (c) 2010-2013 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ****************************************************************************/
package org.cocos2dx.lib;

import android.view.View;
import org.cocos2dx.amazon.AmazonAdClient;
import org.cocos2dx.amazon.gamecircle.AmazonGameCircleClient;
import org.cocos2dx.amazon.inapp.AmazonInAppClient;
import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.View;
import android.util.Log;
import android.widget.FrameLayout;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.facebook.*;
import com.facebook.model.*;
import org.cocos2dx.facebook.FacebookCustomClient;
import org.cocos2dx.plugins.TwitterClient;
import org.json.*;
import android.net.Uri;
import android.database.Cursor;
import java.io.*;
import com.example.android.trivialdrivesample.util.*;
import java.util.ArrayList;
import com.chartboost.sdk.*;
import com.google.ads.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.*;
import com.google.android.gms.games.achievement.*;
import com.tapjoy.*;
import com.sponsorpay.sdk.android.SponsorPay;
import com.sponsorpay.sdk.android.publisher.*;
import com.sponsorpay.sdk.android.publisher.currency.*;
import com.sponsorpay.sdk.android.utils.*;
import java.util.Hashtable;
import com.jirbo.adcolony.*;
import com.flurry.android.FlurryAgent;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdService;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;

public abstract class Cocos2dxActivity extends Activity implements Cocos2dxHelperListener,
 GameHelper.GameHelperListener, OnScoreSubmittedListener,
OnAchievementUpdatedListener, TapjoyEarnedPointsNotifier, TapjoyNotifier, TapjoyFullScreenAdNotifier, TapjoyAwardPointsNotifier, ChartboostDelegate, SPCurrencyServerListener,
AdColonyAdListener, AdColonyV4VCListener, AdColonyAdAvailabilityListener, AppLovinAdLoadListener
{
    private static final int SK_OFFER_WALL_PROVIDER_TAPJOY     = 0;
    private static final int SK_OFFER_WALL_PROVIDER_SPONSORPAY = 1;
	private static final int SK_IMAGE_SOURCE_UNDEFINED      = 0;
	private static final int SK_IMAGE_SOURCE_CAMERA         = 1;
	private static final int SK_IMAGE_SOURCE_PHOTO_LIBRARY  = 2;
	private static final int SK_PURCHASE_REQUEST = 23232323;
	private static final String TEST_PURCHASED      = "android.test.purchased";
	private static final String TEST_CANCELED       = "android.test.canceled";
	private static final String TEST_ITEM_UNAVALBLE = "android.test.item_unavailable";
	private static final String TEST_REFUNDED       = "android.test.refunded";
	private static String TEST_PURCHASE_SKU = TEST_PURCHASED;
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions,publish_stream");
	private static final String TAG = Cocos2dxActivity.class.getSimpleName();
	private static final int TAKE_IMAGE_FROM_GALLERY = 26;
	private static final int TAKE_PHOTO = 23;
	private static final int SEND_EMAIL = 2323;
	private static final int POST_TWITTER = 2727;
    private static int offerWallProvider = -1;
	private int photo_width = -1;
	private int photo_height = -1;
	private int photo_source = SK_IMAGE_SOURCE_UNDEFINED;
	private boolean report_camera = false;
	private Bitmap report_bitmap = null;
	private Chartboost cb = null;
    private boolean _isOnDestroyCalled = false;
    //private SPCurrencyServerListener spRequestListener;

	private Cocos2dxGLSurfaceView mGLSurfaceView;
	public Cocos2dxHandler mHandler;
	private static Cocos2dxActivity sContext = null;
	private GraphUser user = null;
	private static String emailPhotoPath = null;
	private static IabHelper mHelper = null;
    protected TwitterClient _twitterClient = null;
    private FacebookCustomClient _facebookCustomClient;
	private AdColonyV4VCAd v4vc_ad;
	private static native boolean isAdmobInTestMode();
	private static native boolean isAdsRemoved();
	private static native boolean isBannerAdsDisabled();
	private static native boolean isGameCenterDisabled();
	private static native String cbAppId();
	private static native String cbSignature();
	public static native String mediationId();
	private static native String getInAppStoreId(int index);
	private static native boolean isInAppConsumable(String sku);
	private static native int    getPurchaseMode();
	private static native int    getTotalInApps();
	private static native int    getBannerHeight();
    private static native int    getOfferWallProvider();
	public static native void   reportInApp(String sku);
	private static native void   setAdViewHeight(int heigth);
	private static native void   onOfferWallEarn(int amount);
	public static native void   onPurchase(String sku, int result);
	public static native void   onAchievementReported(String st_id, int result);
    public static native void   onScoreSubmitted(String st_id, int result);
    public static native void   onGameCenterAuth();
    public static native void   onGameCenterAuthFailed();
	private static native void   onOfferWallClose();
	private static boolean inappsok = false;
	private static native String tapjoyAppId();
	private static native String tapjoyAppKey();
	private static native String tapjoyCurrencyId(int type);
	private static native String adcolonyAppId();
	private static native String adcolonyZoneId();
    private static native String sponsorpayAppId();
	private static native String sponsorpayAppKey();
	private static native String flurryApiKey();
	private static native String InAppKey();
	private static native String developerTitle();
	private static native boolean isOfferWallEnabled();
	private static native boolean isVideoAdEnabled();
	private static native void    onVideoAdAward(int amount);
    public static native void onFacebookCheckPageLikeCallback(boolean isLiked);

	// The game helper object. This class is mainly a wrapper around this object.
    protected GameHelper gameHelper = null;

    // We expose these constants here because we don't want users of this class
    // to have to know about GameHelper at all.
    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
    public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
    public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;
	private final static int REQUEST_ACHIEVEMENTS = 9003;
	private final static int REQUEST_LEADERBOARD = 9004;

    // Requested clients. By default, that's just the games client.
    protected int mRequestedClients = CLIENT_GAMES;

    //amazon inapp
    protected boolean _isAmazonApp = false;
    private AmazonInAppClient _amazonInAppClient;
    private AmazonInAppClient GetAmazonInAppClient() {
        if (_isAmazonApp && _amazonInAppClient == null) {
            _amazonInAppClient = new AmazonInAppClient(sContext);
        }
        return _amazonInAppClient;
    }

    private AmazonGameCircleClient _amazonGameCircleClient;
    private AmazonGameCircleClient GetAmazonGameCircleClient() {
        if (_isAmazonApp && _amazonGameCircleClient == null) {
            _amazonGameCircleClient = new AmazonGameCircleClient(sContext);
        }
        return _amazonGameCircleClient;
    }

    private AmazonAdClient _amazonAdClient;
    private AmazonAdClient GetAmazonAdClient() {
        if (_isAmazonApp && _amazonAdClient == null) {
            _amazonAdClient = new AmazonAdClient(sContext);
        }
        return _amazonAdClient;
    }


	private static IabHelper.OnIabSetupFinishedListener mOnSetup = new IabHelper.OnIabSetupFinishedListener()
	{
		public void onIabSetupFinished(IabResult result)
		{
			if (!result.isSuccess())
			{
				return;
			}
			inappsok = true;
            ArrayList<String> skus = GetInAppSkus();
			try
			{
				mHelper.queryInventoryAsync(true, skus, mGotInventoryListener);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	};
    private static IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
	{
		public void onQueryInventoryFinished(IabResult result, Inventory inventory)
		{
			if (result.isFailure())
			{
				return;
			}
			List<String> skus = inventory.getAllSkus();
			int n = skus.size();
			for (int i = 0; i < n; ++i)
			{
				reportInApp(skus.get(i));
			}

			List<Purchase> lp = inventory.getAllPurchases();
			List<Purchase> llp = new ArrayList();
			for (Purchase pp : lp)
			{
				if (isInAppConsumable(pp.getSku()))
				{
					llp.add(pp);
				}
			}
			try
			{
				if (llp.size() > 0)
				{
					mHelper.consumeAsync(llp, null);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	};

	private static IabHelper.QueryInventoryFinishedListener mRestorePurchaseListener = new IabHelper.QueryInventoryFinishedListener()
	{
		public void onQueryInventoryFinished(IabResult result, Inventory inventory)
		{
			if (result.isFailure())
			{
				return;
			}
			List<String> skus = inventory.getAllOwnedSkus(IabHelper.ITEM_TYPE_INAPP);
			int n = skus.size();
			//Log.d(TAG, "restore purchase size " + n);
			for (int i = 0; i < n; ++i)
			{
				final String sku = skus.get(i);
				Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
				{
					@Override
					public void run()
					{
						onPurchase(sku, 1);
					}
				});
			}
		}
	};

	private static IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener()
	{
		public void onConsumeFinished(Purchase purchase, IabResult result)
		{
			String sku = purchase.getSku();
			if (null == sku)
			{
				sku = "unknown";
			}
			if (result.isSuccess())
			{
				Log.e(TAG, "purchase consumed " + sku);
			}
			else
			{
				Log.e(TAG, "purchase NOT consumed " + sku);
			}
		}
	};

	private static IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
	{
		public void onIabPurchaseFinished(IabResult result, Purchase purchase)
		{
			String sku = null;
			if (null != purchase)
			{
				sku = purchase.getSku();
				if (null != sku && sku.equals(TEST_PURCHASE_SKU))
				{
					if (null != lastSku)
					{
						sku = lastSku;
					}
				}
			}
			if (null == sku)
			{
				sku = result.getSku();
				if (null != sku && sku.equals(TEST_PURCHASE_SKU))
				{
					if (null != lastSku)
					{
						sku = lastSku;
					}
				}
			}
			if (null == sku)
			{
				sku = lastSku;
			}
			if (null != sku)
			{
				final String ssku = sku;
				final int purchased = result.isFailure() ? 0 : 1;
				Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
				{
					@Override
					public void run()
					{
						onPurchase(ssku, purchased);
					}
				});

				final Purchase p = purchase;
				if (!result.isFailure() && isInAppConsumable(sku))
				{
					((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
					{
					    @Override
						public void run()
						{
							((Cocos2dxActivity)sContext).mHelper.flagEndAsync();
							((Cocos2dxActivity)sContext).mHelper.consumeAsync(p, mConsumeFinishedListener);
						}
					});
				}
			}
			else
			{
				Log.e(TAG, "Unknown sku for onIabPurchaseFinished. We must never get here, looks like something changed in google billing");
			}
			lastSku = null;
		}
	};

    private static boolean IsAmazonDevice() {
        String deviceCompany = android.os.Build.MANUFACTURER;
        Log.i(TAG, "Device = " + deviceCompany);
        if (deviceCompany.toLowerCase().equals("amazon"))
            return true;
        return false;

        //(android.os.Build.MODEL.equals("Kindle Fire") || android.os.Build.MODEL.startsWith("KF"));
    }

	private static void initInApps(final String key)
	{
        if (sContext._isAmazonApp/* && IsAmazonDevice()*/) {
            sContext.runOnUiThread(new Runnable() { @Override public void run() {
                sContext.GetAmazonInAppClient().Init();
            }});
        }
	}

    private static void initGameServices()
    {
        if (sContext._isAmazonApp) {
            sContext.runOnUiThread(new Runnable() { @Override public void run() {
                sContext.GetAmazonGameCircleClient().Init();
            }});
        }
        else 
        {
        	// this check will fail in first 3 seconds after app start because gameHelper creation is postponded
        	if (null == ((Cocos2dxActivity)sContext).gameHelper)
        	{
            	return;
        	}

			if (isGameCenterDisabled())
			{
				return;
			}

			((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
			{
		    	@Override
				public void run()
				{
					if (!((Cocos2dxActivity)sContext).isSignedIn())
					{
						((Cocos2dxActivity)sContext).gameHelper.beginUserInitiatedSignIn();
					}
				}
			});
        }
    }

	private static void restorePurchases()
	{
        if (sContext._isAmazonApp/* && IsAmazonDevice()*/) {
            sContext.GetAmazonInAppClient().RestorePurchases();
            return;
        }

		if (null == mHelper)
		{
			return;
		}
		if (!inappsok)
		{
			try
			{
				mHelper.startSetup(mOnSetup);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		try
		{
			ArrayList<String> skus = new ArrayList<String>();
			int n = getTotalInApps();
			for (int i = 0; i < n; ++i)
			{
				skus.add(getInAppStoreId(i));
			}
			final ArrayList<String> sskus = skus;
			((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
			{
			    @Override
				public void run()
				{
					((Cocos2dxActivity)sContext).mHelper.flagEndAsync();
					((Cocos2dxActivity)sContext).mHelper.queryInventoryAsync(true, sskus, mRestorePurchaseListener);
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String lastSku;
	private static int buyProduct(String sku)
	{
		if (sContext._isAmazonApp/* && IsAmazonDevice()*/)
		{
			sContext.GetAmazonInAppClient().Buy(sku);
			return 1;
		}
		if (null == mHelper)
		{
			return 0;
		}
		if (!inappsok)
		{
			try
			{
				mHelper.startSetup(mOnSetup);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return 0;
		}
		lastSku = sku;
		int m = getPurchaseMode();
		if (m > 0)
		{
			if (1 == m)
			{
				TEST_PURCHASE_SKU = TEST_PURCHASED;
			}
			else
			if (2 == m)
			{
				TEST_PURCHASE_SKU = TEST_CANCELED;
			}
			else
			if (3 == m)
			{
				TEST_PURCHASE_SKU = TEST_ITEM_UNAVALBLE;
			}
			else
			if (4 == m)
			{
				TEST_PURCHASE_SKU = TEST_REFUNDED;
			}
			sku = TEST_PURCHASE_SKU;
		}
		try
		{
			mHelper.flagEndAsync();
			mHelper.launchPurchaseFlow((Activity)sContext, sku, SK_PURCHASE_REQUEST, mPurchaseFinishedListener);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	private static int killProduct(final String sku)
	{
/*
		if (null == mHelper)
		{
			return 0;
		}
		if (!inappsok)
		{
			try
			{
				mHelper.startSetup(mOnSetup);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return 0;
		}
		try
		{
			((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
			{
			    @Override
				public void run()
				{
					((Cocos2dxActivity)sContext).mHelper.flagEndAsync();
					((Cocos2dxActivity)sContext).mHelper.consumeAsync(((Cocos2dxActivity)sContext).mInventory.getPurchase(sku), mConsumeFinishedListener);
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		*/
		return 1;
	}

	public static String getGameservicesPlayerId()
	{
		if (sContext._isAmazonApp)
			return "";

        if (null == ((Cocos2dxActivity)sContext).gameHelper)
            return "";

		if (isGameCenterDisabled())
			return "";

		return ((Cocos2dxActivity)sContext).gameHelper.getGamesClient().getCurrentPlayerId(); 
	}

	public static boolean isGameservicesAuthenticated()
	{
		if (sContext._isAmazonApp)
			return false;

        if (null == ((Cocos2dxActivity)sContext).gameHelper)
            return false;

		if (isGameCenterDisabled())
			return false;

		return ((Cocos2dxActivity)sContext).gameHelper.isSignedIn(); 
	}

    public static ArrayList<String> GetInAppSkus() {
        ArrayList<String> skus = new ArrayList<String>();
        int n = getTotalInApps();
        Log.d(TAG, "$$$ Total in-apps to query " + n);
        for (int i = 0; i < n; ++i)
        {
            String storeId = getInAppStoreId(i);
            Log.d(TAG, i + " $$$ storeId " + storeId);
            skus.add(storeId);
        }
        return skus;
    }



	public static Context getContext()
	{
		return sContext;
	}

	private static int highscore = -1;

	// google play game services

	public void onSignInSucceeded()
	{
		onGameCenterAuth();
	}

	public void onSignInFailed()
	{
		onGameCenterAuthFailed();
	}

	public static void showLeaderboard(final String leaderboardId)
	{
		if (sContext._isAmazonApp)
		{
			sContext.runOnUiThread(new Runnable() { @Override public void run()
			{
                sContext.GetAmazonGameCircleClient().ShowLeaderboard(leaderboardId);
            }});
            return;
        }

        if (null == ((Cocos2dxActivity)sContext).gameHelper)
        {
            return;
        }

		if (isGameCenterDisabled())
		{
			return;
		}
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				if (!((Cocos2dxActivity)sContext).isSignedIn())
				{
					((Cocos2dxActivity)sContext).gameHelper.beginUserInitiatedSignIn();
				}
				else
				{
					((Cocos2dxActivity)sContext).startActivityForResult(((Cocos2dxActivity)sContext).gameHelper.getGamesClient().getLeaderboardIntent(leaderboardId), REQUEST_LEADERBOARD);
				}
			}
		});
	}

	public static void logEvent(final String event)
	{
		if (sContext == null)
		{
			return;
		}
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				FlurryAgent.logEvent(event);
			}
		});
	}

	protected boolean isSignedIn()
	{
        return gameHelper != null && gameHelper.isSignedIn();
    }

	public static void showAchievements()
	{
        if (sContext._isAmazonApp) {
            sContext.runOnUiThread(new Runnable() { @Override public void run() {
                sContext.GetAmazonGameCircleClient().ShowAchievements();
            }});
            return;
        }

        if (sContext.gameHelper == null)
            return;

		//Log.d(TAG, "showAchievements");
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				if (!((Cocos2dxActivity)sContext).isSignedIn())
				{
					((Cocos2dxActivity)sContext).gameHelper.beginUserInitiatedSignIn();
				}
				else
				{
					((Cocos2dxActivity)sContext).startActivityForResult( ((Cocos2dxActivity)sContext).gameHelper.getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
				}
			}
		});
	}

	public static void submitScore(final String leaderboardId, final int score)
	{
		if (sContext == null)
		{
			return;
		}
        if (sContext._isAmazonApp) {
            sContext.runOnUiThread(new Runnable() { @Override public void run() {
                sContext.GetAmazonGameCircleClient().SubmitScore(leaderboardId, score);
            }});
            return;
        }
		if (isGameCenterDisabled())
		{
			return;
		}
		if (!((Cocos2dxActivity)sContext).isSignedIn())
		{
			return;
		}
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				((Cocos2dxActivity)sContext).gameHelper.getGamesClient().submitScoreImmediate((Cocos2dxActivity)sContext, leaderboardId, score);
			}
		});
	}

	public void onScoreSubmitted(int statusCode, SubmitScoreResult result)
	{
		if (isGameCenterDisabled())
		{
			return;
		}
		if (GamesClient.STATUS_OK  == statusCode)
		{
			onScoreSubmitted(result.getLeaderboardId(), (int)result.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME).rawScore);
		}
	}

	public static void reportAchievement(final String achievementId, final int percent)
	{
		if (sContext == null)
		{
			return;
		}
        Log.i(TAG, "reportAchievement: " + achievementId);

        if (sContext._isAmazonApp) {
            sContext.runOnUiThread(new Runnable() { @Override public void run() {
                sContext.GetAmazonGameCircleClient().ReportAchievement(achievementId, percent);
            }});
            return;
        }

		if (isGameCenterDisabled())
		{
			return;
		}
		if (percent < 100 || !((Cocos2dxActivity)sContext).isSignedIn()) // google does not support percents
		{
			return;
		}
		sContext.runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
                try {
				    sContext.gameHelper.getGamesClient().unlockAchievementImmediate(sContext, achievementId);
                } catch (Exception ex) {
                    Log.e(TAG, "Error achievement " + achievementId + " " + ex.toString());
                }
			}
		});
	}


	public static void openOfferWall()
	{
		if (!isOfferWallEnabled())
		{
			return;
		}
		try
		{
	        if(SK_OFFER_WALL_PROVIDER_TAPJOY == offerWallProvider)
	        {
	            ((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
	            {
	                @Override
	                public void run()
	                {
	                    TapjoyConnect.getTapjoyConnectInstance().showOffers();
	                }
	            });
	        }
	        else if(SK_OFFER_WALL_PROVIDER_SPONSORPAY == offerWallProvider)
	        {
	            ((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
	            {
	                @Override
	                public void run()
	                {
	                    Intent offerWallIntent = SponsorPayPublisher.getIntentForOfferWallActivity((Cocos2dxActivity)sContext, true);
	                    ((Cocos2dxActivity)sContext).startActivityForResult(offerWallIntent, SponsorPayPublisher.DEFAULT_OFFERWALL_REQUEST_CODE);
	                }
	            });
	        }
		}
		catch(Exception e)
		{
            e.printStackTrace();
		}
	}

	public void onAchievementUpdated(int statusCode, String achievementId)
	{
		if (GamesClient.STATUS_OK  == statusCode)
		{
			onAchievementReported(achievementId, 100);
		}
	}

	public static void openUrl(final String url)
	{
		if (sContext == null)
		{
			return;
		}
		sContext.runOnUiThread(new Runnable() { @Override public void run()
		{
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			sContext.startActivity(browserIntent);
		}});
	}

	public static void showChartboost()
	{
		if (sContext == null)
		{
			return;
		}
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				if (((Cocos2dxActivity)sContext).tapjoyOk)
				{
					TapjoyConnect.getTapjoyConnectInstance().getTapPoints((Cocos2dxActivity)sContext);
				}
				Chartboost cb = ((Cocos2dxActivity)sContext).cb;
				if (null != cb)
				{
					cb.showInterstitial();
				}
			}
		});
	}

	public static void showAdColonyNoReward(final boolean showFailDialog)
	{
		if (sContext == null)
		{
			return;
		}
		videoAdDialog = showFailDialog;
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				if (((Cocos2dxActivity)sContext).tapjoyOk)
				{
					TapjoyConnect.getTapjoyConnectInstance().getTapPoints((Cocos2dxActivity)sContext);
				}
	    		AdColonyVideoAd ad = new AdColonyVideoAd(adcolonyZoneId()).withListener( (Cocos2dxActivity)sContext );
	    		ad.show();
			}
		});
	}

	public static void showAdColonyReward(final boolean showFailDialog)
	{
		if (!adcolonyLoaded)
		{
			return;
		}
		videoAdDialog = showFailDialog;
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				((Cocos2dxActivity)sContext).v4vc_ad = new AdColonyV4VCAd(adcolonyZoneId()).withListener( (Cocos2dxActivity)sContext ).withConfirmationDialog().withResultsDialog();
				// v4vc_ad.getRewardName()
				// v4vc_ad.getAvailableViews();
	    		((Cocos2dxActivity)sContext).v4vc_ad.show();
			}
		});
	}
	private static boolean videoAdDialog = false;
	private static boolean adcolonyLoaded = false;
	private static boolean moreGamesWasTapped = false;
	public static void showMoreGames()
	{
		if (sContext == null)
		{
			return;
		}
		((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				Chartboost cb = ((Cocos2dxActivity)sContext).cb;
				if (null != cb)
				{
					cb.showMoreApps();
					moreGamesWasTapped = true;
				}
			}
		});
	}

	public static void showAds(final boolean isVisible)
	{
		if (sContext == null)
		{
			return;
		}
		//Log.i("cocos2dx", MessageFormat.format("showAds = {0}", isVisible));
		if (sContext._isAmazonApp)
		{
			sContext.runOnUiThread(new Runnable() { @Override public void run()
			{
				sContext.GetAmazonAdClient().ShowAds(isVisible);
				}});
				return;
		}
		((Cocos2dxActivity)sContext).mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (((Cocos2dxActivity)sContext).tapjoyOk)
				{
					TapjoyConnect.getTapjoyConnectInstance().getTapPoints((Cocos2dxActivity)sContext);
				}
				AdView av = ((Cocos2dxActivity) sContext).adView;
				if (null != av)
				{
					av.setVisibility(isVisible ? AdView.VISIBLE : AdView.GONE);
				}
			}
		}, 50);

	}

	public static void rateMe()
	{
		if (sContext == null)
		{
			return;
		}
		sContext.runOnUiThread(new Runnable() { @Override public void run()
		{
			String packageName = sContext.getApplicationContext().getPackageName();
			//NOTE: https://developer.amazon.com/post/Tx3A1TVL67TB24B/Linking-To-the-Amazon-Appstore-for-Android.html
			String prefix = sContext._isAmazonApp ? "amzn://apps/android?p=" : "market://details?id=";
			Cocos2dxActivity.openUrl(prefix + packageName);
			}});
	}

	public static void postTwitter(final String message, final String url)
	{
		if (null == sContext)
		{
			return;
		}
        sContext.runOnUiThread(new Runnable() { @Override public void run()
        {
            sContext._twitterClient.SendTweet(message + "\n" + url, true);
            /*
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setType("application/twitter");
            try
            {
                ((Activity)sContext).startActivityForResult(Intent.createChooser(intent, "Pick twitter app:"), POST_TWITTER);
            }
            catch(Exception e)
            {
                ((Cocos2dxActivity)sContext).showDialog("There are no twitter clients installed.", "Error");
            }
            */
        }});
	}

	public static void sendEmail(final String to, final String subject, final String message, final String photoPath)
	{
		if (null == sContext)
		{
			return;
		}

        sContext.runOnUiThread(new Runnable() { @Override public void run()
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {to});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setType("message/rfc822");
            try
            {
                if (photoPath.length() > 0)
                {
                    File file = new File(photoPath);
                    if (file.exists() && file.canRead())
                    {
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    }
                    emailPhotoPath = photoPath;
                }
                else
                {
                    emailPhotoPath = null;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            try
            {
                ((Activity)sContext).startActivityForResult(Intent.createChooser(intent, "Pick an email app:"), SEND_EMAIL);
            }
            catch(Exception e)
            {
                ((Cocos2dxActivity)sContext).showDialog("There are no email clients installed.", "Error");
            }
        }});
	}

	// ===========================================================
	// Constructors
	// ===========================================================
	private AdView adView = null;
	private boolean tapjoyOk = false;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		//useCb = android.os.Build.VERSION.SDK_INT >= 14 || _isAmazonApp; // android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH
        Log.i(TAG, "SDK Version " + android.os.Build.VERSION.SDK_INT);
        _isOnDestroyCalled = false;

		super.onCreate(savedInstanceState);
		sContext = this;
    	this.mHandler = new Cocos2dxHandler(this);
		Cocos2dxHelper.init(this, this);
    	this.init();

        _twitterClient = new TwitterClient(this);
        _facebookCustomClient = new FacebookCustomClient(this);

        String appId = cbAppId();
        String appSignature = cbSignature();
		this.cb = Chartboost.sharedChartboost();
		this.cb.onCreate(this, appId, appSignature, this);

		AppLovinSdk.initializeSdk( this );

		mHandler.postDelayed(new Runnable()
		{
		    @Override
			public void run()
			{
				if (!isGameCenterDisabled() && !_isAmazonApp)
				{
					Cocos2dxActivity.this.gameHelper = new GameHelper(Cocos2dxActivity.this);
					Cocos2dxActivity.this.gameHelper.setup(Cocos2dxActivity.this, CLIENT_GAMES);
                    Cocos2dxActivity.this.gameHelper.onStart(Cocos2dxActivity.this);
                    Cocos2dxActivity.this.initGameServices();
				}
			}
		}, 3000);

		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				Hashtable<String, String> flags = new Hashtable<String, String>();
				//flags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

				if (isOfferWallEnabled())
				{
					offerWallProvider = getOfferWallProvider();
			        TapjoyConnect.requestTapjoyConnect(getApplicationContext(), tapjoyAppId(), tapjoyAppKey(), flags);

			        if(SK_OFFER_WALL_PROVIDER_TAPJOY == offerWallProvider)
			        {
                        TapjoyConnect.getTapjoyConnectInstance().setEarnedPointsNotifier(Cocos2dxActivity.this);

			            TapjoyConnect.getTapjoyConnectInstance().setTapjoyViewNotifier(new TapjoyViewNotifier()
			                {
			                    @Override
			                    public void viewWillOpen(int viewType)
			                    {
			                        Log.e(TAG, "viewWillOpen: ");
			                    }

			                    @Override
			                    public void viewWillClose(int viewType)
			                    {
			                        Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
			                        {
			                        @Override
			                        public void run()
			                        {
			                            onOfferWallClose();
			                        }
			                    });
			                }
			                @Override
			                public void viewDidOpen(int viewType)
			                {
			                    Log.e(TAG, "viewDidOpen: ");
			                }
			                @Override
			                public void viewDidClose(int viewType)
			                {
								Log.e(TAG, "viewDidClose: ");
								TapjoyConnect.getTapjoyConnectInstance().getTapPoints(Cocos2dxActivity.this);
								Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
								{
									@Override
									public void run()
									{
										onOfferWallClose();
									}
								});
							}
						});
						TapjoyConnect.getTapjoyConnectInstance().getTapPoints(Cocos2dxActivity.this);
						tapjoyOk = true;
			        }
			        else if(SK_OFFER_WALL_PROVIDER_SPONSORPAY == offerWallProvider)
			        {
			            try {
			                SponsorPay.start(sponsorpayAppId(), null, sponsorpayAppKey(), (Cocos2dxActivity)sContext);
			                VirtualCurrencyConnector.shouldShowToastNotification(true);
					                SponsorPayPublisher.requestNewCoins((Cocos2dxActivity)sContext, Cocos2dxActivity.this);
			                //logging
			                SponsorPayLogger.enableLogging(true);

			            } catch (RuntimeException e){
			                Log.d(TAG, e.getLocalizedMessage());
			            }
			        }
				}
			}
		}, 4000);

		mHandler.postDelayed(new Runnable()
		{
		    @Override
			public void run()
			{
				AdColony.configure( Cocos2dxActivity.this, "version:1.0,store:google", adcolonyAppId(), adcolonyZoneId());
			    AdColony.addV4VCListener( Cocos2dxActivity.this );
			    AdColony.addAdAvailabilityListener( Cocos2dxActivity.this );
			    adcolonyLoaded = true;
			}
		}, 7000);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onResume()
	{
		super.onResume();
		Cocos2dxHelper.onResume();
		this.mGLSurfaceView.onResume();
		if (report_camera)
		{

			final int w = report_bitmap == null ? -1 : report_bitmap.getWidth();
			final int h = report_bitmap == null ? -1 : report_bitmap.getHeight();
			final byte[] b = report_bitmap == null ? null : new byte[report_bitmap.getWidth() * report_bitmap.getHeight() * 4];
			if (b != null)
			{
				final ByteBuffer buf = ByteBuffer.wrap(b);
				buf.order(ByteOrder.nativeOrder());
				report_bitmap.copyPixelsToBuffer(buf);
				//Log.i(TAG, "w =" + report_bitmap.getWidth() + " h = " + report_bitmap.getHeight() + " config = " + report_bitmap.getConfig());
			}
			report_bitmap = null;
			photo_width = -1;
			photo_height = -1;
			report_camera = false;

			mHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
					{
						@Override
						public void run()
						{
							nativeOnTakePhoto(w, h, b);
							System.gc();
						}
					});
				}
			}, 1000);
		}

		if (isOfferWallEnabled())
		{
			if(SK_OFFER_WALL_PROVIDER_TAPJOY == offerWallProvider)
			{
				TapjoyConnect.getTapjoyConnectInstance().getTapPoints(this);
			}
			else
			if(SK_OFFER_WALL_PROVIDER_SPONSORPAY == offerWallProvider)
			{
				SponsorPayPublisher.requestNewCoins((Cocos2dxActivity)sContext, this);
			}
		}

        if (_isAmazonApp)
		{
            GetAmazonInAppClient().OnResume();
            GetAmazonGameCircleClient().OnResume();
        }
	}

    @Override
    public void onSPCurrencyServerError(CurrencyServerAbstractResponse response) {
        // Log.d("SPCurrencyServerListener",
        // "Request or Response Error: " + response.getErrorType());
    }

    @Override
    public void onSPCurrencyDeltaReceived(CurrencyServerDeltaOfCoinsResponse response)
	{

        final int amount = (int)response.getDeltaOfCoins();

        Log.d("SPCurrencyServerListener",
        "Response From Currency Server. Delta of Coins: " +
        String.valueOf(amount) +
        ", Latest Transaction Id: " + response.getLatestTransactionId());

        if(amount > 0)
            Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
            {
                @Override
                public void run()
                {
                    onOfferWallEarn(amount);
                }
            });
    }

	@Override
	protected void onPause()
	{
		super.onPause();
		Cocos2dxHelper.onPause();
		this.mGLSurfaceView.onPause();
		if (isOfferWallEnabled())
		{
			try
			{
				TapjoyConnect.getTapjoyConnectInstance().enableDisplayAdAutoRefresh(false);
			}
			catch(Exception e)
			{
	            e.printStackTrace();
			}
		}
		if (_isAmazonApp)
		{
            GetAmazonGameCircleClient().OnPause();
        }
	}

	@Override
	public void earnedTapPoints(final int amount)
	{
		//Log.e(TAG, "XXXYYY You've just earned " + amount + " Tap Points!");
		Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
		{
			@Override
			public void run()
			{
				onOfferWallEarn(amount);
			}
		});
	}

	@Override
	public void getUpdatePoints(String currencyName, int pointTotal)
	{
	    //Log.d(TAG, "getUpdatePoints " + currencyName + " " + pointTotal);
	}

	@Override
	public void getUpdatePointsFailed(String error)
	{
	    //Log.e(TAG, error);
	}

	@Override
	public void getAwardPointsResponse(String s, int i)
	{
	    Log.d(TAG, "Tapjoy : getAwardPointsResponse" + s + i);
		final int amount = i;
		Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
		{
			@Override
			public void run()
			{
				onVideoAdAward(amount);
			}
		});
	}

	@Override
	public void getAwardPointsResponseFailed(String s)
	{
	    //Log.d(TAG, s);
	}

	private static int state = -1;
	private static int scoreSubm = 0;

	@Override
	public void showDialog(final String pTitle, final String pMessage)
	{
        final Cocos2dxHandler handler = this.mHandler;
        this.runOnUiThread(new Runnable() { @Override public void run()
        {
            Message msg = new Message();
            msg.what = Cocos2dxHandler.HANDLER_SHOW_DIALOG;
            msg.obj = new Cocos2dxHandler.DialogMessage(pTitle, pMessage);
            handler.sendMessage(msg);
        }});
	}

	public static class FaceBookPost
	{
		public String message;
		public String url;
		public String title;
		public String subtitle;
		public String description;
		public String pictureUrl;



		public FaceBookPost(final String message, final String url, final String title, final String subtitle, final String description, final String pictureUrl)
		{
			this.message = message;
			this.url = url;
			this.title = title;
			this.subtitle = subtitle;
			this.description = description;
			this.pictureUrl = pictureUrl;
		}
	}

	public static class FaceBookImagePost
	{
		public String path;
		public String message;

		public FaceBookImagePost(final String path, final String message)
		{
			this.path = path;
			this.message = message;
		}
	}

	public FaceBookPost cachedFacebookPost;
    public FaceBookImagePost cachedFacebookImagePost;

	@Override
	public void onDestroy()
	{
        _isOnDestroyCalled = true;
		if (null != mHelper)
		{
			mHelper.dispose();
			mHelper = null;
		}
		if (null != cb)
		{
			cb.onDestroy(this);
		}

        if (_isAmazonApp)
        {
            GetAmazonAdClient().Destroy();
            _amazonAdClient = null;
        }
		else if (adView != null)
		{
			adView.destroy();
            adView = null;
		}

		if (isOfferWallEnabled())
		{
			try
			{
				TapjoyConnect.getTapjoyConnectInstance().sendShutDownEvent();
			}
			catch(Exception e)
			{
                e.printStackTrace();
			}
		}

        if (_isAmazonApp) {
            GetAmazonGameCircleClient().OnDestroy();
        }

		super.onDestroy();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		if (null != cb)
		{
			cb.onStart(this);
			cb.startSession();
			cb.cacheInterstitial();
			cb.cacheMoreApps();
		}

		try
		{
			FlurryAgent.onStartSession(Cocos2dxActivity.this, flurryApiKey());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (Cocos2dxActivity.this.gameHelper != null)
				{
					Cocos2dxActivity.this.gameHelper.onStart(Cocos2dxActivity.this);
				}
			}
		}, 1500);

		mHandler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (null == Cocos2dxActivity.this.mHelper)
				{
					Cocos2dxActivity.this.mHelper = new IabHelper(sContext, InAppKey());
					//mHelper.enableDebugLogging(true);
				}
				try
				{
					Cocos2dxActivity.this.mHelper.startSetup(mOnSetup);
				}
				catch(Exception e)
				{
                    e.printStackTrace();
				}
			}
		}, 7000);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (this.gameHelper != null)
		{
			this.gameHelper.onStop();
		}
		if (this.cb != null)
		{
			this.cb.onStop(this);
		}
		try
		{
			FlurryAgent.onEndSession(this);
		}
		catch(Exception e)
		{
            e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed()
	{
	    if (this.cb != null && this.cb.onBackPressed())
		{
			return;
		}
		else
		{
			super.onBackPressed();
		}
	}

	public void doFacebookPostWall(FaceBookPost post)
	{
		if (!prepareToPost())
		{
			return;
		}
		final Bundle postParams = new Bundle();
		postParams.putString("message", post.message);
		postParams.putString("link", post.url);

		postParams.putString("name", post.title);
		postParams.putString("caption", post.subtitle);
		postParams.putString("description", post.description);
		postParams.putString("picture", post.pictureUrl);

		final Request.Callback callback = new Request.Callback()
		{
			public void onCompleted(Response response)
			{
                FacebookRequestError error = response.getError();
                if (error != null)
                {
                    showDialog("Failure", "Message is not posted because of error");
                    Log.i(TAG, "Facebook error:" + error.toString());
                    return;
                }

				String postId = null;
				try
				{
					postId = response.getGraphObject().getInnerJSONObject().getString("id");
				}
				catch (Exception e)
				{
					Log.i(TAG, "Facebook error " + e.getMessage());
				}

                if (postId != null) {
                    showDialog("Success", "Message is successfully posted on Facebook");
                } else {
                    showDialog("Failure", "Message is not posted because of error");
                }
			}
		};
		this.runOnUiThread(new Runnable()
		{
		    @Override
			public void run()
			{
				Request request = new Request(Session.getActiveSession() , "me/feed", postParams, HttpMethod.POST, callback);
				RequestAsyncTask task = new RequestAsyncTask(request);
				task.execute();
			}
		});
		cachedFacebookPost = null;
	}

	public void doFacebookImagePost(FaceBookImagePost post)
	{
		if (!prepareToPost())
		{
			return;
		}
		//Log.i(TAG, "trying to post image on Facebook " + post.path + " + " + post.message);
		try
		{
			final String path = post.path;

			final Request.Callback callback = new Request.Callback()
			{
				public String p = path;

				@Override
				public void onCompleted(Response response)
				{
					FacebookRequestError error = response.getError();
	 				if (error != null)
					{
						//Log.i(TAG, "facebook post image failed");
					}
					else
					{
						//Log.i(TAG, "facebook post image ok " + p);
					}
					try
					{
						File f = new File(p);
						if (f.delete())
						{
							//Log.i(TAG, "temp image successfully deleted!");
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			};

			final String message = post.message;
			this.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Request request = Request.newUploadPhotoRequest(Session.getActiveSession() , new File(path),  callback);
						Bundle params = request.getParameters();
						params.putString("message", message);
						RequestAsyncTask task = new RequestAsyncTask(request);
						task.execute();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		cachedFacebookImagePost = null;
	}

	 @Override
	 public void facebookPostWall(final String message, final String url, final String title, final String subtitle, final String description, final String pictureUrl)
	{
		FaceBookPost post = new FaceBookPost(message, url, title, subtitle, description, pictureUrl);
		if (!prepareToPost())
		{
			cachedFacebookPost = post;
			return;
		}
		doFacebookPostWall(post);
	}

	@Override
	public void facebookPostImage(final String path, final String message)
	{
		FaceBookImagePost post = new FaceBookImagePost(path, message);
		if (!prepareToPost())
		{
			cachedFacebookImagePost = post;
			return;
		}
		doFacebookImagePost(post);
	}

    public static void facebookOpenPage(final String pageId) {
        sContext._facebookCustomClient.OpenPage(pageId);
    }

    public static void facebookCheckPageLike(final String pageId) {
        sContext._facebookCustomClient.CheckIfPageHasLike(pageId);
    }

	@Override
	public void showDialogYesNo(final String message, final String title, final String yes_text, final String no_text)
	{
        final Cocos2dxHandler handler = this.mHandler;

        this.runOnUiThread(new Runnable() { @Override public void run()
        {
            Message msg = new Message();
            msg.what = Cocos2dxHandler.HANDLER_SHOW_DIALOG_YES_NO;
            msg.obj = new Cocos2dxHandler.DialogMessage(message, title, yes_text, no_text);
            handler.sendMessage(msg);
        }});
	}

	private boolean auth()
	{
		if (Session.getActiveSession() == null || !Session.getActiveSession().isOpened())
		{
			Session.openActiveSession(this, true, new Session.StatusCallback()
			{
				@Override
				public void call(Session session, SessionState state, Exception exception)
				{
					if (session.isOpened())
					{
						getUser();
					}
                    else if (exception != null)
                    {
                        showDialog("Failure", "No connection to Facebook");
                    }
				}
			});
			return false;
		}
		else
		{
			return true;
		}
	}

	private boolean getUser()
	{
		if (user != null)
		{
			return true;
		}
		Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback()
		{
			@Override
			public void onCompleted(GraphUser user, Response response)
			{
				if (user != null)
				{
					Cocos2dxActivity.this.user = user;
					Log.i(TAG, "FB name is " + user.getName());
					if (prepareToPost())
					{
						if (null != Cocos2dxActivity.this.cachedFacebookPost)
						{
							doFacebookPostWall(Cocos2dxActivity.this.cachedFacebookPost);
						}
						if (null != Cocos2dxActivity.this.cachedFacebookImagePost)
						{
							doFacebookImagePost(Cocos2dxActivity.this.cachedFacebookImagePost);
						}

                        _facebookCustomClient.TryToPostCachedInfo();
					}
				}
			}
		});
		return false;
	}

	public boolean prepareToPost()
	{
		if (!auth())
		{
			return false;
		}
		if (!getUser())
		{
			return false;
		}
        Session session = Session.getActiveSession();
		if (!session.getPermissions().contains("publish_actions"))
		{
			Session.NewPermissionsRequest r = new Session.NewPermissionsRequest(Cocos2dxActivity.this, PERMISSIONS);
			// this code does not work now because of bugs in Facebook Android SDK 3.0.1, we will use workaround
			/*
			r.setCallback(new Session.StatusCallback()
			{
				@Override
				public void call(Session session, SessionState state, Exception exception)
				{
					if (session.isOpened())
					{
						if (null != Cocos2dxActivity.this.cachedFacebookPost)
						{
							doFacebookPostWall(Cocos2dxActivity.this.cachedFacebookPost);
						}
					}
				}
			});
			*/

			try
			{
				session.requestNewPublishPermissions(r);

                /*
                //checkFirstPost and checkFirstImagePost don't work
				mHandler.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						Cocos2dxActivity.this.checkFirstPost();
						Cocos2dxActivity.this.checkFirstImagePost();
					}
				}, 3000);
                */

                _facebookCustomClient.RepeatTryToPostFacebookPost(10);
                _facebookCustomClient.RepeatTryToPostCachedInfo(10);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				mHandler.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						Cocos2dxActivity.this.prepareToPost();
					}
				}, 5000);
			}
			return false;
		}
		return true;
    }

	void checkFirstPost()
	{
		if (null != Cocos2dxActivity.this.cachedFacebookPost)
		{
			doFacebookPostWall(Cocos2dxActivity.this.cachedFacebookPost);
		}
		else
		{
			mHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					Cocos2dxActivity.this.checkFirstPost();
				}
			}, 3000);
		}
	}

	void checkFirstImagePost()
	{
		if (null != Cocos2dxActivity.this.cachedFacebookImagePost)
		{
			doFacebookImagePost(Cocos2dxActivity.this.cachedFacebookImagePost);
		}
		else
		{
			mHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					Cocos2dxActivity.this.checkFirstImagePost();
				}
			}, 7000);
		}
	}

	@Override
	public void takePhoto(final int w, final int h, final int source)
	{
		if (SK_IMAGE_SOURCE_UNDEFINED == source)
		{
			return;
		}
		photo_width  = w;
		photo_height = h;
		photo_source = source;
		if (photo_source == SK_IMAGE_SOURCE_CAMERA)
		{
			Intent intent = new Intent(this, SKCocos2dxCamera.class);
			startActivityForResult(intent, TAKE_PHOTO);
		}
		else
		{
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select Picture"), TAKE_IMAGE_FROM_GALLERY);
		}
	}

	private static native void nativeOnTakePhoto(final int pWidth, final int pHeight, final byte[] pPixels);
	private static native void nativeOnChooseImageFromLibrary(final String imagePath);

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		try
		{
			if (this.gameHelper != null)
			{
				gameHelper.onActivityResult(requestCode, resultCode, data);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			if (null != mHelper && mHelper.handleActivityResult(requestCode, resultCode, data))
			{
				Log.d(TAG, "onActivityResult handled by IABUtil.");
				return;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (requestCode == SEND_EMAIL)
		{
			if (null != emailPhotoPath)
			{
				try
				{
					File f = new File(emailPhotoPath);
					//if (f.delete())
					{
					//	Log.e(TAG, "WOWO : file is removed!!!!");
					}
				}
				catch(Exception e)
				{

				}
			}
		}
		else
		if(requestCode == TAKE_IMAGE_FROM_GALLERY)
		{
			report_camera = true;
			String imageFilePath = null;
			if (null != data && null != data.getData())
			{
				Uri _uri = data.getData();
				if (null != _uri)
				{
					//User had pick an image.
					Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
					cursor.moveToFirst();
					//Link to the image
					imageFilePath = cursor.getString(0);
					cursor.close();
				}
			}
			if (null != imageFilePath)
			{
				//Log.i(TAG, "Image is selected, path is " + imageFilePath);
				try
				{
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
					report_bitmap = BitmapFactory.decodeFile(imageFilePath, opts);
				}
				catch(Exception e)
				{
					report_bitmap = null;
				}
				if (null != report_bitmap && photo_width != -1 && photo_height != -1)
				{
					report_bitmap = SKCreateScaledBitmap(report_bitmap, photo_width, photo_height, true);
				}
			}
			else
			{
				report_bitmap = null;
				//Log.i(TAG, "No image selected omg");
			}
		}
		else
		if (requestCode == TAKE_PHOTO)
		{
			report_camera = true;

			if(resultCode == RESULT_CANCELED)
			{
				report_bitmap = null;
			}
			else
			{
				Bundle extras = data.getExtras();
				byte[] b = extras.getByteArray("photo");
				if (null == b)
				{
						report_bitmap = null;
				}
				else
				{
					Bundle params  = extras.getBundle("params");
					int isFrontFace = params.getInt("frontcamera");

					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
					report_bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, opts);
					if (photo_width != -1 && photo_height != -1)
					{
						report_bitmap = SKCreateScaledBitmap(report_bitmap, photo_width, photo_height, true);
					}
			        if(android.os.Build.VERSION.SDK_INT > 13 && isFrontFace != 0)
			        {
						 Matrix rotateRight = new Matrix();
			             float[] mirrorY = {  -1, 0, 0,
				                              0, -1, 0,
				                              0, 0, 1};
			             rotateRight = new Matrix();
			             Matrix matrixMirrorY = new Matrix();
			             matrixMirrorY.setValues(mirrorY);
			             rotateRight.postConcat(matrixMirrorY);
				         report_bitmap= Bitmap.createBitmap(report_bitmap, 0, 0, report_bitmap.getWidth(), report_bitmap.getHeight(), rotateRight, true);
			        }
				}
			}
		}
		if (Session.getActiveSession() != null)
		{
			try
			{
				Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

        _facebookCustomClient.OnActivityResult(requestCode, resultCode, data);
	}

	public static Bitmap SKCreateScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter)
	{
	    Matrix m = new Matrix();
	    m.setScale(dstWidth  / (float)src.getWidth(), dstHeight / (float)src.getHeight());
	    Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, src.getConfig());
	    Canvas canvas = new Canvas(result);

	        Paint paint = new Paint();
	        paint.setFilterBitmap(filter);
	        canvas.drawBitmap(src, m, paint);

	    return result;

	}
	@Override
	public void showEditTextDialog(final String pTitle, final String pContent, final int pInputMode, final int pInputFlag, final int pReturnType, final int pMaxLength) {
		Message msg = new Message();
		msg.what = Cocos2dxHandler.HANDLER_SHOW_EDITBOX_DIALOG;
		msg.obj = new Cocos2dxHandler.EditBoxMessage(pTitle, pContent, pInputMode, pInputFlag, pReturnType, pMaxLength);
		this.mHandler.sendMessage(msg);
	}

	@Override
	public void runOnGLThread(final Runnable pRunnable) {
		this.mGLSurfaceView.queueEvent(pRunnable);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private FrameLayout framelayout;

	public void init() {

    	// FrameLayout
        ViewGroup.LayoutParams framelayout_params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                       ViewGroup.LayoutParams.FILL_PARENT);
        framelayout = new FrameLayout(this);
        framelayout.setLayoutParams(framelayout_params);

        // Cocos2dxEditText layout
        ViewGroup.LayoutParams edittext_layout_params =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                       ViewGroup.LayoutParams.WRAP_CONTENT);
        Cocos2dxEditText edittext = new Cocos2dxEditText(this);
        edittext.setLayoutParams(edittext_layout_params);

        // ...add to FrameLayout
        framelayout.addView(edittext);


		this.mGLSurfaceView = this.onCreateView();

        // ...add to FrameLayout
        framelayout.addView(this.mGLSurfaceView);

        // Switch to supported OpenGL (ARGB888) mode on emulator
        if (isAndroidEmulator())
           this.mGLSurfaceView.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);

        this.mGLSurfaceView.setCocos2dxRenderer(new Cocos2dxRenderer());
        this.mGLSurfaceView.setCocos2dxEditText(edittext);

		boolean r = isAdsRemoved() || isBannerAdsDisabled();
		String manufacturer = android.os.Build.MANUFACTURER;
		if (!r)
		{
		    if (_isAmazonApp)
            {
                GetAmazonAdClient().OnCreate();
            }

			mHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{

                    //after onDestroy, do nothing
                    if (_isOnDestroyCalled) {
                        Log.i(TAG, "init _isOnDestroyCalled = true");
                        return;
                    }

                    if (!_isAmazonApp)
                    {
                        Cocos2dxActivity.this.adView = new AdView(Cocos2dxActivity.this, AdSize.SMART_BANNER, mediationId());

                        ViewGroup.LayoutParams ad_layout_params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                Cocos2dxActivity.this.adView.setLayoutParams(ad_layout_params);

                        AdRequest adRequest = new AdRequest();

                        if (isAdmobInTestMode())
                        {
                            // TODO : add test devices via C++ code
                            adRequest.addTestDevice("65B78A031CE460DE072BA021ED529264");
                            adRequest.addTestDevice("ABEA16E09AE540395A5D02E66F46C17F");
                            adRequest.addTestDevice("9B5088538B22172533C65809815858D5");
                            adRequest.addTestDevice("BC805D95AC92DB417CF3D5B1021F0EED");
                            adRequest.setTesting(true);
                        }

                        Cocos2dxActivity.this.adView.loadAd(adRequest);
                    }

                    final View adViewConcrete = _isAmazonApp ? GetAmazonAdClient().GetAdView() : adView;
                    adViewConcrete.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
                    {
                        @Override
                        public void onGlobalLayout()
                        {
                            int height = adViewConcrete.getHeight();
                            if (height > 0)
                            {
                                setAdViewHeight(height);
                            }
                        }
                    });
					Cocos2dxActivity.this.framelayout.addView(adViewConcrete);

                    if (!_isAmazonApp)
                    {
                        Cocos2dxActivity.this.adView.setVisibility(AdView.VISIBLE);
                    }
				}
			}, 7000);
		}
        // Set framelayout as the content view
		setContentView(framelayout);
	}

    public Cocos2dxGLSurfaceView onCreateView() {
    	return new Cocos2dxGLSurfaceView(this);
    }

   private final static boolean isAndroidEmulator() {
      String model = Build.MODEL;
      Log.d(TAG, "model=" + model);
      String product = Build.PRODUCT;
      Log.d(TAG, "product=" + product);
      boolean isEmulator = false;
      if (product != null) {
         isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
      }
      Log.d(TAG, "isEmulator=" + isEmulator);
      return isEmulator;
   }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        _twitterClient.OnNewIntent(intent);
    }


	/*
     * shouldDisplayInterstitial(String location)
     *
     * This is used to control when an interstitial should or should not be displayed
     * If you should not display an interstitial, return FALSE
     *
     * For example: during gameplay, return FALSE.
     *
     * Is fired on:
     * - showInterstitial()
     * - Interstitial is loaded & ready to display
     */
    @Override
    public boolean shouldDisplayInterstitial(String location) {
        return true;
    }

    /*
     * shouldRequestInterstitial(String location)
     *
     * This is used to control when an interstitial should or should not be requested
     * If you should not request an interstitial from the server, return FALSE
     *
     * For example: user should not see interstitials for some reason, return FALSE.
     *
     * Is fired on:
     * - cacheInterstitial()
     * - showInterstitial() if no interstitial is cached
     *
     * Notes:
     * - We do not recommend excluding purchasers with this delegate method
     * - Instead, use an exclusion list on your campaign so you can control it on the fly
     */
    @Override
    public boolean shouldRequestInterstitial(String location) {
        return true;
    }

    /*
     * didCacheInterstitial(String location)
     *
     * Passes in the location name that has successfully been cached
     *
     * Is fired on:
     * - cacheInterstitial() success
     * - All assets are loaded
     *
     * Notes:
     * - Similar to this is: cb.hasCachedInterstitial(String location)
     * Which will return true if a cached interstitial exists for that location
     */
    @Override
    public void didCacheInterstitial(String location) {
       // Save which location is ready to display immediately
		chartboostOk = true;
    }

    /*
     * didFailToLoadInterstitial(String location)
     *
     * This is called when an interstitial has failed to load for any reason
     *
     * Is fired on:
     * - cacheInterstitial() failure
     * - showInterstitial() failure if no interstitial was cached
     *
     * Possible reasons:
     * - No network connection
     * - No publishing campaign matches for this user (go make a new one in the dashboard)
     */
    @Override
    public void didFailToLoadInterstitial(String location)
	{
		Log.e(TAG, "Chartboost didFailToLoadInterstitial " + location);
		//chartboostOk = false;
		tryAppLoving();
    }

    /*
     * didDismissInterstitial(String location)
     *
     * This is called when an interstitial is dismissed
     *
     * Is fired on:
     * - Interstitial click
     * - Interstitial close
     *
     * #Pro Tip: Use the code below to immediately re-cache interstitials
     */
    @Override
    public void didDismissInterstitial(String location)
	{
    }

    /*
     * didCloseInterstitial(String location)
     *
     * This is called when an interstitial is closed
     *
     * Is fired on:
     * - Interstitial close
     */
    @Override
    public void didCloseInterstitial(String location) {
        // Know that the user has closed the interstitial
		chartboostOk = false;
		//cb.cacheInterstitial();
    }

    /*
     * didClickInterstitial(String location)
     *
     * This is called when an interstitial is clicked
     *
     * Is fired on:
     * - Interstitial click
     */
    @Override
    public void didClickInterstitial(String location) {
        // Know that the user has clicked the interstitial
    }

    /*
     * didShowInterstitial(String location)
     *
     * This is called when an interstitial has been successfully shown
     *
     * Is fired on:
     * - showInterstitial() success
     */
    @Override
    public void didShowInterstitial(String location) {
        // Know that the user has seen the interstitial
    }

    /*
     * More Apps delegate methods
     */

    /*
     * shouldDisplayLoadingViewForMoreApps()
     *
     * Return FALSE to prevent the pretty More-Apps loading screen
     *
     * Is fired on:
     * - showMoreApps()
     */
    @Override
    public boolean shouldDisplayLoadingViewForMoreApps() {
        return true;
    }

    /*
     * shouldRequestMoreApps()
     *
     * Return FALSE to prevent a More-Apps page request
     *
     * Is fired on:
     * - cacheMoreApps()
     * - showMoreApps() if no More-Apps page is cached
     */
    @Override
    public boolean shouldRequestMoreApps() {
        return true;
    }

    /*
     * shouldDisplayMoreApps()
     *
     * Return FALSE to prevent the More-Apps page from displaying
     *
     * Is fired on:
     * - showMoreApps()
     * - More-Apps page is loaded & ready to display
     */
    @Override
    public boolean shouldDisplayMoreApps() {
        return true;
    }

    /*
     * didFailToLoadMoreApps()
     *
     * This is called when the More-Apps page has failed to load for any reason
     *
     * Is fired on:
     * - cacheMoreApps() failure
     * - showMoreApps() failure if no More-Apps page was cached
     *
     * Possible reasons:
     * - No network connection
     * - No publishing campaign matches for this user (go make a new one in the dashboard)
     */
    @Override
    public void didFailToLoadMoreApps()
	{
		if (Cocos2dxActivity.moreGamesWasTapped)
		{
			if (!_isAmazonApp)
			{
				Cocos2dxActivity.openUrl("market://search?q=pub:\"" + developerTitle() + "\"");
			}
			Cocos2dxActivity.moreGamesWasTapped = false;
		}
    }

    /*
     * didCacheMoreApps()
     *
     * Is fired on:
     * - cacheMoreApps() success
     * - All assets are loaded
     */
    @Override
    public void didCacheMoreApps() {
        // Know that the More-Apps page is cached and ready to display
    }

    /*
     * didDismissMoreApps()
     *
     * This is called when the More-Apps page is dismissed
     *
     * Is fired on:
     * - More-Apps click
     * - More-Apps close
     */
    @Override
    public void didDismissMoreApps() {
        // Know that the More-Apps page has been dismissed
    }

    /*
     * didCloseMoreApps()
     *
     * This is called when the More-Apps page is closed
     *
     * Is fired on:
     * - More-Apps close
     */
    @Override
    public void didCloseMoreApps() {
        // Know that the More-Apps page has been closed
    }

    /*
     * didClickMoreApps()
     *
     * This is called when the More-Apps page is clicked
     *
     * Is fired on:
     * - More-Apps click
     */
    @Override
    public void didClickMoreApps() {
        // Know that the More-Apps page has been clicked

    }

    /*
     * didShowMoreApps()
     *
     * This is called when the More-Apps page has been successfully shown
     *
     * Is fired on:
     * - showMoreApps() success
     */
    @Override
    public void didShowMoreApps() {
        // Know that the More-Apps page has been presented on the screen
    }

    /*
     * shouldRequestInterstitialsInFirstSession()
     *
     * Return FALSE if the user should not request interstitials until the 2nd startSession()
     *
     */
    @Override
    public boolean shouldRequestInterstitialsInFirstSession() {
        return true;
    }

    @Override
    public void didFailToLoadUrl(java.lang.String s) {
    }

  public void onAdColonyV4VCReward( AdColonyV4VCReward reward )
  {
    if (reward.success())
    {
		final int amount = reward.amount();
		Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable()
		{
			@Override
			public void run()
			{
				onVideoAdAward(amount);
			}
		});
    }
  }

  // Ad Started Callback - called only when an ad successfully starts playing
  public void onAdColonyAdStarted( AdColonyAd ad )
  {
	//Log.d("AdColony", "onAdColonyAdStarted");
  }

  //Ad Attempt Finished Callback - called at the end of any ad attempt - successful or not.

	private void doShowTapjoyInterstitial()
	{
		if (mHandler == null || sContext == null)
		{
			return;
		}
		if (tapjoyOk)
		mHandler.postDelayed(new Runnable()
		{
		    @Override
			public void run()
			{
				((Cocos2dxActivity)sContext).runOnUiThread(new Runnable()
				{
				    @Override
					public void run()
					{
						TapjoyConnect.getTapjoyConnectInstance().getFullScreenAd((Cocos2dxActivity)sContext);
					}
				});
			}
		}, 1000);
	}


	private void tryAppLoving()
	{
		AppLovinSdk sdk = AppLovinSdk.getInstance( (Cocos2dxActivity)sContext );
		AppLovinInterstitialAdDialog adDialog = AppLovinInterstitialAd.create(sdk, (Cocos2dxActivity)sContext);
		adDialog.setAdLoadListener( (Cocos2dxActivity)sContext );
		adDialog.show();
	}

	public void adReceived(AppLovinAd ad)
    {
		//Log.e(TAG, "APPLOVING AD OK");
    }

    public void failedToReceiveAd(int errorCode)
    {
		//Log.e(TAG, "APPLOVING AD FAILED; code:" + errorCode);
		if (204 == errorCode)
		{
		}
    }



  public void onAdColonyAdAttemptFinished( AdColonyAd ad )
  {
	if (ad.notShown())
	{
		if (ad.noFill())
		{
			if (Math.random() > 0.5)
			{
				openOfferWall();
			}
			else
			{
				tryAppLoving();
			}
		}
	 }
	// You can ping the AdColonyAd object here for more information:
	// ad.shown() - returns true if the ad was successfully shown.
	// ad.notShown() - returns true if the ad was not shown at all (i.e. if onAdColonyAdStarted was never triggered)
	// ad.skipped() - returns true if the ad was skipped due to an interval play setting
	// ad.canceled() - returns true if the ad was cancelled (either programmatically or by the user)
	// ad.noFill() - returns true if the ad was not shown due to no ad fill.
  }

  // Ad Availability Change Callback - update button text
	public void onAdColonyAdAvailabilityChange(boolean available, String zone_id)
	{

	}

	public void getFullScreenAdResponse()
	{
		TapjoyConnect.getTapjoyConnectInstance().showFullScreenAd();
	}

	public void getFullScreenAdResponseFailed(int error)
	{
	}

	private boolean chartboostOk = false;

}
