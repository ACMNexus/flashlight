package com.luooh.flashlight;

import android.app.Application;
import com.luooh.flashlight.controller.FlashLightManager;

/**
 * Created by Luooh on 2017/8/22.
 */
public class FlashLightApplication extends Application {

    public static FlashLightApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        FlashLightManager.getInstance().init(sInstance);
    }
}
