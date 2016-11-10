package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Json;

public class HealthECGDisplay extends LaunchFrame
{
    private static final String LOGTAG = HealthECGDisplay.class.getSimpleName();

    private Paint myPaint;

    private JSONObject ecg;
    private JSONObject ecgInf;

    private int samplingRate;

    public HealthECGDisplay(Context context, LaunchItem parent)
    {
        super(context, parent);

        myInit();
    }

    private void myInit()
    {
        Log.d(LOGTAG,"myInit: pupsi");

        myPaint = new Paint();

        setBackgroundColor(Color.WHITE);
    }

    public void setConfig(JSONObject ecg)
    {
        this.ecg = ecg;

        ecgInf = Json.getObject(ecg, "inf");

        samplingRate = Json.getInt(ecgInf, "SamplingRate");
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int wid = canvas.getWidth();
        int hei = canvas.getHeight();

        Log.d(LOGTAG,"onDraw: wid=" + wid + " hei=" + hei);

        myPaint.setColor(0x8000ff00);
        myPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawRect(0.0f, 0.0f, wid / 2, hei / 2, myPaint);

        if (samplingRate == 0) return;

        int rowSeconds;
        int totalRows;
        int samplesPerRow;

        if (hei > wid)
        {
            //
            // Portrait: 6 Rows with 5 s
            //

            rowSeconds = 5;
            totalRows = 6;
        }
        else
        {
            //
            // Landscape: 3 Rows with 10 s
            //

            rowSeconds = 10;
            totalRows = 3;
        }

        samplesPerRow = samplingRate * rowSeconds;

        final int ecg_grid1 = 0xfffad2f0;
        final int ecg_grid2 = 0xffffc0cb;
        final int ecg_grid3 = 0xffff7a7a;

        float iGrid = wid / (float) rowSeconds;
        float iCell = wid / (float) (rowSeconds * 5);
        float iTiny = wid / (float) (rowSeconds * 5 * 5);

        int siz = Math.max(wid, hei);

        for (float inx = 0; inx < hei; inx += iGrid)
        {
            myPaint.setColor(ecg_grid3);
            myPaint.setStrokeWidth(3.0f);

            canvas.drawLine(0.0f, inx, wid, inx, myPaint);
            canvas.drawLine(inx, 0.0f, inx, hei, myPaint);

            for (float jnx = inx; jnx < ((inx + hei) - iCell) + 1.0f; jnx += iCell)
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
                    canvas.drawLine(jnx + k, 0.0f, jnx + k, wid, myPaint);
                }
            }
        }
    }
}
