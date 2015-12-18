package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;

//
// Launch item view on home screen.
//

public class LaunchGroup extends FrameLayout implements
        View.OnTouchListener

{
    protected final String LOGTAG = "LaunchGroup";

    protected Context context;

    protected LaunchItem parent;
    protected JSONObject config;
    protected String configTree;

    protected int horzSize = 220;
    protected int vertSize = 220;

    protected int horzItems;
    protected int vertItems;
    protected int horzStart;
    protected int vertStart;
    protected int horzSpace;
    protected int vertSpace;

    protected FrameLayout arrowLeft;
    protected FrameLayout arrowRight;

    protected ArrayList<FrameLayout> launchPages = null;
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

        setBackgroundColor(GlobalConfigs.LaunchPageBackgroundColor);

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        setLayoutParams(layout);

        addSliderButtons();

        setOnTouchListener(this);

        configTree = "launchitems";
    }

    private void addSliderButtons()
    {
        Bitmap bmp;
        BitmapDrawable bmpDraw;

        final int MP = FrameLayout.LayoutParams.MATCH_PARENT;

        arrowLeft = new FrameLayout(getContext());
        arrowLeft.setLayoutParams(new FrameLayout.LayoutParams(32, MP, Gravity.START));
        arrowLeft.setBackgroundColor(GlobalConfigs.LaunchArrowBackgroundColor);

        FrameLayout ali = new FrameLayout(getContext());
        ali.setLayoutParams(new FrameLayout.LayoutParams(MP, 6 * 96, Gravity.CENTER_VERTICAL));
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_left);
        bmpDraw = new BitmapDrawable(getResources(),bmp);
        bmpDraw.setTileModeXY(Shader.TileMode.CLAMP, Shader.TileMode.REPEAT);
        ali.setBackground(bmpDraw);

        arrowLeft.addView(ali);
        this.addView(arrowLeft);

        arrowRight = new FrameLayout(getContext());
        arrowRight.setLayoutParams(new FrameLayout.LayoutParams(32, MP, Gravity.END));
        arrowRight.setBackgroundColor(GlobalConfigs.LaunchArrowBackgroundColor);

        FrameLayout ari = new FrameLayout(getContext());
        ari.setLayoutParams(new FrameLayout.LayoutParams(MP, 6 * 96, Gravity.CENTER_VERTICAL));
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_right);
        bmpDraw = new BitmapDrawable(getResources(),bmp);
        bmpDraw.setTileModeXY(Shader.TileMode.CLAMP, Shader.TileMode.REPEAT);
        ari.setBackground(bmpDraw);

        arrowRight.addView(ari);
        this.addView(arrowRight);
    }

    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (view == this)
        {
            //
            // Kill all clicks between items.
            //

            return true;
        }

        return false;
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

        horzSpace = (width  - (horzItems * horzSize)) / (horzItems + 1);
        vertSpace = (height - (vertItems * vertSize)) / vertItems;

        horzStart = ((width  - (horzItems * horzSize)) % (horzItems + 1)) / 2;
        vertStart = ((height - (vertItems * vertSize)) % vertItems) / 2;

        horzStart += horzSpace;
        vertStart += vertSpace / 2;

        Log.d(LOGTAG, "onMeasure:" + width + "=" + height);
        Log.d(LOGTAG, "onMeasure:" + horzItems + "/" + vertItems + " <=> " + horzSpace + "/" + vertSpace);

        if (launchItems == null) createLaunchItems();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void positionLaunchItems()
    {
        int col = 0;
        int row = 0;

        int xpos = horzStart;
        int ypos = vertStart;

        for (LaunchItem li : launchItems)
        {
            li.setPosition(xpos, ypos);

            //Log.d(LOGTAG, "LI " + xpos + "/" + ypos);

            xpos += horzSize + horzSpace;

            if (++col >= horzItems)
            {
                xpos = horzStart;

                ypos += vertSize + vertSpace;

                col = 0;

                if (++row >= vertItems)
                {
                    ypos = vertStart;

                    row = 0;
                }
            }
        }
    }

    protected void createLaunchItems()
    {
        launchItems = new ArrayList<>();
        launchPages = new ArrayList<>();

        if ((config == null) || ! config.has(configTree))
        {
            Toast.makeText(context, "Keine <" + configTree + "> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            JSONArray lis = config.getJSONArray(configTree);

            int bgcol = GlobalConfigs.LaunchPageBackgroundColor;

            FrameLayout lp = new FrameLayout(context);
            lp.setBackgroundColor(bgcol);
            launchPages.add(lp);
            this.addView(lp);

            int numItems = lis.length();
            int maxSlots = horzItems * vertItems;
            int nextSlot = 0;

            for (int inx = 0; inx < numItems; inx++)
            {
                LaunchItem li = new LaunchItem(context);
                li.setSize(horzSize, vertSize);

                li.setConfig(this, lis.getJSONObject(inx));
                launchItems.add(li);

                if (nextSlot >= maxSlots)
                {
                    lp = new FrameLayout(context);
                    lp.setBackgroundColor(bgcol);
                    launchPages.add(lp);
                    this.addView(lp);

                    nextSlot = 0;
                }

                lp.addView(li);
                nextSlot++;
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }

        launchPages.get(0).bringToFront();

        arrowLeft.setVisibility(INVISIBLE);

        arrowRight.setVisibility((launchPages.size() > 1) ? VISIBLE : INVISIBLE);

        arrowLeft.bringToFront();
        arrowRight.bringToFront();
    }

    public void setConfig(LaunchItem parent, JSONObject config)
    {
        this.parent = parent;
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

    @Nullable
    public LaunchItem getLaunchItem()
    {
        return this.parent;
    }
}
