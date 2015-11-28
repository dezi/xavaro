package de.xavaro.android.safehome;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

//
// Launch item view on home screen.
//

public class LaunchGroup extends FrameLayout
{
    private final String LOGTAG = "LaunchGroup";

    private int horzSize = 180;
    private int vertSize = 180;

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
        setBackgroundColor(0x8000ff00);

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

        int numItems = horzItems * vertItems;

        for (int inx = 0; inx < numItems; inx++)
        {
            LaunchItem li = new LaunchItem(getContext());
            li.setSize(horzSize, vertSize);
            launchItems.add(li);
            addView(li);
        }
    }
}
