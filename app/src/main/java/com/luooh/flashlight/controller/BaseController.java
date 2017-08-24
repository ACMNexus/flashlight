package com.luooh.flashlight.controller;

import android.content.Context;
import android.os.Handler;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Luooh on 2017/8/7.
 */
public class BaseController implements FlashLightFactory {

    protected static final String TAG = BaseController.class.getSimpleName();

    protected Handler mHandler;
    protected String mCameraId;
    protected Context mContext;
    protected volatile boolean mCameraAvailable;
    protected volatile boolean mFlashlightEnabled;
    protected ArrayList<WeakReference<FlashlightListener>> mListeners;

    protected BaseController(Context context) {
        this.mContext = context;
        mListeners = new ArrayList<>();
    }

    @Override
    public void setFlashlight(boolean enabled) {
    }

    @Override
    public void killFlashlight() {
    }

    /**
     * 分发错误，开启手电筒发生错误时调用该方法
     * @param errorCode
     */
    public void dispatchError(int errorCode) {
        dispatchListeners(DISPATCH_ERROR, false, errorCode);
    }

    /**
     * 当手电筒状态发生变化时调用该方法
     * @param available
     */
    public void dispatchFlashStateChanged(boolean available) {
        dispatchListeners(DISPATCH_STATE_CHANGED, available, -1);
    }

    /**
     * 添加手电筒状态回调监听
     * @param listener
     */
    @Override
    public void addListener(FlashlightListener listener) {
        synchronized (mListeners) {
            cleanUpListenersLocked(listener);
            mListeners.add(new WeakReference(listener));
        }
    }

    /**
     * 删除手电筒状态监听
     * @param listener
     */
    @Override
    public void remoteListener(FlashlightListener listener) {
        synchronized (mListeners) {
            cleanUpListenersLocked(listener);
        }
    }

    private void cleanUpListenersLocked(FlashlightListener listener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            FlashlightListener found = mListeners.get(i).get();
            if (found == null || found == listener) {
                mListeners.remove(i);
            }
        }
    }

    /**
     * 用于处理手电筒的状态改变以及打开手电筒时的一些错误信息回调
     * @param message 0表示处理打开手电筒错误信息，1表示处理手电筒状态
     * @param argument true表示手电筒打开，false表示手电筒关闭
     * @param erroCode 表示打开手电筒的过程中出现的错误码
     */
    protected void dispatchListeners(int message, boolean argument, int erroCode) {
        synchronized (mListeners) {
            final int N = mListeners.size();
            boolean cleanup = false;
            for (int i = 0; i < N; i++) {
                FlashlightListener l = mListeners.get(i).get();
                if (l != null) {
                    if (message == DISPATCH_ERROR) {
                        l.onFlashlightError(erroCode);
                    } else if (message == DISPATCH_STATE_CHANGED) {
                        l.onFlashlightStateChanged(argument);
                    }
                } else {
                    cleanup = true;
                }
            }
            if (cleanup) {
                cleanUpListenersLocked(null);
            }
        }
    }

    @Override
    public synchronized boolean isAvailable() {
        return mCameraAvailable;
    }

    @Override
    public synchronized boolean getFlashLightState() {
        return mFlashlightEnabled;
    }
}
