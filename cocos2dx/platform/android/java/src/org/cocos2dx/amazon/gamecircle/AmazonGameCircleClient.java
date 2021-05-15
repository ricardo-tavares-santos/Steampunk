package org.cocos2dx.amazon.gamecircle;

import android.util.Log;
import com.amazon.ags.api.*;
import com.amazon.ags.api.achievements.AchievementsClient;
import com.amazon.ags.api.achievements.UpdateProgressResponse;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;
import com.amazon.ags.api.overlay.PopUpLocation;
import org.cocos2dx.lib.Cocos2dxActivity;

import java.text.MessageFormat;
import java.util.EnumSet;

//NOTE: all calls to AmazonGameCircleClient must be from runOnUiThread
public class AmazonGameCircleClient implements AmazonGamesCallback {
    private static final String Tag = "Cocos2dx Amazon GameCircle";

    private final Cocos2dxActivity _activity;
    private AmazonGamesClient _amazonClient;
    private AmazonGameCircleState _state = AmazonGameCircleState.NotConstructed;
    private final boolean _isEnabled = true; //for test purpose

    enum AmazonGameCircleState {
        NotConstructed,
        Constructed,
        Initializing,
        InitializedSuccessfully,
        InitializedWithError
    }

    public AmazonGameCircleClient(Cocos2dxActivity activity) {
        _activity = activity;
    }

    public void Init() {
        _state = AmazonGameCircleState.Constructed;
        OnResume();
    }

    public void OnResume() {
        if (_state == AmazonGameCircleState.NotConstructed)
            return;

        //init amazon client
        _state = AmazonGameCircleState.Initializing;
        EnumSet<AmazonGamesFeature> features = EnumSet.of(
                AmazonGamesFeature.Achievements, AmazonGamesFeature.Leaderboards);
        AmazonGamesClient.initialize(_activity, this, features);
    }

    public void OnPause() {
        if (_amazonClient != null) {
            AmazonGamesClient.release();
            _amazonClient = null;
            _state = AmazonGameCircleState.Constructed;
        }
    }

    public void OnDestroy() {
        if (_amazonClient != null) {
            AmazonGamesClient.shutdown();
            _amazonClient = null;
            _state = AmazonGameCircleState.Constructed;
        }
    }

    private AmazonGamesClient GetInitializedAmazonWithMessageBox() {
        switch (_state) {
            case Initializing:
                _activity.showDialog("GameCircle", "GameCircle is initializing. Wait");
                return null;
            case InitializedWithError:
                _activity.showDialog("GameCircle Error", "GameCircle's initialization's error");
                return null;
            case InitializedSuccessfully:
                return _amazonClient;
            default:
                _activity.showDialog("GameCircle Error", "GameCircle's strange error. state = " + _state.toString());
                return null;
        }
    }

    //------------Achievements-----------

    public void ReportAchievement(final String achievementId, final int percent) {
        if (!_isEnabled)
            return;

        if (_amazonClient == null)
            return;

        Log.i(Tag, "ReportAchievement try: " + achievementId + " " + percent);
        AchievementsClient acClient = _amazonClient.getAchievementsClient();
        AGResponseHandle<UpdateProgressResponse> handle = acClient.updateProgress(achievementId, percent);
        handle.setCallback(new AGResponseCallback<UpdateProgressResponse>() {
            @Override
            public void onComplete(UpdateProgressResponse result) {
                if (result.isError()) {
                    // Add optional error handling here.  Not strictly required
                    // since retries and on-device request caching are automatic.
                    Log.e(Tag, "ReportAchievement error:" + result.getError());
                } else {
                    Log.i(Tag, "ReportAchievement success: " + achievementId + " " + percent);
                    _activity.runOnGLThread(new Runnable() { @Override public void run() {
                        _activity.onAchievementReported(achievementId, percent);
                    }});
                }
            }
        });
    }

    public void ShowAchievements() {
        if (!_isEnabled)
            return;

        final AmazonGamesClient amazonClient = GetInitializedAmazonWithMessageBox();
        if (amazonClient == null)
            return;

        AchievementsClient acClient = amazonClient.getAchievementsClient();
        AGResponseHandle<RequestResponse> responce = acClient.showAchievementsOverlay();
        responce.setCallback(new AGResponseCallback<RequestResponse>() {
            @Override
            public void onComplete(RequestResponse requestResponse) {
                Log.i(Tag, "ShowAchievements complete");
            }
        });
    }

    //--------Leaderboards------------

    public void ShowLeaderboard(final String leaderboardId) {
        if (!_isEnabled)
            return;

        final AmazonGamesClient amazonClient = GetInitializedAmazonWithMessageBox();
        if (amazonClient == null)
            return;

        amazonClient.getLeaderboardsClient().showLeaderboardOverlay(leaderboardId);
    }

    public void SubmitScore(final String leaderboardId, final int score) {
        if (!_isEnabled)
            return;

        if (_amazonClient == null)
            return;

        Log.i(Tag, "SubmitScore try: " + leaderboardId + " " + score);
        AGResponseHandle<SubmitScoreResponse> responce =
            _amazonClient.getLeaderboardsClient().submitScore(leaderboardId, score);
        responce.setCallback(new AGResponseCallback<SubmitScoreResponse>() {
            @Override
            public void onComplete(SubmitScoreResponse submitScoreResponse) {
                if (submitScoreResponse.isError()) {
                    Log.e(Tag, "SubmitScore error:" + submitScoreResponse.getError());
                } else {
                    Log.i(Tag, "SubmitScore succesfull: " + leaderboardId + " " + score);
                    _activity.runOnGLThread(new Runnable() { @Override public void run() {
                        _activity.onScoreSubmitted(leaderboardId, score);
                    }});
                }
            }
        });
    }

    //-----------AmazonGamesCallback------------
    private void OnServiceAuthorized(final Boolean isAuthorized) {
        _activity.runOnGLThread(new Runnable() { @Override public void run() {
            if (isAuthorized)
                _activity.onGameCenterAuth();
            else
                _activity.onGameCenterAuthFailed();
        }});
    }

    @Override
    public void onServiceReady(AmazonGamesClient amazonGamesClient) {
        _amazonClient = amazonGamesClient;
        _amazonClient.setPopUpLocation(PopUpLocation.TOP_CENTER);
        _state = AmazonGameCircleState.InitializedSuccessfully;
        OnServiceAuthorized(true);
    }

    //https://developer.amazon.com/sdk/gamecircle/documentation/gamecircle-init.html
    @Override
    public void onServiceNotReady(AmazonGamesStatus amazonGamesStatus) {
        _state = AmazonGameCircleState.InitializedWithError;
        Log.i(Tag, MessageFormat.format("onServiceNotReady = {0}", amazonGamesStatus));

        if (amazonGamesStatus == AmazonGamesStatus.CANNOT_INITIALIZE) {
            Log.i(Tag, "you probably haven’t properly registered the package name and signature of the game build");
        }

        OnServiceAuthorized(false);
    }

    //------------------------------------------
}
