package com.hrw.linphonelibrary.call;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.hrw.linphonelibrary.LinPhoneHelper;
import com.hrw.linphonelibrary.config.AndroidAudioManager;
import com.hrw.linphonelibrary.utils.FileUtil;

import org.linphone.core.Address;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.MediaEncryption;
import org.linphone.core.VideoActivationPolicy;
import org.linphone.core.tools.Log;

/**
 * @version 1.0.0
 * @author:hrw
 * @date:2019/09/12 14:18
 * @desc:拨号管理
 */
public class CallHelper {

    /**
     * 发起语音通话
     */
    public void startVoiceCall(String callAddress, String callName, CoreListenerStub listenerStub) {
        Core core = LinPhoneHelper.getInstance().getCore();
        if (listenerStub != null) core.addListener(listenerStub);
        Address address = core.interpretUrl(callAddress);
        address.setDisplayName(callName);
        CallParams params = core.createCallParams(null);
        params.enableVideo(false);
        core.inviteAddressWithParams(address, params);
    }

    public void startVoiceCall(String callAddress, String callName) {
        startVoiceCall(callAddress, callName, null);
    }

    public void addListener(CoreListenerStub listenerStub) {
        Core core = LinPhoneHelper.getInstance().getCore();
        if (listenerStub != null) core.addListener(listenerStub);
    }

    public void removeListener(CoreListenerStub listenerStub) {
        Core core = LinPhoneHelper.getInstance().getCore();
        if (listenerStub != null) core.removeListener(listenerStub);
    }

    /**
     * 发起视屏通话
     */
    public void startVideoCall(String to, String displayName) {
        Core core = LinPhoneHelper.getInstance().getCore();
        Address address = core.interpretUrl(to);
        address.setDisplayName(displayName);
        CallParams params = core.createCallParams(null);
        params.enableVideo(true);
        core.inviteAddressWithParams(address, params);
    }

