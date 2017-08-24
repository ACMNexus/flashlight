package com.luooh.flashlight.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Luooh on 2017/8/7.
 */
@TargetApi(Build.VERSION_CODES.M)
public class HighVersionController extends CompatController {

    protected HighVersionController(Context context) {
        super(context);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (mCameraId != null) {
            mCameraManager.registerTorchCallback(mTorchCallback, mHandler);
        }
    }

    @Override
    public void setFlashlight(boolean enabled) {
        boolean pendingError = false;
        if(!TextUtils.isEmpty(mCameraId)) {
            synchronized (this) {
                if (mFlashlightEnabled != enabled) {
                    mFlashlightEnabled = enabled;
                    try {
                        mCameraManager.setTorchMode(mCameraId, enabled);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Couldn't set torch mode", e);
                        mFlashlightEnabled = false;
                        pendingError = true;
                    }
                }
            }
        }

        dispatchModeChanged(mFlashlightEnabled);
        if (pendingError) {
//            dispatchError();
        }
    }

    private void dispatchModeChanged(boolean enabled) {
    }

    private final CameraManager.TorchCallback mTorchCallback =
            new CameraManager.TorchCallback() {

        @Override
        public void onTorchModeUnavailable(String cameraId) {
            if (TextUtils.equals(cameraId, mCameraId)) {
                setCameraAvailable(false);
            }
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            if (TextUtils.equals(cameraId, mCameraId)) {
                setCameraAvailable(true);
                setTorchMode(enabled);
            }
        }

        private void setCameraAvailable(boolean available) {
            boolean changed;
            synchronized (HighVersionController.this) {
                changed = mCameraAvailable != available;
                mCameraAvailable = available;
            }
            if (changed) {
                Log.d(TAG, "dispatchAvailabilityChanged(" + available + ")");
            }
        }

        private void setTorchMode(boolean enabled) {
            boolean changed;
            synchronized (HighVersionController.this) {
                changed = mFlashlightEnabled != enabled;
                mFlashlightEnabled = enabled;
            }
            if (changed) {
                Log.d(TAG, "dispatchModeChanged(" + enabled + ")");
                dispatchModeChanged(enabled);
            }
        }
    };
}
