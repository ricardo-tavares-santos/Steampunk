package org.cocos2dx.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.*;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FacebookCustomClient {

    private static final String TAG = "FacebookCustomClient";
    private Cocos2dxActivity _activity;
    private String _cachedFacebookLike = null;
    private String _cachedPageIdForLike = null;
    private String _cachedCheckLikeForPageId = null;

    public FacebookCustomClient(Cocos2dxActivity activity) {
        _activity = activity;
    }

    //------------

    //залайкать пост или коммент к посту. Но не страничку!
    //So, you can not like page "https://www.facebook.com/SpaceFlipGame"!
    public void PostLikePostOrComment(String likeUrl) {
        if (!_activity.prepareToPost()) {
            _cachedFacebookLike = likeUrl;
            return;
        }

        DoPostLikePostOrComment(likeUrl);
    }

    //NOTE: Этот метод пока что с захаркодеными id-шниками.
    private void DoPostLikePostOrComment(final String likeUrl) {
        if (!_activity.prepareToPost()) {
            return;
        }
        final Bundle postParams = new Bundle();

        //Этим самым можно только
        String s = "{\n" +
                "   \"title\": \"Some title\",\n" +
                "   \"id\": \"484068161664685\",\n" +
                "   \"url\": \"http://www.facebook.com/SpaceFlipGame/posts/484068161664685\",\n" +
                "}";

        //postParams.putString("object", "http://www.facebook.com/SpaceFlipGame/posts/484068161664685111q1");
        //postParams.putString("object", s);

        //"SpaceFlipGame/likes"
        final Request.Callback callback = new Request.Callback()
        {
            public void onCompleted(Response response)
            {
                if (response.getGraphObject() == null) {
                    Log.e(TAG, response.toString());
                    return;
                }

                JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                String postId = null;
                try {
                    postId = graphResponse.getString("id");
                }
                catch (JSONException e) {
                    Log.i(TAG, "JSON error "+ e.getMessage());
                }
                FacebookRequestError error = response.getError();
                if (error != null) {
                    Log.i(TAG, "error "+ error);
                }
            }
        };

        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String accessToken = Session.getActiveSession().getAccessToken();

                Request request = new Request(Session.getActiveSession(), /*"me/og.likes"*/"512348648783389/likes",
                        postParams, HttpMethod.POST, callback);
                RequestAsyncTask task = new RequestAsyncTask(request);
                task.execute();
            }
        });

        _cachedFacebookLike = null;
    }

    //--------------

    public void OpenPage(final String pageId) {
        _activity.runOnUiThread(new Runnable() { @Override public void run() {
            if (!_activity.prepareToPost()) {
                _cachedPageIdForLike = pageId;
                return;
            }

            DoOpenPage(pageId);
        }});
    }

    //открыть фейсбук-страничку, например https://www.facebook.com/SpaceFlipGame
    //pageid - id странички
    private void DoOpenPage(final String pageId) {
        if (!_activity.prepareToPost()) {
            return;
        }

        Intent intent = new Intent(_activity, FBLikeActivity.class);
        //intent.putExtra(FBLikeActivity.INTENT_FB_LIKE, false);
        intent.putExtra(FBLikeActivity.INTENT_FB_PAGE_ID, pageId);
        _activity.startActivityForResult(intent, FBLikeActivity.LIKE_ACTIVITY_RESULT_CODE);

        _cachedPageIdForLike = null;
    }

    public void OnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FBLikeActivity.LIKE_ACTIVITY_RESULT_CODE)
            return;

        if (resultCode == _activity.RESULT_OK){

            //если юзер нажал на лайк или дизлайк
            if (data.getExtras().containsKey(FBLikeActivity.INTENT_FB_LIKE))
            {
                boolean isHasLike = data.getExtras().getBoolean(FBLikeActivity.INTENT_FB_LIKE);
                Log.i(TAG, "isLiked = " + isHasLike);
                //пока что используем тот же callback, что и в CheckIfPageHasLike
                _activity.onFacebookCheckPageLikeCallback(isHasLike);
            }
            else  //если юзер не нажал на лайк-дизлайк, то вызываем CheckIfPageHasLike
            {
                String pageId = data.getExtras().getString(FBLikeActivity.INTENT_FB_PAGE_ID);;
                CheckIfPageHasLike(pageId);
            }
        }
    }

    //------------------

    public void CheckIfPageHasLike(final String pageId) {
        if (!_activity.prepareToPost()) {
            _cachedCheckLikeForPageId = pageId;
            return;
        }

        DoCheckIfPageHasLike(pageId);
    }

    private void DoCheckIfPageHasLike(final String pageId) {
        if (!_activity.prepareToPost()) {
            return;
        }

        final Request.Callback callback = new Request.Callback()
        {
            public void onCompleted(Response response)
            {
                if (response.getGraphObject() == null) {
                    Log.e(TAG, response.toString());
                    return;
                }

                FacebookRequestError error = response.getError();
                if (error != null) {
                    Log.i(TAG, "error "+ error);
                    return;
                }

                String receivedPageId = "";

                try {
                    JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                    JSONArray dataArray = graphResponse.getJSONArray("data");
                    receivedPageId = dataArray.getJSONObject(0).getString("page_id");
                } catch (JSONException ex) {
                    //no such key
                }

                boolean isHasLike = (receivedPageId.equals(pageId));
                _activity.onFacebookCheckPageLikeCallback(isHasLike);
            }
        };


        //all page's likes:
        //https://graph.facebook.com/me/likes

        //FQL:
        //https://developers.facebook.com/docs/android/run-fql-queries/
        //https://developers.facebook.com/tools/explorer  - tool for testing FQL
        //https://developers.facebook.com/docs/technical-guides/fql/
        //https://developers.facebook.com/docs/reference/fql/

        final String fqlQuery = String.format(
                "SELECT page_id FROM page_fan WHERE uid = me() AND page_id = %s", pageId);
        final Bundle params = new Bundle();
        params.putString("q", fqlQuery);

        _activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request(Session.getActiveSession(), "/fql",
                        params, HttpMethod.GET, callback);
                RequestAsyncTask task = new RequestAsyncTask(request);
                task.execute();
            }
        });

        _cachedCheckLikeForPageId = null;
    }

    //-----------------

    public static boolean IsSessionActive() {
        Session session = Session.getActiveSession();
        if (session == null)
            return false;

        boolean isHasPermission = session.getPermissions().contains("publish_actions");
        return isHasPermission;
    }

    public void RepeatTryToPostCachedInfo(final int repeatCount) {
        if (repeatCount == 0)
            return;

        if (_cachedFacebookLike == null && _cachedPageIdForLike == null && _cachedCheckLikeForPageId == null)
            return;

        if (IsSessionActive()) {
            TryToPostCachedInfo();
        } else {
            _activity.mHandler.postDelayed(new Runnable() { @Override public void run() {
                RepeatTryToPostCachedInfo(repeatCount - 1);
            }}, 3000);
        }
    }

    public void TryToPostCachedInfo() {
        if (_cachedFacebookLike != null) {
            DoPostLikePostOrComment(_cachedFacebookLike);
        } else if (_cachedPageIdForLike != null) {
            DoOpenPage(_cachedPageIdForLike);
        } else if (_cachedCheckLikeForPageId != null)
            DoCheckIfPageHasLike(_cachedCheckLikeForPageId);
    }

    public void RepeatTryToPostFacebookPost(final int repeatCount) {
        if (repeatCount == 0)
            return;

        if (_activity.cachedFacebookPost == null && _activity.cachedFacebookImagePost == null)
            return;

        if (IsSessionActive()) {

            if (_activity.cachedFacebookPost != null)
                _activity.doFacebookPostWall(_activity.cachedFacebookPost);
            else if (_activity.cachedFacebookImagePost != null)
                _activity.doFacebookImagePost(_activity.cachedFacebookImagePost);

        } else {
            _activity.mHandler.postDelayed(new Runnable() { @Override public void run() {
                RepeatTryToPostFacebookPost(repeatCount - 1);
            }}, 3000);
        }
    }
}
