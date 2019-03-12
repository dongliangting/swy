package com.iflytek.aiui.demo.chat.ui.chat;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.media.AudioManager;

import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayer;
import com.iflytek.aiui.demo.chat.repository.player.PlayState;
import com.iflytek.aiui.demo.chat.repository.TTSManager;
import com.iflytek.aiui.demo.chat.ui.common.SingleLiveEvent;

import java.util.List;

import javax.inject.Inject;

/**
 * 播放器ViewModel
 * 获得播放器状态
 * 控制播放器播放，停止
 */

public class PlayerViewModel extends ViewModel {
    private AudioManager mAudioManager;
    private AIUIPlayer mPlayer;
    private TTSManager mTTSManager;
    private SingleLiveEvent<String> mPlayerTips = new SingleLiveEvent<>();

    @Inject
    public PlayerViewModel(Context context, AIUIPlayer player, TTSManager ttsManager) {
        mPlayer = player;
        mTTSManager = ttsManager;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public LiveData<PlayState> getPlayState() {
        return mPlayer.getLiveState();
    }

    public LiveData<String> getPlayerTips() {
        return mPlayerTips;
    }

    public void playList(List<AIUIPlayer.MediaInfo> list, int startIndex) {
        mPlayer.playList(list, startIndex);
    }

    public AIUIPlayer.MediaInfo play() {
        return mPlayer.play();
    }

    //自动暂停 唤醒后自动暂停或按住说话自动暂停
    public AIUIPlayer.MediaInfo autoPause() {
        return mPlayer.autoPause();
    }

    public AIUIPlayer.MediaInfo manualPause() {
        return mPlayer.manualPause();
    }

    public void resumeIfNeed() {
        mPlayer.resumeIfNeed();
    }

    public AIUIPlayer.MediaInfo prev() {
        AIUIPlayer.MediaInfo prev = mPlayer.prev();
        if(prev == null) {
           mPlayerTips.setValue("当前已经是播放列表第一项");
        }

        return prev;
    }

    public AIUIPlayer.MediaInfo next() {
        AIUIPlayer.MediaInfo next = mPlayer.next();
        if(next == null) {
            mPlayerTips.setValue("当前已经是播放列表最后一项");
        }

        return next;
    }

    public AIUIPlayer.MediaInfo current() {
        return mPlayer.currentMedia();
    }


    public boolean stop() {
        mPlayer.stop();
        return true;
    }

    public boolean isActive() {
       return mPlayer.isActive();
    }


    public void volumeMinus() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI);
    }

    public void volumePlus() {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI);
    }

    public void volumePercent(float percent) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * percent), AudioManager.FLAG_SHOW_UI);
    }

    public void startTTS(String text, Runnable callback, boolean resume) {
        mTTSManager.startTTS(text, callback, resume);
    }

    public void pauseTTS() {
        mTTSManager.pauseTTS();
    }
}
