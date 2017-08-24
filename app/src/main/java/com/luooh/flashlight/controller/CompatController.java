package com.luooh.flashlight.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by Luooh on 2017/8/17.
 */
public class CompatController extends BaseController {

    protected CameraManager mCameraManager;

    protected CompatController(Context context) {
        super(context);
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void initialize() {
        try {
            mCameraId = getCameraId();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Couldn't initialize.", e);
            return;
        }  catch (SecurityException e1) {
            Log.e(TAG, "The phone reject the camera permission", e1);
            dispatchError(ERROR_CODE_NOT_SUPPORT);
            return;
        } catch (Throwable throwable) {
            dispatchError(ERROR_CODE_UNKNOW);
            return;
        }

        if (mCameraId != null) {
            ensureHandler();
        }
    }

    public synchronized void ensureHandler() {
        if (mHandler == null) {
            HandlerThread thread = new HandlerThread(TAG, 8);
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }
    }

    /**
     * 获取相机的cameraId，如果cameraId为空表示该手机不支持闪光灯
     * 还有一种可能就是该Camera的权限被拒绝了，所以一直拿不到CameraService的
     * @return
     * @throws CameraAccessException
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String getCameraId() throws CameraAccessException {
        String ids[] = mCameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable
                    && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    @Override
    public void setFlashlight(boolean enabled) {
    }

}
