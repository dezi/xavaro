package de.xavaro.android.common;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class VideoControl extends FrameLayout
{
    private static final String LOGTAG = VideoControl.class.getSimpleName();

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
        setLayoutParams(new FrameLayout.LayoutParams(Simple.MP, 80, Gravity.BOTTOM));
        setBackgroundColor(0x00000000);
        setPadding(40, 15, 40, 15);

        seekBar = new SeekBar(getContext());

        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());
        thumb.setIntrinsicHeight(50);
        thumb.setIntrinsicWidth(40);
        thumb.getPaint().setColor(0xcccccccc);

        seekBar.setThumb(thumb);

        this.addView(seekBar);
    }

    public void setDuration(int duration)
    {
        seekBar.setMax(duration);
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