package org.cocos2dx.plugins;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.cocos2dx.lib.Cocos2dxActivity;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClient {

    private static final String PREFERENCE_NAME = "twitter_oauth";
    private static final String PREF_KEY_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TOKEN = "oauth_token";
    private static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";

    private static final String TAG = "TwitterClient";

    private String _consumerKey;
    private String _consumerSecret;
    private String _callbackUrl;

    private Cocos2dxActivity _activity;
    private final SharedPreferences _sharedPreferences;

    //these variables are only needed from AskOAuth to AskOAuthCallback
    private Twitter _twitter;
    private RequestToken _requestToken;
    private String _cachedMessage;
    private boolean _isAsyncMethodCalling = false;

    public TwitterClient(Cocos2dxActivity activity) {
        _activity = activity;
        _sharedPreferences = _activity.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
    }

    //NOTE:
    //callbackHost - must be unique among all android applications.
    //it has to be equal to host from from AndroidManifest.xml in <data android:scheme="oauth" android:host="twittercallback11"/>
    public void SetApplicationSettings(String consumerKey, String consumerSecret, String callbackHost) {
        //private static final String CONSUMER_KEY = "R5kbSKJSyAvwGoPhwSevuQ";
        //private static final String CONSUMER_SECRET = "ruhXo0jRrr08tdTx7YLTOp70RLi5xwHiolpA8lSlI8";
        //callbackHost = "oauth://twittercallback11"
        _consumerKey = consumerKey;
        _consumerSecret = consumerSecret;
        _callbackUrl = "oauth://" + callbackHost;
    }

    public void OnNewIntent(Intent intent) {
        AskOAuthCallback(intent);
    }

    private boolean IsConnected() {
        return _sharedPreferences.getString(PREF_KEY_TOKEN, null) != null;
    }

    private boolean IsHasConsumerKeyAndSecret() {
        if (_consumerKey == null || _consumerSecret == null) {
            Log.e(TAG, "IsHasConsumerKeyAndSecret NO Twitter consumerKey or consumerSecret");
            return false;
        }

        return true;
    }

    private void AskOAuth() {

        if (!IsHasConsumerKeyAndSecret())
            return;

        new AsyncTask<Void, Void, RequestToken>() {

            private Exception _exception;

            //invoked on UI thread
            @Override
            protected void onPreExecute() {
                _isAsyncMethodCalling = true;

                ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
                configurationBuilder.setOAuthConsumerKey(_consumerKey);
                configurationBuilder.setOAuthConsumerSecret(_consumerSecret);
                Configuration configuration = configurationBuilder.build();
                _twitter = new TwitterFactory(configuration).getInstance();
            }

            //invoked on the background thread
            @Override
            protected RequestToken doInBackground(Void... voids) {
                try {
                    RequestToken requestToken = _twitter.getOAuthRequestToken(_callbackUrl);
                    return requestToken;
                } catch (Exception ex) {

                    //NOTE: can be stupid error: twitter limits requests
                    //https://support.twitter.com/articles/15364-about-twitter-limits-update-api-dm-and-following
                    //https://dev.twitter.com/discussions/25148 - Upgrading twitter4j to the latest version is fixed my problem. Sorry for useless thread.

                    _exception = ex;
                    Log.e(TAG, " " + ex.getMessage());
                    ex.printStackTrace();
                }

                return null;
            }

            //invoked on UI thread
            @Override
            protected void onPostExecute(RequestToken requestToken) {
                if (requestToken == null || _exception != null) {
                    _activity.showDialog("Failure", "Error in authorizing app in twitter");
                    _isAsyncMethodCalling = false;
                    return;
                }

                _requestToken = requestToken;
                Toast.makeText(_activity, "Please authorize this app!", Toast.LENGTH_LONG).show();

                try {
                    _activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()))
                            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                _isAsyncMethodCalling = false;
            }

        }.execute();
    }

    private void AskOAuthCallback(Intent data)
    {
        if (data == null)
            return;

        //на старых андроидах (н-р, ZTE Racer - 256 Мб), где мало оперативной памяти, есть баг
        //вместо того, чтобы после странички твиттера возвращаться в уже запущенный инстанс игры,
        //запускается новый инстанс этой игры (т.к. запущенный инстанс игры уже убился системой ),
        //в котором, естественно, нет объектов _twitter и _requestToken
        if (_twitter == null) {
            Log.i(TAG, "AskOAuthCallback _twitter == null");
            return;
        }

        _isAsyncMethodCalling = true;
        Uri uri = data.getData();
        if (uri != null && uri.toString().startsWith(_callbackUrl)) {
            final String verifier = uri.getQueryParameter(IEXTRA_OAUTH_VERIFIER);

            new AsyncTask<Void, Void, AccessToken>() {

                @Override
                protected AccessToken doInBackground(Void... voids) {
                    try {
                        AccessToken accessToken = _twitter.getOAuthAccessToken(_requestToken, verifier);
                        return accessToken;
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                        ex.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(AccessToken accessToken) {
                    _isAsyncMethodCalling = false;
                    if (accessToken == null) {
                        Log.e(TAG, "AskOAuthCallback accessToken == null");
                        return;
                    }

                    SharedPreferences.Editor e = _sharedPreferences.edit();
                    e.putString(PREF_KEY_TOKEN, accessToken.getToken());
                    e.putString(PREF_KEY_SECRET, accessToken.getTokenSecret());
                    e.commit();

                    if (_cachedMessage != null) {  //resend tweet message
                        SendTweet(_cachedMessage, false);
                        _cachedMessage = null;
                    }
                }
            }.execute();
        }
    }

    public void SendTweet(final String message, boolean isCanAuth) {
        if (_isAsyncMethodCalling)
            return;

        if (!IsConnected()) {
            if (isCanAuth) {
                _cachedMessage = message;
                AskOAuth();
            }
            return;
        }

        _isAsyncMethodCalling = true;

        final String token = _sharedPreferences.getString(PREF_KEY_TOKEN, "");
        final String secret = _sharedPreferences.getString(PREF_KEY_SECRET, "");
        Toast.makeText(_activity, "Please wait...", Toast.LENGTH_LONG).show();

        new AsyncTask<Void, Void, Boolean>() {

            private TwitterException _exception;

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    AccessToken accessToken = new AccessToken(token, secret);
                    final Twitter twitter = new TwitterFactory().getInstance();
                    twitter.setOAuthConsumer(_consumerKey, _consumerSecret);
                    twitter.setOAuthAccessToken(accessToken);

                    twitter.updateStatus(message);
                    return true;
                } catch (TwitterException ex) {
                    Log.i(TAG, ex.toString());
                    _exception = ex;
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                _isAsyncMethodCalling = false;
                if (result)
                    _activity.showDialog("Success", "Tweet is successfully posted");
                else
                    _activity.showDialog("Failure", "Tweet is not posted, because of error. \n"
                            + (_exception == null ? "" : _exception.getErrorMessage()));
            }
        }.execute();
    }

}
