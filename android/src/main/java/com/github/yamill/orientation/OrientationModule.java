package com.github.yamill.orientation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class OrientationModule extends ReactContextBaseJavaModule implements LifecycleEventListener{
    OrientationEventListener eventListener;
    String lastOrientation;
    int currentRotation = 0;

    public OrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        final ReactApplicationContext ctx = reactContext;
        if (eventListener != null) eventListener.disable();
        eventListener = new OrientationEventListener(reactContext) {
            @Override
            public void onOrientationChanged(int i) {
                currentRotation = i;
                String specificOrientation = getSpecificOrientationString(currentRotation);
                if (!specificOrientation.equals(lastOrientation)) {
                    lastOrientation = specificOrientation;
                    WritableMap specificParams = Arguments.createMap();
                    specificParams.putString("specificOrientation", specificOrientation);
                    if (ctx.hasActiveCatalystInstance()) {
                        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("specificOrientationDidChange", specificParams);
                    }
                }

            }
        };
        eventListener.enable();
        ctx.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    private String getSpecificOrientationString(int rotation) {
        if (rotation < 45 || rotation > 315) {
            return "PORTRAIT";
        } else if (rotation > 180) {
            return "LANDSCAPE-LEFT";
        } else {
            return "LANDSCAPE-RIGHT";
        }
    }

    @ReactMethod
    public void getSpecificOrientation(Callback callback){
        callback.invoke(null, getSpecificOrientationString(currentRotation));
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        final int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

        String orientation = this.getOrientationString(orientationInt);

        if (orientation == "null") {
            callback.invoke(orientationInt, null);
        } else {
            callback.invoke(null, orientation);
        }
    }

    @ReactMethod
    public void lockToPortrait() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @ReactMethod
    public void lockToLandscape() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeLeft() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeRight() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @ReactMethod
    public void unlockAllOrientations() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();
        int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

        String orientation = this.getOrientationString(orientationInt);
        if (orientation == "null") {
            constants.put("initialOrientation", null);
        } else {
            constants.put("initialOrientation", orientation);
        }

        return constants;
    }

    private String getOrientationString(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "LANDSCAPE";
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "PORTRAIT";
        } else if (orientation == Configuration.ORIENTATION_UNDEFINED) {
            return "UNKNOWN";
        } else {
            return "null";
        }
    }

    @Override
    public void onHostResume() {

    }
    @Override
    public void onHostPause() {
      
    }

    @Override
    public void onHostDestroy() {

    }
}
