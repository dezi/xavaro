package de.xavaro.android.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.SeekBar;
import android.view.Gravity;
import android.util.AttributeSet;

public class VideoControl extends FrameLayout
{
    private static final String LOGTAG = VideoControl.class.getSimpleName();

    private TextView timeStart;
    private TextView timeUntil;
    private SeekBar seekBar;

    public VideoControl(Context context)
    {
        super(context);

        init();
    }

    public VideoControl(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    public VideoControl(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    public void init()
    {
        LayoutParams lp;

        lp = new LayoutParams(Simple.WC, Simple.WC, Gravity.BOTTOM + Gravity.START);

        timeStart = new TextView(getContext());
        timeStart.setLayoutParams(lp);
        timeStart.setPadding(25, 0, 0, 0);
        timeStart.setTextColor(0xff888888);
        timeStart.setTextSize(Simple.getDeviceTextSize(22f));

        addView(timeStart);

        lp = new LayoutParams(Simple.WC, Simple.WC, Gravity.BOTTOM + Gravity.END);

        timeUntil = new TextView(getContext());
        timeUntil.setLayoutParams(lp);
        timeUntil.setPadding(0, 0, 25, 0);
        timeUntil.setTextColor(0xff888888);
        timeUntil.setTextSize(Simple.getDeviceTextSize(22f));

        addView(timeUntil);

        seekBar = new SeekBar(getContext());
        seekBar.setPadding(25, 0, 25, 8);

        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.setIntrinsicHeight(50);
        thumb.setIntrinsicWidth(40);
        thumb.getPaint().setColor(0xaaaaaaaa);

        seekBar.setThumb(thumb);
        seekBar.getProgressDrawable().setColorFilter(0xffcccccc, PorterDuff.Mode.SRC_IN);

        this.addView(seekBar);
    }

    public void setDuration(int duration)
    {
        seekBar.setMax(duration);

        int minutes =  (duration / 1000) / 60;
        int seconds =  (duration / 1000) % 60;

        String start = "00:00";
        String until = String.format("%2d:%02d", minutes, seconds);

        timeStart.setText(start);
        timeUntil.setText(until);
    }

    public void setCurrentPosition(int current)
    {
        seekBar.setProgress(current);
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener)
    {
        seekBar.setOnSeekBarChangeListener(listener);
    }
}