package org.cocos2dx.amazon;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.amazon.device.ads.*;
import org.cocos2dx.lib.Cocos2dxActivity;

public class AmazonAdClient implements AdListener {

    private final Cocos2dxActivity _activity;
    private AdLayout _adView;
    private static final String TAG = "AmazonAdClient";
    private boolean _isVisible = true;

    public AmazonAdClient(Cocos2dxActivity activity) {
        _activity = activity;
    }

    public AdLayout GetAdView() {
        return _adView;
    }

    public void OnCreate() {
        // For debugging purposes enable logging, but disable for production builds
        //AdRegistration.enableLogging(true);
        // For debugging purposes flag all ad requests as tests, but set to false for production builds
        //AdRegistration.enableTesting(true);

        _adView = new AdLayout(_activity, AdSize.SIZE_AUTO);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        _adView.setLayoutParams(layoutParams);
        _adView.setListener(this);
        if (!_isVisible)
            _adView.setVisibility(View.GONE);

        try {
            //String amazonApplicationKey = "sample-app-v1_pub-2";  //sample application key
            String amazonApplicationKey = _activity.mediationId();
            Log.i(TAG, "amazonApplicationKey = " + amazonApplicationKey);
            AdRegistration.setAppKey(amazonApplicationKey);
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown: " + e.toString());
            return;
        }

        LoadAd();
    }

    /**
     * Load a new ad.
     */
    private void LoadAd() {
        // Load the ad with the appropriate ad targeting options.
        AdTargetingOptions adOptions = new AdTargetingOptions();
        _adView.loadAd(adOptions);
    }

    public void ShowAds(boolean isVisible) {
        _isVisible = isVisible;
        if (_adView == null)
            return;

        Log.i(TAG, "ShowAds = " + isVisible);
        _adView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        if (isVisible)
            LoadAd();
        else
            _adView.collapseAd();
    }

    public void Destroy() {
        Log.i(TAG, "Destroy adView");
        if (_adView != null) {
            _adView.destroy();
            _adView = null;
        }
    }


    //-----------------AdListener------------------

    /**
     * This event is called once an ad loads successfully.
     */
    @Override
    public void onAdLoaded(AdLayout adLayout, AdProperties adProperties) {
        Log.d(TAG, adProperties.getAdType().toString() + " Ad loaded successfully.");
    }

    /**
     * This event is called if an ad fails to load.
     */
    @Override
    public void onAdFailedToLoad(AdLayout adLayout, AdError adError) {
        Log.w(TAG, "Ad failed to load. Code: " + adError.getCode() + ", Message: " + adError.getMessage());
    }

    /**
     * This event is called after a rich media ad expands.
     */
    @Override
    public void onAdExpanded(AdLayout adLayout) {
        Log.d(TAG, "Ad expanded.");
    }

    /**
     * This event is called after a rich media ads has collapsed from an expanded state.
     */
    @Override
    public void onAdCollapsed(AdLayout adLayout) {
        Log.d(TAG, "Ad collapsed.");
    }
}
