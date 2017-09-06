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
        setFlashlight(enabled, true);
    }

    /**
     * 打开或者关闭手电筒
     * @param enabled true表示打开手电筒，false表示关闭手电筒
     * @param quickStart 是否使用快速打开手电筒的方法进行处理
     */
    public void setFlashlight(boolean enabled, boolean quickStart) {
        if(enabled == mFlashlightEnabled) {
            return;
        }

        if(quickStart) {
            if (enabled) {
                try {
                    Camera camera = quickStart();
                    if(camera == null) {
                        mCameraAvailable = false;
                        mFlashlightEnabled = false;
                        dispatchError(ERROR_CODE_NOT_SUPPORT);
                        return;
                    }
                    mFlashlightEnabled = true;
                }catch (SecurityException e) {
                    mFlashlightEnabled = false;
                    dispatchError(ERROR_CODE_NOT_PERMISSION);
                } catch (Throwable throwable) {
                    mFlashlightEnabled = false;
                    dispatchError(ERROR_CODE_CAMERA_IN_USE);
                }
                dispatchFlashStateChanged(mFlashlightEnabled);
            }else {
                try {
                    closeNotRelease();
                    mFlashlightEnabled = false;
                } catch (Throwable throwable) {
                    //这里表示关闭失败了，不做处理
                }
                dispatchFlashStateChanged(mFlashlightEnabled);
            }
        }else {
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
    }

    /**
     * 表示强制关闭手电筒并且释放手电筒资源
     */
    @Override
    public void killFlashlight() {
        if(mCamera != null) {
            try {
                if(mCamera != null) {
                    close();
                }
                mCamera = null;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * 打开手电筒，但是该方法打开手电筒的速度没有quickStart()的速度这么快
     * 因为我们在打开创建Camera之后又会去销毁这个Camera的，但是该方法有一个
     * 好处就是关闭手电筒之后会释放手电筒的资源。
     * @return
     * @throws Throwable
     */
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

    /**
     * 快速开启手电筒
     * TODO 如果我们调用了该方法的话，其打开手电筒的速度跟系统的打开手电筒的速度是一样的
     * TODO 但是因为我们占用了系统相机的资源，所以必须要释放资源的，如果不释放资源的话，则其他的程序是不打不开的相机和手电筒的。
     * @return
     */
    private Camera quickStart() {
        if(mCamera == null) {
            mCamera = Camera.open();
        }

        if(mCamera == null) {
            return null;
        }
        if(mParameters == null) {
            mParameters = mCamera.getParameters();
        }

        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
        return mCamera;
    }

    /**
     * 关闭手电筒但是不释放资源
     */
    private void closeNotRelease() {
        if(mParameters != null) {
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        if(mCamera != null) {
            mCamera.setParameters(mParameters);
        }
    }

    /**
     * 关闭手电筒而且还会释放资源
     * @throws Throwable
     */
    private void close() throws Throwable {
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
        mCamera.stopPreview();
        mCamera.release();
    }
}
