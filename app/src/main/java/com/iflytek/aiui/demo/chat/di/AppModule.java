package com.iflytek.aiui.demo.chat.di;

import android.app.Application;
import android.content.Context;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger2 Module
 */

@Module(includes = ViewModelModule.class)
public class AppModule {
    @Provides
    @Singleton
    public Context providesContext(Application application) {
        return application;
    }

    @Provides
    @Singleton
    public AIUIPlayer providePlayer(Application application) {
        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        SimpleExoPlayer player =
                ExoPlayerFactory.newSimpleInstance(application, trackSelector);

        return new AIUIPlayer(application, player);
    }

    @Provides
    @Singleton
    public ScheduledExecutorService provideExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