    public static boolean isHighBandwidthConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        // in doubt, assume connection is good.
        return true;
    }

    public boolean isVideoEnabled() {
        if (LinPhoneHelper.getInstance().getCore() == null) return false;
        return LinPhoneHelper.getInstance().getCore().videoSupported() && LinPhoneHelper.getInstance().getCore().videoEnabled();
    }

    public boolean shouldInitiateVideoCall() {
        if (LinPhoneHelper.getInstance().getCore() == null) return false;
        return LinPhoneHelper.getInstance().getCore().getVideoActivationPolicy().getAutomaticallyInitiate();
    }

    public void inviteAddress(Address address, boolean videoEnabled, boolean lowBandwidth) {
        inviteAddress(address, videoEnabled, lowBandwidth, false);
    }

    private void inviteAddress(Address address, boolean videoEnabled, boolean lowBandwidth, boolean forceZRTP) {
        Core core = LinPhoneHelper.getInstance().getCore();

        CallParams params = core.createCallParams(null);
//        mBandwidthManager.updateWithProfileSettings(params);

        if (videoEnabled && params.videoEnabled()) {
            params.enableVideo(true);
        } else {
            params.enableVideo(false);
        }

        if (lowBandwidth) {
            params.enableLowBandwidth(true);
            Log.d("[Call Manager] Low bandwidth enabled in call params");
        }

        if (forceZRTP) {
            params.setMediaEncryption(MediaEncryption.ZRTP);
        }

//        String recordFile = FileUtils.getCallRecordingFilename(LinphoneService.instance(), address);
//        params.setRecordFile(recordFile);

        core.inviteAddressWithParams(address, params);
    }

    public void setInitiateVideoCall(boolean initiate) {
        if (LinPhoneHelper.getInstance().getCore() == null) return;
        VideoActivationPolicy vap = LinPhoneHelper.getInstance().getCore().getVideoActivationPolicy();
        vap.setAutomaticallyInitiate(initiate);
        LinPhoneHelper.getInstance().getCore().setVideoActivationPolicy(vap);
    }

    /**
     * 语音通话切换到视屏通话
     */
    public void toggleVideo() {
        Call call = LinPhoneHelper.getInstance().getCore().getCurrentCall();
        if (call == null) return;
        if (call.getCurrentParams().videoEnabled()) {
            removeVideo();
        } else {
            addVideo();
        }
    }

    public boolean videoEnabled() {
        Call call = LinPhoneHelper.getInstance().getCore().getCurrentCall();
        return call.getCurrentParams().videoEnabled();
    }


    public void addVideo() {
        Call call = LinPhoneHelper.getInstance().getCore().getCurrentCall();
        if (call.getState() == Call.State.End || call.getState() == Call.State.Released) return;
        if (!call.getCurrentParams().videoEnabled()) {
            enableCamera(call, true);
            reinviteWithVideo();
        }
    }

    public void removeVideo() {
        Core core = LinPhoneHelper.getInstance().getCore();
        Call call = core.getCurrentCall();
        CallParams params = core.createCallParams(call);
        params.enableVideo(false);
        enableCamera(call, false);
        call.update(params);
    }

    private boolean reinviteWithVideo() {
        Core core = LinPhoneHelper.getInstance().getCore();
        Call call = core.getCurrentCall();
        if (call == null) {
            Log.e("[Call Manager] Trying to add video while not in call");
            return false;
        }
        if (call.getRemoteParams().lowBandwidthEnabled()) {
            Log.e("[Call Manager] Remote has low bandwidth, won't be able to do video");
            return false;
        }

        CallParams params = core.createCallParams(call);
        if (params.videoEnabled()) return false;

        // Check if video possible regarding bandwidth limitations
        if (params != null) { // in call
            // Update video parm if
            params.enableVideo(true);
            params.setAudioBandwidthLimit(0); // disable limitation
        }

        // Abort if not enough bandwidth...
        if (!params.videoEnabled()) {
            return false;
        }

        // Not yet in video call: try to re-invite with video
        call.update(params);
        return true;
    }

    private void enableCamera(Call call, boolean enable) {
        if (call != null) {
            call.enableCamera(enable);
        }
    }

    /**
     * 切换到扩音
     */

    public void toggleSpeaker() {
        AndroidAudioManager audioManager = LinPhoneHelper.getInstance().getAndroidAudioManager();
        if (audioManager.isAudioRoutedToSpeaker()) {
            audioManager.routeAudioToEarPiece();
        } else {
            audioManager.routeAudioToSpeaker();
        }
    }


    public boolean isSpeakerphoneOn() {
        AndroidAudioManager audioManager = LinPhoneHelper.getInstance().getAndroidAudioManager();
        return audioManager.isAudioRoutedToSpeaker();
    }


    /**
     * 静音切换
     */
    public void toggleMic() {
        Core core = LinPhoneHelper.getInstance().getCore();
        core.enableMic(!core.micEnabled());
    }


    /**
     * 是否启用静音
     *
     * @return
     */
    public boolean micEnabled() {
        return LinPhoneHelper.getInstance().getCore().micEnabled();
    }

    /**
     * 接通电话
     *
     * @param call
     * @return
     */
    public boolean acceptCall(Context context, Call call) {
        if (call == null) return false;

        Core core = LinPhoneHelper.getInstance().getCore();
        CallParams params = core.createCallParams(call);

//        boolean isLowBandwidthConnection = !LinphoneUtils.isHighBandwidthConnection(LinphoneService.instance());

        if (params != null) {
            params.enableLowBandwidth(true);
            params.setRecordFile(FileUtil.getCallRecordingFilename(context, call.getRemoteAddress()));
        } else {
            Log.e("[Call Manager] Could not create call params for call");
            return false;
        }

        call.acceptWithParams(params);
        return true;
    }

    /**
     * 挂断通话
     */
    public void cancelCall() {
        Core core = LinPhoneHelper.getInstance().getCore();
        Call call = core.getCurrentCall();
        if (call != null) {
            call.terminate();
        } else if (core.isInConference()) {
            core.terminateConference();
        } else {
            core.terminateAllCalls();
        }
    }
}