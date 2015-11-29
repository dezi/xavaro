package de.xavaro.android.safehome;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// Launch item view on home screen.
//

public class LaunchGroup extends FrameLayout
{
    private final String LOGTAG = "LaunchGroup";

    private JSONObject config;

    private int horzSize = 220;
    private int vertSize = 220;

    private int horzItems;
    private int vertItems;
    private int horzSpace;
    private int vertSpace;

    private ArrayList<LaunchItem> launchItems = null;

    public LaunchGroup(Context context)
    {
        super(context);

        myInit();
    }

    public LaunchGroup(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit();
    }

    public LaunchGroup(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit();
    }

    private void myInit()
    {
        setBackgroundColor(0xffffffee);

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        setLayoutParams(layout);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        Log.d(LOGTAG,"onLayout:" + changed + "=" + left + "=" + top + "=" + right + "=" + bottom);

        int width  = right - left;
        int height = bottom - top;

        horzItems = width  / horzSize;
        vertItems = height / vertSize;

        horzSpace = (width  - (horzItems * horzSize)) / horzItems;
        vertSpace = (height - (vertItems * vertSize)) / vertItems;

        Log.d(LOGTAG, "onLayout:" + horzItems + "/" + vertItems + " <=> " + horzSpace + "/" + vertSpace);

        if (launchItems == null) createLaunchItems();

        positionLaunchItems();

        super.onLayout(changed, left, top, right, bottom);
    }

    private void positionLaunchItems()
    {
        int col = 0;
        int row = 0;

        int xpos = horzSpace / 2;
        int ypos = vertSpace / 2;

        for (LaunchItem li : launchItems)
        {
            if (row >= vertItems)
            {
                Toast.makeText(getContext(),"Zu viele LaunchItems!!!!",Toast.LENGTH_LONG).show();

                break;
            }

            li.setPosition(xpos, ypos);

            //Log.d(LOGTAG, "LI " + xpos + "/" + ypos);

            xpos += horzSize + horzSpace;

            if (++col >= horzItems)
            {
                xpos = horzSpace / 2;

                ypos += vertSize + vertSpace;

                col = 0;
                row++;
            }
        }
    }

    private void createLaunchItems()
    {
        launchItems = new ArrayList<>();

        if ((config == null) || ! config.has("launchitems"))
        {
            Toast.makeText(getContext(), "Keine <launchitems> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            JSONArray lis = config.getJSONArray("launchitems");

            int numItems = horzItems * vertItems;
            int maxSlots = lis.length();

            for (int inx = 0; inx < numItems; inx++)
            {
                LaunchItem li = new LaunchItem(getContext());
                li.setSize(horzSize, vertSize);

                if (inx < maxSlots) li.setConfig(lis.getJSONObject(inx));

                launchItems.add(li);
                addView(li);
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    public void setConfig(JSONObject config)
    {
        this.config = config;
    }

    public int getNumLaunchItems()
    {
        return (launchItems == null) ? 0 : launchItems.size();
    }

    public LaunchItem geLaunchItem(int inx)
    {
        return (launchItems == null) ? null : launchItems.get(inx);
    }
}
