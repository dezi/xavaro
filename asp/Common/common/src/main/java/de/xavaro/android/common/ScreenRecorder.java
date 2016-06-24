package de.xavaro.android.common;

import android.app.Activity;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.content.Context;
import android.content.Intent;
import android.view.Surface;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.util.Log;

import java.io.IOException;

public class ScreenRecorder
{
    private static final String LOGTAG = ScreenRecorder.class.getSimpleName();

    private static final int REQUEST_CODE = 1000;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private android.media.MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private DisplayMetrics metrics;

    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public void create()
    {
        metrics = new DisplayMetrics();
        Simple.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mMediaRecorder = new android.media.MediaRecorder();

        mProjectionManager = (MediaProjectionManager) Simple.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != REQUEST_CODE) return;

        if (resultCode != Activity.RESULT_OK)
        {
            return;
        }

        Log.d(LOGTAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);

        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    public void onStartRecording()
    {
        initRecorder();
        shareScreen();
    }

    public void onStopRecording()
    {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        Log.v(LOGTAG, "Stopping Recording");
        stopScreenSharing();
    }

    private void shareScreen()
    {
        if (mMediaProjection == null)
        {
            Simple.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }

        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay()
    {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
    }

    private void initRecorder()
    {
        try
        {
            mMediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(android.media.MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(Environment
                    .getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS) + "/video.mp4");
            mMediaRecorder.setVideoSize(metrics.widthPixels, metrics.heightPixels);
            mMediaRecorder.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = Simple.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback
    {
        @Override
        public void onStop()
        {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(LOGTAG, "Recording Stopped");

            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing()
    {
        if (mVirtualDisplay == null)
        {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot be reused again
        destroyMediaProjection();
    }

    public void onDestroy()
    {
        stopScreenSharing();
        destroyMediaProjection();
    }

    private void destroyMediaProjection()
    {
        if (mMediaProjection != null)
        {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(LOGTAG, "MediaProjection Stopped");
    }
}
