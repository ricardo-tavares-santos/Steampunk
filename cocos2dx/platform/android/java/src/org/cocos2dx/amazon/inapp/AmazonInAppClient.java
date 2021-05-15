package org.cocos2dx.amazon.inapp;

import android.util.Log;
import com.amazon.inapp.purchasing.*;
import com.amazon.inapp.purchasing.GetUserIdResponse.GetUserIdRequestStatus;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse.PurchaseUpdatesRequestStatus;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

//https://developer.amazon.com/sdk/in-app-purchasing/documentation/quick-start.html?preferredLanguage=en_US
public class AmazonInAppClient extends BasePurchasingObserver {

    public static final String TAG = "Cocos2dx Amazon Inapp";
    private final Cocos2dxActivity _activity;
    private String _userId;
    private AppPurchasingObserver.PurchaseDataStorage _purchaseDataStorage;
    private String _lastSkuThatBuy;
    private boolean _isInited = false;

    public AmazonInAppClient(Cocos2dxActivity activity) {
        super(activity);
        _activity = activity;
    }

    public void Init() {
        Log.i(TAG, "Init");
        _isInited = true;
        _purchaseDataStorage = new AppPurchasingObserver.PurchaseDataStorage(_activity);
        PurchasingManager.registerObserver(this);
        OnResume();
    }

    public void OnResume() {
        if (!_isInited) {
            return;
        }

        Log.i(TAG, "onResume: call initiateGetUserIdRequest");
        PurchasingManager.initiateGetUserIdRequest();

        Log.i(TAG, "onResume: call initiateItemDataRequest for skus: ");
        ArrayList<String> skus = _activity.GetInAppSkus();
        PurchasingManager.initiateItemDataRequest(new HashSet<String>(skus));
    }

    public void Buy(String sku) {
        _lastSkuThatBuy = sku;
        String requestId = PurchasingManager.initiatePurchaseRequest(sku);
        Log.i(TAG, MessageFormat.format("Try buy sku = '{0}' and requestId = {1}", sku, requestId));
    }

    public void RestorePurchases() {
        Log.i(TAG, "RestorePurchases");
        PurchasingManager.initiatePurchaseUpdatesRequest(_purchaseDataStorage.getPurchaseUpdatesOffset());
    }

    private void CompletePurchase(final String sku, final boolean isSuccess) {
        Cocos2dxHelper.sCocos2dxHelperListener.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                _activity.onPurchase(sku, isSuccess ? 1 : 0);
            }
        });
    }


    //-----------BasePurchasingObserver-----
    @Override
    public void onSdkAvailable(boolean isSandboxMode) {
        Log.i(TAG, MessageFormat.format("onSdkAvailable:{0}", isSandboxMode));
    }

    @Override
    public void onGetUserIdResponse(final GetUserIdResponse response) {
        if (response.getUserIdRequestStatus() == GetUserIdRequestStatus.FAILED) {
            Log.i(TAG, "onGetUserIdResponse Failed");
            return;
        }

        Log.i(TAG, "onGetUserIdResponse Success");
        _userId = response.getUserId();
        _purchaseDataStorage.saveCurrentUser(_userId);
        //PurchasingManager.initiatePurchaseUpdatesRequest(_purchaseDataStorage.getPurchaseUpdatesOffset());
    }

    @Override
    public void onItemDataResponse(final ItemDataResponse response) {
        if (response.getItemDataRequestStatus() == ItemDataResponse.ItemDataRequestStatus.FAILED) {
            Log.i(TAG, "onItemDataResponse Failed");
            return;
        }

        if (response.getItemDataRequestStatus() == ItemDataResponse.ItemDataRequestStatus.SUCCESSFUL_WITH_UNAVAILABLE_SKUS) {
            for (final String sku : response.getUnavailableSkus()) {
                Log.v(TAG, "Unavailable SKU:" + sku);
            }
        }

        //success
        Log.i(TAG, "onItemDataResponse Success");
        final Map<String, Item> items = response.getItemData();
        for (final String key : items.keySet()) {
            Item i = items.get(key);
            _activity.reportInApp(i.getSku());
            Log.v(TAG, String.format("Item: %s\n Type: %s\n SKU: %s\nPrice: %s\n Description: %s\n",
                    i.getTitle(),i.getItemType(), i.getSku(), i.getPrice(), i.getDescription()));
        }

        //restore purchases after initiation
        RestorePurchases();
    }

    //for non consumable items
    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {

        if (purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus() == PurchaseUpdatesRequestStatus.FAILED) {
            Log.i(TAG, "onPurchaseUpdatesResponse Failed");
            return;
        }

        Log.i(TAG, "onPurchaseUpdatesResponse Success");

        //request more PurchaseUpdates
        Offset offset = _purchaseDataStorage.getPurchaseUpdatesOffset();
        _purchaseDataStorage.savePurchaseUpdatesOffset(offset);
        if (purchaseUpdatesResponse.isMore()) {
            PurchasingManager.initiatePurchaseUpdatesRequest(_purchaseDataStorage.getPurchaseUpdatesOffset());
        }

        //restore purchases
        for (Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
            if (receipt.getItemType() == Item.ItemType.ENTITLED) {
                CompletePurchase(receipt.getSku(), true);
            }
        }
    }


    @Override
    public void onPurchaseResponse(PurchaseResponse response) {
        String userId = response.getUserId();
        PurchaseResponse.PurchaseRequestStatus status = response.getPurchaseRequestStatus();

        if (!_purchaseDataStorage.isSameAsCurrentUser(userId)) {
            Log.i(TAG, MessageFormat.format(
                    "onPurchaseResponse: userId ({0}) in response is NOT the same as current user!", userId));
        }

        if (status != PurchaseResponse.PurchaseRequestStatus.SUCCESSFUL) {
            Log.i(TAG, MessageFormat.format("onPurchaseResponse fail: {0}, ", status));
            CompletePurchase(_lastSkuThatBuy, false);
            return;
        }

        //
        Receipt receipt = response.getReceipt();
        Log.i(TAG, MessageFormat.format("onPurchaseResponse success sku = {0}", receipt.getSku()));
        CompletePurchase(receipt.getSku(), true);
    }
}
