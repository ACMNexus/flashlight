package com.luooh.flashlight.controller;

import android.content.Context;
import android.os.Build;

/**
 * Created by Luooh on 2017/8/7.
 */
public class FlashLightManager {

    private Context mContext;
    private BaseController mController;
    private static FlashLightManager sInstance;

    public static FlashLightManager getInstance() {
        if(sInstance == null) {
            sInstance = new FlashLightManager();
        }
        return sInstance;
    }

    public void init(Context context) {
        if(mController == null) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mController = new LowVersionController(context);
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mController = new HighVersionController(context);
            }else {
                mController = new MiddleVersionController(context);
            }
        }
    }

    public synchronized void startFlashLight(boolean state) {
        mController.setFlashlight(state);
    }

    public synchronized boolean getFlashLightState() {
        return mController.getFlashLightState();
    }

    public synchronized void killFlashLight() {
        mController.killFlashlight();
    }

    public void registerListener(FlashlightListener listener) {
        if(listener != null) {
            mController.addListener(listener);
        }
    }

    public void unregisterListener(FlashlightListener listener) {
        if(listener != null) {
            mController.remoteListener(listener);
        }
    }
}
