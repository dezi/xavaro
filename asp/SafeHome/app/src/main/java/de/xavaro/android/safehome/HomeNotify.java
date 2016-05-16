package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.view.Gravity;

import org.json.JSONObject;

import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeNotify extends HomeFrame
{
    private static final String LOGTAG = HomeNotify.class.getSimpleName();

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
    }

}
