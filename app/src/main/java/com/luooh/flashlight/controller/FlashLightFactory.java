package com.luooh.flashlight.controller;

/**
 * Created by Luooh on 2017/8/7.
 */
public interface FlashLightFactory {

    int DISPATCH_ERROR = 0;
    int DISPATCH_STATE_CHANGED = 3;

    /**
     * 表示当前手机不支持手电筒
     */
    int ERROR_CODE_NOT_SUPPORT = 0x11;

    /**
     * 表示用户未授权打不开手电筒
     */
    int ERROR_CODE_NOT_PERMISSION = 0x13;

    /**
     * 表示其他应用程序正在使用Camera
     */
    int ERROR_CODE_CAMERA_IN_USE = 0x14;

    /**
     * indicating that the camera device could not be opened due to a device
     * policy.
     * @see android.app.admin.DevicePolicyManager#setCameraDisabled(android.content.ComponentName, boolean)
     */
    int ERROR_CODE_CAMERA_DISABLED = 0x15;

    /**
     * An error code that can be reported by {@link #onError}
     * indicating that the camera service has encountered a fatal error.
     *
     * <p>The Android device may need to be shut down and restarted to restore
     * camera function, or there may be a persistent hardware problem.</p>
     *
     * <p>An attempt at recovery <i>may</i> be possible by closing the
     * CameraDevice and the CameraManager, and trying to acquire all resources
     * again from scratch.</p>
     */
    int ERROR_CODE_CAMERA_SERVICE = 0x16;


    /**
     * An error code that can be reported by {@link #onError}
     * indicating that the camera device has encountered a fatal error.
     *
     * <p>The camera device needs to be re-opened to be used again.</p>
     */
    int ERROR_CODE_CAMERA_DEVICE = 0x17;

    /**
     * 未知错误码
     */
    int ERROR_CODE_UNKNOW = 0x20;

    /**
     * 获取手机是否支持闪光灯功能
     * @return false表示当前手机不支持手电筒功能
     */
    boolean isAvailable();

    /**
     * 获取手电筒当前的状态
     * @return true 表示
     */
    boolean getFlashLightState();

    /**
     * 开启或者关闭手电筒
     * @param enabled true表示开启手电筒，false表示关闭手电筒
     */
    void setFlashlight(boolean enabled);

    /**
     * 强制关闭手电筒并且释放资源
     */
    void killFlashlight();

    /**
     * 添加手电筒状态回调监听
     * @param listener
     */
    void addListener(FlashlightListener listener);

    /**
     * 删除手电筒状态回调监听
     * @param listener
     */
    void remoteListener(FlashlightListener listener);
}
