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

    private boolean pendingError = false;
    private MiddleVersionController mController;

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

        mController = new MiddleVersionController(mContext);
    }

    @Override
    public void setFlashlight(boolean enabled) {
        if(!TextUtils.isEmpty(mCameraId) && !pendingError) {
            synchronized (this) {
                if (mFlashlightEnabled != enabled) {
                    try {
                        /**
                         * 在有些手机，比如说我遇到的360 6.0手机上在开启手电筒的时候就会崩溃的
                         * 当出现这种情况的时候，我们就使用MiddleVersion去开启手电筒，然后依次，
                         * 如果Middle的方式开启手电筒失败了，则使用LowVersion方式开启手电筒
                         */
                        mCameraManager.setTorchMode(mCameraId, enabled);
                        mFlashlightEnabled = enabled;
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "Couldn't set torch mode", e);
                        mFlashlightEnabled = false;
                        pendingError = true;
                    } catch (Throwable throwable) {
                        mFlashlightEnabled = false;
                        pendingError = true;
                    }
                }
            }
        }

        if (pendingError) {
            mController.setFlashlight(enabled);
            return;
        }

        dispatchModeChanged(mFlashlightEnabled);
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
