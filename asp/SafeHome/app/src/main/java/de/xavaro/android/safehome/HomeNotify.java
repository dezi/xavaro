package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeNotify extends HomeFrame
{
    private static final String LOGTAG = HomeNotify.class.getSimpleName();

    protected LinearLayout contentFrame;

    protected HomeEvent topFrame;
    protected HomeEvent event1Frame;
    protected HomeEvent event2Frame;

    public HomeNotify(Context context)
    {
        super(context);

        layoutParams.width = Simple.MP;
        layoutParams.height = notifySize - 16;
        layoutParams.gravity = Gravity.TOP;

        layoutParams.leftMargin = 8;
        layoutParams.topMargin = 8;
        layoutParams.rightMargin = isPortrait() ? 8 : peopleSize;
        layoutParams.bottomMargin = 8;

        layoutNormal = new LayoutParams(layoutParams);

        //
        // Notify content areas.
        //
        
        setDisablefullscreen();

        int padh = Simple.getDevicePixels(16);
        int padv = Simple.getDevicePixels(4);

        LayoutParams barLayout = new LayoutParams(Simple.MP, Simple.getDevicePixels(2));
        FrameLayout barFrame;

        payloadFrame.setPadding(padh, 0, padh, 0);

        contentFrame = new LinearLayout(context);
        contentFrame.setOrientation(LinearLayout.VERTICAL);
        payloadFrame.addView(contentFrame);

        topFrame = new HomeEvent(context);
        contentFrame.addView(topFrame);

        barFrame = new FrameLayout(context);
        barFrame.setLayoutParams(barLayout);
        barFrame.setPadding(0, padv, 0, padv);
        barFrame.setBackgroundColor(0xffcccccc);
        contentFrame.addView(barFrame);

        event1Frame = new HomeEvent(context);
        contentFrame.addView(event1Frame);

        barFrame = new FrameLayout(context);
        barFrame.setLayoutParams(barLayout);
        barFrame.setPadding(0, padv, 0, padv);
        barFrame.setBackgroundColor(0xffcccccc);
        contentFrame.addView(barFrame);

        event2Frame = new HomeEvent(context);
        contentFrame.addView(event2Frame);
    }

    public void setConfig(JSONObject config)
    {
    }

    @Override
    protected void onChangeOrientation()
    {
        layoutNormal.height = notifySize - 16;
        layoutNormal.rightMargin = isPortrait() ? 8 : peopleSize;

        if (! fullscreen)
        {
            layoutParams.height = layoutNormal.height;
            layoutParams.rightMargin = layoutNormal.rightMargin;
        }

        int height = payloadFrame.getHeight() / 4;

        Log.d(LOGTAG, "========================> " + payloadFrame.getHeight() + "x" + payloadFrame.getWidth());

        //
        // Important: Only set dimensions if different from
        // actual state because it will fire a chain of new
        // resize events if not.
        //

        if (topFrame.getLayoutHeight() != (height * 2))
        {
            topFrame.setLayoutHeight(height * 2);
            event1Frame.setLayoutHeight(height);
            event2Frame.setLayoutHeight(height);
        }
    }
}
