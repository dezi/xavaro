package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;

public class HealthECGDisplay extends LaunchFrame
{
    private static final String LOGTAG = HealthECGDisplay.class.getSimpleName();

    private static final int ecg_grid1 = 0xfffad2f0;
    private static final int ecg_grid2 = 0xffffc0cb;
    private static final int ecg_grid3 = 0xffff7a7a;

    private Path myPath;
    private Paint myPaint;

    private int samplingRate;
    private JSONArray samplingData;

    public HealthECGDisplay(Context context, LaunchItem parent)
    {
        super(context, parent);

        myPath = new Path();
        myPaint = new Paint();

        setBackgroundColor(Color.WHITE);
    }

    public void setConfig(JSONObject ecg)
    {
        JSONObject ecgInf = Json.getObject(ecg, "inf");

        samplingData = Json.getArray(ecg, "efi");
        samplingRate = Json.getInt(ecgInf, "SamplingRate");
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int wid = canvas.getWidth();
        int hei = canvas.getHeight();

        Log.d(LOGTAG,"onDraw: wid=" + wid + " hei=" + hei);

        if ((samplingRate == 0) || (samplingData == null))
        {
            //
            // Todo: draw hint.
            //

            return;
        }

        int rowSeconds;
        int totalRows;

        if (hei > wid)
        {
            rowSeconds = 5;
            totalRows = 6;
        }
        else
        {
            rowSeconds = 7;
            totalRows = 4;
        }

        int samplesPerRow = samplingRate * rowSeconds;
        float pixelsPerSample = wid / (float) samplesPerRow;
        float pixelsPerAmp = pixelsPerSample;

        Log.d(LOGTAG, "onDraw: pixelsPerSample=" + pixelsPerSample);

        float iGrid = wid / (float) rowSeconds;
        float iCell = wid / (float) (rowSeconds * 5);
        float iTiny = wid / (float) (rowSeconds * 5 * 5);

        int siz = Math.max(wid, hei);

        for (float inx = 0; inx < siz; inx += iGrid)
        {
            myPaint.setColor(ecg_grid3);
            myPaint.setStrokeWidth(3.0f);

            canvas.drawLine(0.0f, inx, wid, inx, myPaint);
            canvas.drawLine(inx, 0.0f, inx, hei, myPaint);

            for (float jnx = inx; jnx < ((inx + siz) - iCell) + 1.0f; jnx += iCell)
            {
                myPaint.setColor(ecg_grid2);
                myPaint.setStrokeWidth(2.0f);

                canvas.drawLine(0.0f, jnx, wid, jnx, myPaint);
                canvas.drawLine(jnx, 0.0f, jnx, hei, myPaint);

                myPaint.setStrokeWidth(1.0f);
                myPaint.setColor(ecg_grid1);

                for (float k = iTiny; k < (iCell - iTiny) + 1.0f; k += iTiny)
                {
                    canvas.drawLine(0.0f, jnx + k, wid, jnx + k, myPaint);
                    canvas.drawLine(jnx + k, 0.0f, jnx + k, hei, myPaint);
                }
            }
        }

        int offset = samplingRate * 3; // Skip first three seconds from data.
        int maxoff = samplingData.length();

        float yorg;
        float ypos;
        float xpos;

        float sample;

        //
        // Preflight samples to get baseline offset.
        //

        float base = 0f;
        float minbase = +100000f;
        float maxbase = -100000f;

        yorg = iGrid / 2.0f;

        for (int inx = offset; inx < maxoff; inx++)
        {
            sample = Json.getInt(samplingData, inx) * pixelsPerAmp;
            ypos = yorg - sample;

            if (ypos < minbase) minbase = ypos;
            if (ypos > maxbase) maxbase = ypos;
        }

        float fitScale = iGrid / (maxbase - minbase);
        Log.d(LOGTAG, "onDraw: fitScale=" + fitScale);

        if ((fitScale >= 0.25f) && (fitScale <= 1.75f))
        {
            //
            // Redo baseline compute with new scale.
            //

            pixelsPerAmp *= fitScale;

            minbase = +100000f;
            maxbase = -100000f;

            for (int inx = offset; inx < maxoff; inx++)
            {
                sample = Json.getInt(samplingData, inx) * pixelsPerAmp;
                ypos = yorg - sample;

                if (ypos < minbase) minbase = ypos;
                if (ypos > maxbase) maxbase = ypos;
            }

            base = minbase;
        }

        Log.d(LOGTAG, "onDraw: minbase=" + minbase + " maxbase=" + maxbase + " base=" + base);
        Log.d(LOGTAG, "onDraw: pixelsPerAmp=" + pixelsPerAmp + " pixelsPerSample=" + pixelsPerSample);

        //
        // Draw samples.
        //

        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setColor(Color.rgb(0, 0, 0));
        myPaint.setStrokeWidth(5.0f);

        for (int row = 0; row < totalRows; row++)
        {
            yorg = (row * iGrid) + (iGrid / 2.0f) - base;

            if (offset >= maxoff) break;

            sample = Json.getInt(samplingData, offset - 1) * pixelsPerAmp;
            ypos = yorg - sample;

            myPath.reset();
            myPath.moveTo(0, ypos);

            for (int sinx = 0; sinx < samplesPerRow; sinx++)
            {
                sample = Json.getInt(samplingData, offset++) * pixelsPerAmp;;

                xpos = sinx * pixelsPerSample;
                ypos = yorg - sample;

                myPath.lineTo(xpos, ypos);

                if (offset >= maxoff) break;
            }

            canvas.drawPath(myPath, myPaint);
        }
    }
}
