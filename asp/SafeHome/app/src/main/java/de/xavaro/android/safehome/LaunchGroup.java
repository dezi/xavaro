package de.xavaro.android.safehome;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
// Launch item view on home screen.
//

public class LaunchGroup extends FrameLayout
{
    protected final String LOGTAG = "LaunchGroup";

    protected Context context;

    protected JSONObject config;

    protected int horzSize = 220;
    protected int vertSize = 220;

    protected int horzItems;
    protected int vertItems;
    protected int horzSpace;
    protected int vertSpace;

    protected ArrayList<LaunchItem> launchItems = null;

    public LaunchGroup(Context context)
    {
        super(context);

        myInit(context);
    }

    public LaunchGroup(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public LaunchGroup(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    protected void myInit(Context context)
    {
        this.context = context;

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

        positionLaunchItems();

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width  = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        horzItems = width  / horzSize;
        vertItems = height / vertSize;

        horzSpace = (width  - (horzItems * horzSize)) / horzItems;
        vertSpace = (height - (vertItems * vertSize)) / vertItems;

        Log.d(LOGTAG, "onMeasure:" + width + "=" + height);
        Log.d(LOGTAG, "onMeasure:" + horzItems + "/" + vertItems + " <=> " + horzSpace + "/" + vertSpace);

        if (launchItems == null) createLaunchItems();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void positionLaunchItems()
    {
        int col = 0;
        int row = 0;

        int xpos = horzSpace / 2;
        int ypos = vertSpace / 2;

        for (LaunchItem li : launchItems)
        {
            if (row >= vertItems)
            {
                Toast.makeText(context,"Zu viele LaunchItems!!!!",Toast.LENGTH_LONG).show();

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

    protected void createLaunchItems()
    {
        launchItems = new ArrayList<>();

        if ((config == null) || ! config.has("launchitems"))
        {
            Toast.makeText(context, "Keine <launchitems> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            JSONArray lis = config.getJSONArray("launchitems");

            int numItems = horzItems * vertItems;
            int maxSlots = lis.length();

            for (int inx = 0; inx < numItems; inx++)
            {
                LaunchItem li = new LaunchItem(context);
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
