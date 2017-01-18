/**
 * Author: Sunil Kumar
 * Publish Date: 02-01-2017
 * Name: Jack Reader
 * Version: 1.0.0
 */
package com.deftsoft.org.jack.reader;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.square.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class JackReader extends CordovaPlugin {
	private CallbackContext callbackContext;  
    private static final String LOG_TAG = "CardReaderPlugin";
    public static final int     UNKNOWN_ERROR = 0;
    private MagRead readCard;
    public static final String AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final int REQ_CODE = 1;
   
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	this.callbackContext = callbackContext;
        if(readCard != null){
            readCard.stop();
            readCard.release();
            readCard = null;
        }

        if (action.equals("ReadCard")) {
            if(PermissionHelper.hasPermission(this, AUDIO)) {
                readCard();
            } else {
                getReadPermission(REQ_CODE);
            }
        }

        return true;
    }

    private void readCard(){
        readCard = new MagRead();
        readCard.addListener(new MagReadListener() {
            @Override
            public void updateBits(String s) {

            }

            @Override
            public void updateBytes(String bytes) {
                try {
                    Log.v(LOG_TAG, "UpdateBytes received" + bytes);
                    CardResult scanResult = getCardDetails(bytes);
                    if (scanResult != null) {
                        final JSONObject result = new JSONObject();
                        result.put("card_number", scanResult.getCardNumber());
                        result.put("expiry_month", scanResult.getExpiryMonth());
                        result.put("expiry_year", "20"+scanResult.getExpiryYear());
                        callbackContext.success(result);
                    }else{
                        Log.e(LOG_TAG, "Error reading Card");
                        callbackContext.success("Error reading Card");
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error reading Card: " + e.getMessage());
                    callbackContext.success(e.getMessage());
                }
            }
        });
        readCard.start();
    }
    
    private CardResult getCardDetails(String bytes) {
        CardResult result = null;
        try {
            result = new CardResult();
            StringBuffer cardNumber = new StringBuffer();
            int i = 0;

            // Find Card Number
            for (i = 1; i < bytes.length(); i++) {
                if (bytes.charAt(i) != '=') {
                    cardNumber.append(bytes.charAt(i));
                } else {
                    i++;
                    break;
                }
            }

            // Find expiry Year
            StringBuffer expiryYear = new StringBuffer();
            expiryYear.append(bytes.charAt(i));
            expiryYear.append(bytes.charAt(++i));

            // Find expiry Month
            StringBuffer expiryMonth = new StringBuffer();
            expiryMonth.append(bytes.charAt(++i));
            expiryMonth.append(bytes.charAt(++i));

            result.setCardNumber(cardNumber.toString());
            result.setExpiryMonth(Integer.parseInt(expiryMonth.toString()));
            result.setExpiryYear(Integer.parseInt(expiryYear.toString()));
        } catch (Exception e) {
            callbackContext.success("Please try again!");
        }
        return result;
    }

    private void getReadPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, AUDIO);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for(int r:grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.success("Permission Denied");
                return;
            }
        }
        switch(requestCode) {
            case REQ_CODE:
                readCard();
                break;
        }
    }

}

