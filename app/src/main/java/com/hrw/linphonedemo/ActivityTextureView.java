package com.hrw.linphonedemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.hrw.linphonelibrary.call.CallHelper;
import com.hrw.linphonelibrary.widget.TimeTextView;

import org.linphone.core.Core;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2019/09/19 15:04
 * @desc:
 */
public class ActivityTextureView extends AppCompatActivity implements View.OnClickListener {
    protected CallHelper mCallHelper;
    protected Core mCore;

    protected TextureView ttvMyVideo;
    protected TextureView ttvOtherVideo;

    protected TextView tvCancelCall;//挂断通话
    protected TextView tvAcceptCall;//接受通话

    protected TextView tvSwitchVoiceVideo;
    protected TextView tvMute;//静音
    protected TextView tvGreaterVoice;//扩音

    protected TimeTextView ttvComputerTime;
    protected TextView tvWaitConnect;

    private void initView() {
        ttvMyVideo = (TextureView) findViewById(com.hrw.linphonelibrary.R.id.ttv_my_video);
        ttvOtherVideo = (TextureView) findViewById(com.hrw.linphonelibrary.R.id.ttv_other_video);

        tvCancelCall = (TextView) findViewById(com.hrw.linphonelibrary.R.id.bt_cancel_call);
        tvAcceptCall = (TextView) findViewById(com.hrw.linphonelibrary.R.id.bt_accept_call);

        tvSwitchVoiceVideo = (TextView) findViewById(com.hrw.linphonelibrary.R.id.tv_switch_voice_video);
        tvMute = (TextView) findViewById(com.hrw.linphonelibrary.R.id.tv_mute);
        tvGreaterVoice = (TextView) findViewById(com.hrw.linphonelibrary.R.id.tv_greater_voice);

        ttvComputerTime = (TimeTextView) findViewById(com.hrw.linphonelibrary.R.id.ttv_computer_time);
        tvWaitConnect = (TextView) findViewById(com.hrw.linphonelibrary.R.id.tv_wait_accept);

        tvCancelCall.setOnClickListener(this);
        tvAcceptCall.setOnClickListener(this);

        tvSwitchVoiceVideo.setOnClickListener(this);
        tvMute.setOnClickListener(this);
        tvGreaterVoice.setOnClickListener(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_view);
        initView();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == com.hrw.linphonelibrary.R.id.bt_cancel_call) {
            System.out.println("取消通话");
        } else if (i == com.hrw.linphonelibrary.R.id.bt_accept_call) {
            System.out.println("接受通话");
        } else if (i == com.hrw.linphonelibrary.R.id.tv_switch_voice_video) {
            System.out.println("转换视频");
        } else if (i == com.hrw.linphonelibrary.R.id.tv_mute) {
            System.out.println("静音操作");
        } else if (i == com.hrw.linphonelibrary.R.id.tv_greater_voice) {
            System.out.println("扩音操作");
        }
    }
}
