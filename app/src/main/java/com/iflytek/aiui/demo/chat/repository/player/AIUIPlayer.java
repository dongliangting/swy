package com.iflytek.aiui.demo.chat.repository.player;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 播放器
 */

public class AIUIPlayer {
    //单条歌曲信息
    public static class MediaInfo {
        public String author;
        public String mediaName;
        public String mediaPath;

        public MediaInfo(String author, String songName, String audioPath) {
            this.author = author;
            this.mediaName = songName;
            this.mediaPath = audioPath;
        }
    }

    private DataSource.Factory mDataSourceFactory;
    private Context mContext;
    //当前播放的歌曲列表
    private List<MediaInfo> songList = new ArrayList<>();
    //Google ExoPlayer实例
    private ExoPlayer mPlayer;
    //播放控制器
    private ControlDispatcher mDispatcher;
    //当前播放状态
    private MutableLiveData<PlayState> mState = new MutableLiveData<>();
    //当前播放项
    private int mCurrentIndex = -1;
    //是否正在播放
    private boolean mActive = false;
    private boolean mManualPause = false;

    public AIUIPlayer(Context context, ExoPlayer player) {
        this.mPlayer = player;
        this.mContext = context;
        mDataSourceFactory = new DefaultDataSourceFactory(mContext,"AIUIChatDemo");

        this.mDispatcher = new DefaultControlDispatcher();

        //根据播放进度和状态通知外部更新
        this.mPlayer.addListener(new Player.DefaultEventListener() {
            // getCurrentWindowIndex 返回不准问题（https://github.com/google/ExoPlayer/issues/2799）
            @Override
            public void onPositionDiscontinuity(int reason) {
                super.onPositionDiscontinuity(reason);
                mCurrentIndex = mPlayer.getCurrentWindowIndex();
                if(mCurrentIndex > -1 && mCurrentIndex < songList.size()) {
                    MediaInfo info = songList.get(mCurrentIndex);
                    setState(new PlayState(mActive, true, info.mediaName));
                }
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                super.onTracksChanged(trackGroups, trackSelections);
                mCurrentIndex = mPlayer.getCurrentWindowIndex();
                if(mCurrentIndex > -1 && mCurrentIndex < songList.size()) {
                    MediaInfo info = songList.get(mCurrentIndex);
                    setState(new PlayState(mActive, true, info.mediaName));
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Timber.d("state change %b %d", playWhenReady, playbackState);
                super.onPlayerStateChanged(playWhenReady, playbackState);
                if(playWhenReady) {
                    mActive = true;
                }
                boolean playing = playWhenReady && (playbackState != Player.STATE_ENDED);
                if(mCurrentIndex > -1 && mCurrentIndex < songList.size()) {
                    MediaInfo info = songList.get(mCurrentIndex);
                    setState(new PlayState(mActive, playing, info.mediaName));
                }
            }
        });
    }

    /**
     * 播放歌曲列表
     * @param list 歌曲列表
     */
    public void playList(List<MediaInfo> list, int startIndex) {
        songList = list;
        if(songList == null) {
            songList = new ArrayList<>();
        }
        ConcatenatingMediaSource source = new ConcatenatingMediaSource();
        for (MediaInfo info : list) {
            Uri uri = Uri.parse(info.mediaPath);
            int type = Util.inferContentType(uri);
            switch (type) {
                case C.TYPE_HLS: {
                    HlsMediaSource.Factory factory = new HlsMediaSource.Factory(mDataSourceFactory);
                    //忽略Ts文件中的h264 stream
                    factory.setExtractorFactory(new DefaultHlsExtractorFactory(DefaultTsPayloadReaderFactory.FLAG_IGNORE_H264_STREAM));
                    source.addMediaSource(factory.createMediaSource(uri));
                }
                break;

                case C.TYPE_OTHER: {
                    source.addMediaSource(new ExtractorMediaSource.Factory(mDataSourceFactory).createMediaSource(uri));
                }
                break;
            }
        }

        mPlayer.prepare(source);
        mPlayer.setPlayWhenReady(true);

        mDispatcher.dispatchSeekTo(mPlayer, safeIndexInner(startIndex), C.TIME_UNSET);
    }

    /**
     * 获取播放器当前状态
     * @return 当前状态
     */
    public LiveData<PlayState> getLiveState() {
        return mState;
    }

    //自动暂停 唤醒后自动暂停或按住说话自动暂停
    public AIUIPlayer.MediaInfo autoPause() {
        return pause();
    }

    // 手动暂停 按钮点击或语义指令
    public AIUIPlayer.MediaInfo manualPause() {
        mManualPause = true;
        return pause();
    }

    public void resumeIfNeed() {
        if (!mManualPause) {
            play();
        }
    }


    public MediaInfo play() {
        if(mPlayer.getPlaybackState() == Player.STATE_ENDED) {
            mPlayer.seekTo(0);
        }
        mDispatcher.dispatchSetPlayWhenReady(mPlayer, true);
        return safeIndex(mCurrentIndex);
    }

    public MediaInfo next() {
        int index;
        if (mPlayer.getNextWindowIndex() != C.INDEX_UNSET) {
            index = mPlayer.getNextWindowIndex();
            mDispatcher.dispatchSeekTo(mPlayer, index, C.TIME_UNSET);
            play();

            return safeIndex(index);
        } else {
            return null;
        }
    }

    public MediaInfo prev() {
        int index;
        if (mPlayer.getPreviousWindowIndex() != C.INDEX_UNSET) {
            index = mPlayer.getPreviousWindowIndex();
            mDispatcher.dispatchSeekTo(mPlayer, index, C.TIME_UNSET);
            play();
            return safeIndex(index);
        } else {
            return null;
        }
    }

    public void stop() {
        mManualPause = true;
        mActive = false;
        pause();
    }

    public MediaInfo currentMedia() {
        return safeIndex(mCurrentIndex);
    }


    private MediaInfo safeIndex(int index) {
        if(index > -1 && songList.size() > 0 && index < songList.size()) {
            return songList.get(index);
        } else {
            return null;
        }
    }

    private int safeIndexInner(int index) {
        if(index > -1 && songList.size() > 0 && index < songList.size()) {
            return index;
        } else {
            return 0;
        }
    }

    public boolean isActive() {
        return mActive;
    }

    private void setState(PlayState state) {
        if(state.playing) {
            mManualPause = false;
        }
        mState.setValue(state);
    }

    private MediaInfo pause() {
        mDispatcher.dispatchSetPlayWhenReady(mPlayer, false);
        return safeIndex(mCurrentIndex);
    }
}
