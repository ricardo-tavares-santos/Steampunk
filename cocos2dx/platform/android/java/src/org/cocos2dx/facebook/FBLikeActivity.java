package org.cocos2dx.facebook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.cocos2dx.lib.R;

public class FBLikeActivity extends Activity {

    private WebView _webView;
    private Boolean _isLiked = null;
    private String _pageId;

    /* Constants */
    private static final String FB_PAGE_PREFIX = "http://m.facebook.com/";
    public static final String INTENT_FB_LIKE = "like";
    public static final String INTENT_FB_PAGE_ID = "pageid";
    public static final int LIKE_ACTIVITY_RESULT_CODE = 1009;

    private final String _likeURL = "?fan&";
    private final String _unlikeURL = "?unfan&";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fblike);

        //_isLiked = getIntent().getExtras().getBoolean(INTENT_FB_LIKE);

        _webView = (WebView)findViewById(R.id.webview);
        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.setWebViewClient(new FacebookWebViewClient());

        //example: _pageId = "512348648783389" - SpaceFlip;
        _pageId = getIntent().getExtras().getString(INTENT_FB_PAGE_ID);
        String url = FB_PAGE_PREFIX + _pageId;
        _webView.loadUrl(url);
    }

    class FacebookWebViewClient extends WebViewClient {

        @Override
        public void onFormResubmission(WebView view, Message dontResend,
                                       Message resend) {
            super.onFormResubmission(view, dontResend, resend);
            Log.e("FacebookWebViewClient", "FacebookWebViewClient");
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.e("onLoadResource", url);
			/* Check url to check if user clicked 'like' or 'unlike' button.*/
            boolean following = url.indexOf(_likeURL) > -1;
            boolean unFollowing = url.indexOf(_unlikeURL) > -1;
			/* More, check if user clicked 'like' button with our facebook page.
			 * This prevents user from clicking other facebook page, not our page.*/
            boolean isOurPage = url.indexOf(_pageId) > -1;

            if (!isOurPage)
                return;
            if (following) {
                Log.i("like", "liked");
                _isLiked = true;
                Toast.makeText(getApplicationContext(),
                        "You have just selected 'like' ",
                        Toast.LENGTH_LONG).show();
                //you liked
            } else if (unFollowing) {
                Log.i("like", "unliked");
                _isLiked = false;
                Toast.makeText(getApplicationContext(),
                        "You have just selected 'unlike' ",
                        Toast.LENGTH_LONG).show();

            } else {
                // other operations
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e("onPageFinished", "onPageFinished");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e("onPageStarted", "onPageStarted");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.e("onReceivedError", "onReceivedError");
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
            Log.e("onUnhandledKeyEvent", "onUnhandledKeyEvent");
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            Log.e("shouldOverrideKeyEvent", "shouldOverrideKeyEvent");
            return super.shouldOverrideKeyEvent(view, event);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("shouldOverrideUrlLoading", url);
//			return super.shouldOverrideUrlLoading(view, url);
            return false;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_fblike, menu);
        return true;
    }

    /** Allow user to navigate our facebook page.
     * If you don't override, android's back key will just lead user to go back to the main activity. */
    @Override
    public void onBackPressed(){
        if (!_webView.canGoBack()){
            Intent intent = getIntent();

            if (_isLiked != null)
                intent.putExtra(INTENT_FB_LIKE, _isLiked.booleanValue());

            setResult(RESULT_OK, intent);
            finish();
        }
        else{
            _webView.goBack();
        }
    }
}
