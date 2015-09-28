package com.performanceactive.plugins.camera;

import static com.performanceactive.plugins.camera.CustomCameraActivity.FILENAME;
import static com.performanceactive.plugins.camera.CustomCameraActivity.QUALITY;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.venkykowshik.squarecamera.CameraActivity;


public class CustomCamera extends CordovaPlugin {

    private CallbackContext callbackContext;

	@Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
	    if (!hasRearFacingCamera()) {
	        callbackContext.error("No rear camera detected");
	        return false;
	    }
	    this.callbackContext = callbackContext;
	    Context context = this.cordova.getActivity();
	    Toast.makeText(context, "Testing Camera", Toast.LENGTH_SHORT).show();
	   
	    Intent intent = new Intent(context, CameraActivity.class);
	    intent.putExtra(FILENAME, args.getString(0));
	    intent.putExtra(QUALITY, args.getInt(1));
	    cordova.startActivityForResult(this, intent, 0);
	    Toast.makeText(context, "Opend Camera", Toast.LENGTH_SHORT).show();
        return true;
    }

	private boolean hasRearFacingCamera() {
	    Context context = cordova.getActivity().getApplicationContext();
	    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

//	@Override
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//	    if (resultCode == Activity.RESULT_OK) {
//	        callbackContext.success(intent.getExtras().getString(IMAGE_URI));
//	    } else {
//	    	String errorMessage = null;
//	    	if (intent != null) {
//	    		 errorMessage = intent.getExtras().getString(ERROR_MESSAGE);
//			} 
//	        if (errorMessage != null) {
//	            callbackContext.error(errorMessage);
//	        } else {
//	            callbackContext.error("Failed to take picture");
//	        }
//	    }
//    }

	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (resultCode != Activity.RESULT_OK) return;

	        if (data.getExtras() != null) {
	        	callbackContext.error("Failed to take picture");
            } else {
                Uri photoUri = data.getData();
                callbackContext.success(photoUri.toString());
            }

	        super.onActivityResult(requestCode, resultCode, data);
	    }
}
