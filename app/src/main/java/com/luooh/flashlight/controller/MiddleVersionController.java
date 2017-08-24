package com.luooh.flashlight.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import java.util.ArrayList;

/**
 * Created by Luooh on 2017/8/7.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MiddleVersionController extends CompatController {

    private Surface mSurface;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mSession;
    private SurfaceTexture mSurfaceTexture;
    private CaptureRequest mFlashlightRequest;
    private LowVersionController mLowController;

    protected MiddleVersionController(Context context) {
        super(context);
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (mCameraId != null) {
            mCameraManager.registerAvailabilityCallback(mAvailabilityCallback, mHandler);
        }
        if (TextUtils.isEmpty(mCameraId)) {
            mLowController = new LowVersionController(mContext);
        }
    }

    @Override
    public void setFlashlight(boolean enabled) {
        /**
         * TODO 注意：如果这里获取到CameraId为空的话，则使用LowVersion版本的方式打开手电筒
         * 因为会在YunOs手机上可能会因为权限或者是别的什么原因导致获取CameraId为空的情况的
         */
        if (TextUtils.isEmpty(mCameraId)) {
            mLowController.setFlashlight(enabled);
            return;
        }

        if (mFlashlightEnabled != enabled) {
            mFlashlightEnabled = enabled;
            postUpdateFlashlight();
        }
    }

    @Override
    public void killFlashlight() {
        synchronized (this) {
            mFlashlightEnabled = false;
        }
        mHandler.post(mKillFlashlightRunnable);
    }

    private void postUpdateFlashlight() {
        ensureHandler();
        mHandler.post(mUpdateFlashlightRunnable);
    }

    private void teardown() {
        try {
            mCameraDevice = null;
            mSession = null;
            mFlashlightRequest = null;
            if (mSurface != null) {
                mSurface.release();
                mSurfaceTexture.release();
            }
            mSurface = null;
            mSurfaceTexture = null;
            mFlashlightEnabled = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Runnable mUpdateFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            updateFlashlight(false);
        }
    };

    private final Runnable mKillFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                mFlashlightEnabled = false;
            }
            updateFlashlight(true /* forceDisable */);
        }
    };

    private void updateFlashlight(boolean forceDisable) {
        try {
            boolean enabled;
            synchronized (this) {
                enabled = mFlashlightEnabled && !forceDisable;
            }
            if (enabled) {
                if (mCameraDevice == null) {
                    startDevice();
                    return;
                }
                if (mSession == null) {
                    startSession();
                    return;
                }
                if (mFlashlightRequest == null || (mFlashlightRequest.get(CaptureRequest.FLASH_MODE)).intValue() != 2) {
                    CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                    builder.addTarget(mSurface);
                    CaptureRequest request = builder.build();
                    mSession.capture(request, null, mHandler);
                    mFlashlightRequest = request;
                    mFlashlightEnabled = true;
                }
            } else {
                if (mFlashlightRequest != null) {
                    CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    builder.addTarget(mSurface);
                    CaptureRequest request = builder.build();
                    mSession.capture(request, null, mHandler);
                    mFlashlightRequest = request;
                    mFlashlightEnabled = false;
                }
            }
            if (forceDisable) {
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                }
                teardown();
            }
        } catch (CameraAccessException | IllegalStateException | UnsupportedOperationException e) {
            Log.e(TAG, "Error in updateFlashlight", e);
            dispatchError(ERROR_CODE_UNKNOW);
        } catch (Throwable throwable) {
            Log.e(TAG, "Error in updateFlashlight" + (throwable != null ? throwable.getMessage() : "unknow th error"));
            dispatchError(ERROR_CODE_UNKNOW);
        }
    }

    private void startDevice() {
        //表示用户未授权给手电筒
        if (ContextCompat.checkSelfPermission(mContext, "android.permission.CAMERA") != 0) {
            dispatchError(ERROR_CODE_NOT_PERMISSION);
            return;
        }
        try {
            mCameraManager.openCamera(mCameraId, mCameraListener, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e1) {
            Log.e(TAG, "The System will reject the camera permission and not open camera");
        } catch (Throwable throwable) {
            Log.e(TAG, "Couldn't open the camera use the cameraId by the CameraManager");
        }
    }

    private void startSession() throws CameraAccessException {
        mSurfaceTexture = new SurfaceTexture(0, false);
        Size size = getSmallestSize(mCameraDevice.getId());
        mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
        mSurface = new Surface(mSurfaceTexture);
        ArrayList<Surface> outputs = new ArrayList<>(1);
        outputs.add(mSurface);
        mCameraDevice.createCaptureSession(outputs, mSessionListener, mHandler);
    }

    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException("Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }

    private void handleError(int errorCode) {
        synchronized (this) {
            mFlashlightEnabled = false;
        }
        dispatchError(errorCode);
        dispatchFlashStateChanged(false);
        updateFlashlight(true);
    }

    private final CameraDevice.StateCallback mCameraListener = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            postUpdateFlashlight();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            if (mCameraDevice == camera) {
                dispatchError(ERROR_CODE_CAMERA_IN_USE);
                teardown();
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "Camera error: camera = " + camera + " error=" + error);
            if (camera == mCameraDevice || mCameraDevice == null) {
                int errorCode;
                if (error == CameraDevice.StateCallback.ERROR_CAMERA_SERVICE) {
                    errorCode = ERROR_CODE_CAMERA_SERVICE;
                } else if (error == CameraDevice.StateCallback.ERROR_CAMERA_DEVICE) {
                    errorCode = ERROR_CODE_CAMERA_DEVICE;
                } else if (error == CameraDevice.StateCallback.ERROR_CAMERA_DISABLED) {
                    errorCode = ERROR_CODE_CAMERA_DISABLED;
                } else {
                    errorCode = ERROR_CODE_CAMERA_IN_USE;
                }
                handleError(errorCode);
            }
        }
    };

    private final CameraCaptureSession.StateCallback mSessionListener =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mSession = session;
                    postUpdateFlashlight();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "Configure failed.");
                    if (mSession == null || mSession == session) {
                        handleError(ERROR_CODE_UNKNOW);
                    }
                }
            };

    private final CameraManager.AvailabilityCallback mAvailabilityCallback =
            new CameraManager.AvailabilityCallback() {
                @Override
                public void onCameraAvailable(String cameraId) {
                    Log.d(TAG, "onCameraAvailable(" + cameraId + ")");
                    if (cameraId.equals(mCameraId)) {
                        setCameraAvailable(true);
                    }
                }

                @Override
                public void onCameraUnavailable(String cameraId) {
                    Log.d(TAG, "onCameraUnavailable(" + cameraId + ")");
                    if (cameraId.equals(mCameraId)) {
                        setCameraAvailable(false);
                    }
                }

                private void setCameraAvailable(boolean available) {
                    boolean changed;
                    synchronized (MiddleVersionController.this) {
                        changed = mCameraAvailable != available;
                        mCameraAvailable = available;
                    }

                    dispatchFlashStateChanged(available);
                    Log.d(TAG, "dispatchAvailabilityChanged(" + available + ")...changed.." + changed);
                }
            };
}
