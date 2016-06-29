package de.xavaro.android.common;

import android.app.Activity;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.media.MediaRecorder;
import android.content.Intent;
import android.view.Surface;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.util.Log;

import java.io.IOException;

public class CaptureRecorder
{
    private static final String LOGTAG = CaptureRecorder.class.getSimpleName();

    private static CaptureRecorder instance;

    public static CaptureRecorder getInstance()
    {
        if (instance == null) instance = new CaptureRecorder();

        return instance;
    }

    private static final int REQUEST_CODE = 1000;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private android.media.MediaRecorder mMediaRecorder;
    private DisplayMetrics metrics;
    private boolean isRecording;

    public void toggleRecording()
    {
        if (isRecording)
        {
            onStopRecording();
        }
        else
        {
            onStartRecording();
        }
    }

    public void onCreate()
    {
        metrics = Simple.getMetrics();
        mMediaRecorder = new android.media.MediaRecorder();
        mProjectionManager = Simple.getMediaProjectionManager();
    }

    public void onDestroy()
    {
        stopScreenSharing();
        destroyMediaProjection();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(LOGTAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);

        if (requestCode != REQUEST_CODE) return;

        if (resultCode != Activity.RESULT_OK)
        {
            Simple.makeToast("User denied screen recording.");

            return;
        }

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
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
        stopScreenSharing();
    }

    private void shareScreen()
    {
        Log.d(LOGTAG, "shareScreen");

        if (mMediaProjection == null)
        {
            Log.d(LOGTAG, "shareScreen: startActivityForResult");

            Simple.getActContext().startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);

            return;
        }

        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
    }

    private VirtualDisplay createVirtualDisplay()
    {
        Log.d(LOGTAG, "createVirtualDisplay");

        return mMediaProjection.createVirtualDisplay("CaptureActivity",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
    }

    private void initRecorder()
    {
        Log.d(LOGTAG, "initRecorder");

        try
        {
            long now = Simple.nowAsTimeStamp();

            String filename = "screen-"
                    + Simple.getLocalDateInternal(now)
                    + "-"
                    + Simple.getLocalTimeInternal(now)
                    + ".mp4";

            int rotation = Simple.getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(Simple.getMediaPath("recordings") + "/" + filename);
            mMediaRecorder.setVideoSize(metrics.widthPixels, metrics.heightPixels);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncodingBitRate(2048 * 1000);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.prepare();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private void stopScreenSharing()
    {
        Log.d(LOGTAG, "stopScreenSharing");

        if (mVirtualDisplay != null) mVirtualDisplay.release();
        destroyMediaProjection();
        isRecording = false;
    }

    private void destroyMediaProjection()
    {
        Log.d(LOGTAG, "destroyMediaProjection");

        if (mMediaProjection != null)
        {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }
}
