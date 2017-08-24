package com.luooh.flashlight.controller;

import android.content.Context;
import android.hardware.Camera;

/**
 * Created by Luooh on 2017/8/7.
 */
public class LowVersionController extends BaseController {

    private Camera mCamera;
    private Camera.Parameters mParameters;

    protected LowVersionController(Context context) {
        super(context);
    }

    @Override
    public void setFlashlight(boolean enabled) {
        if (enabled) {
            try {
                Camera camera = start();
                //表示不支持闪光灯
                if(camera == null) {
                    mCameraAvailable = false;
                    mFlashlightEnabled = false;
                    dispatchError(ERROR_CODE_NOT_SUPPORT);
                    return;
                }
                mFlashlightEnabled = true;
            } catch (SecurityException e) {
                mFlashlightEnabled = false;
                dispatchError(ERROR_CODE_NOT_PERMISSION);
            } catch (Throwable throwable) {
                mFlashlightEnabled = false;
                dispatchError(ERROR_CODE_CAMERA_IN_USE);
            }
            dispatchFlashStateChanged(mFlashlightEnabled);
        }else {
            try {
                close();
                mFlashlightEnabled = false;
            } catch (Throwable throwable) {
                //这里表示关闭失败了，不做处理
            }
            dispatchFlashStateChanged(mFlashlightEnabled);
        }
    }

    @Override
    public void killFlashlight() {
        if(mCamera != null) {
            try {
                close();
                mCamera = null;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private Camera start() throws Throwable {
        mCamera = Camera.open();
        if(mCamera == null) {
            //表示当前手机没有前置摄像头
            return null;
        }
        mParameters = mCamera.getParameters();
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
        return mCamera;
    }

    private void close() throws Throwable {
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
        mCamera.stopPreview();
        mCamera.release();
    }
}
