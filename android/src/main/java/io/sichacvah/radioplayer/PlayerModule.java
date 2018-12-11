package io.sichacvah.radioplayer;

import android.content.Context;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import android.content.Intent;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

public class PlayerModule extends ReactContextBaseJavaModule {
    static ExoPlayer exoPlayer;

    private final static String TAG = "PLAYER_MODULE";
    public Context mContext = null;

    @Override
    public String getName() {
        return "PlayerModule";
    }

    public PlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }


    private void sendEvent(
        String eventName,
        Object params
    ) {
        getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
        Log.i("ReactNativeRadioPlayer", "PlayerModule: sendEvent (to JS): " + eventName);
    }

    @ReactMethod
    public void startPlayerService(String URL) {
        Intent serviceIntent = new Intent(mContext, NotificationService.class);
        serviceIntent.putExtra("RADIO_PATH", URL);
        serviceIntent.setAction("STARTFOREGROUND_ACTION");
        mContext.startService(serviceIntent);
    }

    @ReactMethod
    public void start(String URL) {
        if (exoPlayer != null) {
            exoPlayer.stop();
        }

        if (URL == null)
            URL = "http://stream8.tanitweb.com/zitounafm";
        Uri URI = Uri.parse(URL);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //Provides estimates of the currently available bandwidth.
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        DefaultLoadControl loadControl = new DefaultLoadControl();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext, "NurZitounaAndroid");
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        Handler mainHandler = new Handler();
        ExtractorMediaSource mediaSource = new ExtractorMediaSource(URI,
                dataSourceFactory,
                extractorsFactory,
                mainHandler,
                null);

        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);

        exoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == 3) {
                    WritableMap params = Arguments.createMap();
                    sendEvent("start", params);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });
    }

    @ReactMethod
    public void stop() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            WritableMap params = Arguments.createMap();
            sendEvent("stop", params);
        }
    }

    @ReactMethod
    public void stopPlayerService() {
        stop();
        Intent serviceIntent = new Intent(mContext, NotificationService.class);
        serviceIntent.setAction("PLAY_ACTION");
        mContext.startService(serviceIntent);
    }

    @ReactMethod
    public void setVolume(float volume) {
        if (exoPlayer != null) {
            WritableMap params = Arguments.createMap();
            double dVolume = (double) volume;
            params.putDouble("volume", dVolume);
            sendEvent("volume_changed", params);
        }
    }
}