/**
 * Author: Sunil Kumar
 * Publish Date: 02-01-2017
 * Name: Jack Reader
 * Version: 1.0.0
 */
package com.betasoft.org.jack.reader;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.square.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

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
    String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS};
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    	this.callbackContext = callbackContext;
        if(readCard != null){
            readCard.stop();
            readCard.release();
            readCard = null;
        }

        if (action.equals("ReadCard")) {

            if(!hasPermissions(this.cordova.getActivity(), PERMISSIONS)){
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.cordova.getActivity() != null && PERMISSIONS != null) {
                    this.cordova.getActivity().requestPermissions(PERMISSIONS, REQ_CODE);
                }
            }else{
                readCard();
            }
         /*   if(PermissionHelper.hasPermission(this, AUDIO)) {
                readCard();
            } else {
                getReadPermission(REQ_CODE);
            }*/
        }

        return true;
    }

    private void readCard(){
        AudioManager am=(AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
        if(!am.isWiredHeadsetOn()) {
            Toast.makeText(cordova.getActivity(), "Swiper Not Connected", Toast.LENGTH_SHORT).show();
            callbackContext.success("NoSwiper");
        }else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
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
                            result.put("expiry_year", "20" + scanResult.getExpiryYear());
                            callbackContext.success(result);
                        } else {
                            Log.e(LOG_TAG, "Error reading Card");
                            Toast.makeText(cordova.getActivity(), "Invalid Swipe. Please try again.", Toast.LENGTH_SHORT).show();
                            callbackContext.success("Error reading Card");
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error reading Card: " + e.getMessage());
                        Toast.makeText(cordova.getActivity(), "Invalid Swipe. Please try again.", Toast.LENGTH_SHORT).show();
                        callbackContext.success(e.getMessage());
                    }
                }
            });
            readCard.start();
        }
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

    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (this.cordova.getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}

