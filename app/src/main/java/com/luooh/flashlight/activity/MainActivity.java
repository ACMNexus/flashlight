package com.luooh.flashlight.activity;

import android.os.Bundle;
import com.luooh.flashlight.R;
import com.luooh.flashlight.controller.FlashLightManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mOuterCircle;
    private ImageView mInnerCircle;
    private FlashLightManager mManager;
    private boolean mFlashLightState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = FlashLightManager.getInstance();
        mFlashLightState = mManager.getFlashLightState();
        mOuterCircle = (ImageView) findViewById(R.id.outer_circle);
        mInnerCircle = (ImageView) findViewById(R.id.inner_circle);
        findViewById(R.id.flashlight_switch).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flashlight_switch:
                switchFlashLight();
                break;
        }
    }

    private void switchFlashLight() {
        if (!mFlashLightState) {
            mOuterCircle.setImageResource(R.drawable.shape_flash_switch_on);
            mInnerCircle.setImageResource(R.drawable.notify_flash_light_on);
        }else {
            mOuterCircle.setImageResource(R.drawable.shape_flash_switch_off);
            mInnerCircle.setImageResource(R.drawable.notify_flash_light_off);
        }
        mManager.startFlashLight(!mFlashLightState);
        mFlashLightState = !mFlashLightState;
    }

    @Override
    protected void onDestroy() {
        mManager.killFlashLight();
        super.onDestroy();
    }
}
