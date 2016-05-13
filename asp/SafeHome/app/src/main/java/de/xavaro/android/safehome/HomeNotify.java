package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeNotify extends FrameLayout
{
    private static final String LOGTAG = HomeNotify.class.getSimpleName();

    private int layoutSize;
    private int buddySize;

    private LayoutParams layoutParams;
    private FrameLayout innerFrame;

    private int orientation;

    public HomeNotify(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);
        setPadding(8, 8, 8, 8);

        innerFrame = new FrameLayout(context);
        addView(innerFrame);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(16);
        gd.setColor(0xffffffff);
        gd.setStroke(2, 0xffcccccc);

        innerFrame.setBackground(gd);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    public void setSize(int layoutSize, int buddySize)
    {
        this.layoutSize = layoutSize;
        this.buddySize = buddySize;

        layoutParams.width = Simple.MP;
        layoutParams.height = layoutSize;
        layoutParams.gravity = Gravity.TOP;
    }

    public void setConfig(JSONObject config)
    {
    }

    private Runnable changeOrientation = new Runnable()
    {
        @Override
        public void run()
        {
            if ((orientation != Configuration.ORIENTATION_PORTRAIT) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT))
            {
                layoutParams.rightMargin = 0;

                orientation = Configuration.ORIENTATION_PORTRAIT;
            }

            if ((orientation != Configuration.ORIENTATION_LANDSCAPE) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE))
            {
                layoutParams.rightMargin = buddySize;

                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Simple.makePost(changeOrientation);
    }
}
