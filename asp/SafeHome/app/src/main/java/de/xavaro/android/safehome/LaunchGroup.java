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
import android.view.ViewGroup;
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

    protected int realWidth;
    protected int realHeight;

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
    protected int launchPage;

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

        bubble = new FrameLayout(context);
        LayoutParams lp = new LayoutParams(200,100);
        lp.leftMargin = 400;
        lp.topMargin = 200;
        bubble.setLayoutParams(lp);
        //bubble.setBackground(StaticUtils.getflippedDrawable(getContext(), R.drawable.bubble, false, false, 0));
        bubble.setBackground(VersionUtils.getDrawableFromResources(getContext(), R.drawable.bubble));
        this.addView(bubble);
    }

    private FrameLayout bubble;

    private void addSliderButtons()
    {
        Bitmap bmp;
        BitmapDrawable bmpDraw;

        final int MP = FrameLayout.LayoutParams.MATCH_PARENT;

        arrowLeft = new FrameLayout(getContext());
        arrowLeft.setLayoutParams(new FrameLayout.LayoutParams(32, MP, Gravity.START));
        arrowLeft.setBackgroundColor(GlobalConfigs.LaunchArrowBackgroundColor);
        arrowLeft.setOnTouchListener(this);

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
        arrowRight.setOnTouchListener(this);

        FrameLayout ari = new FrameLayout(getContext());
        ari.setLayoutParams(new FrameLayout.LayoutParams(MP, 6 * 96, Gravity.CENTER_VERTICAL));
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_right);
        bmpDraw = new BitmapDrawable(getResources(),bmp);
        bmpDraw.setTileModeXY(Shader.TileMode.CLAMP, Shader.TileMode.REPEAT);
        ari.setBackground(bmpDraw);

        arrowRight.addView(ari);
        this.addView(arrowRight);
    }

    private boolean touchValid;
    private int xStartTouch;
    private int yStartTouch;
    private int xLastTouch;
    private int yLastTouch;
    private int xDirTouch;
    private int yDirTouch;

    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        if (view == this)
        {
            //
            // Kill all clicks between items.
            //

            return false;
        }

        int xscreen = (int) motionEvent.getRawX();
        int yscreen = (int) motionEvent.getRawY();

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            xStartTouch = xLastTouch = xscreen;
            yStartTouch = yLastTouch = yscreen;

            touchValid = false;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
        {
            if (! touchValid)
            {
                if ((Math.abs(xscreen - xStartTouch) >= 10) || (Math.abs(yscreen - yStartTouch) >= 10))
                {
                    //
                    // The touch needs to move a bit before we accept it
                    // and it just became valid.
                    //

                    touchValid = true;
                }

                return true;
            }

            xDirTouch = (xLastTouch > xscreen) ? -1 : 1;
            yDirTouch = (yLastTouch > yscreen) ? -1 : 1;

            xLastTouch = xscreen;
            yLastTouch = yscreen;

            if ((view == arrowLeft) && (launchPage > 0))
            {
                FrameLayout lpage = launchPages.get(launchPage - 1);

                LayoutParams lparams = (LayoutParams) lpage.getLayoutParams();
                lparams.leftMargin = xscreen - lpage.getWidth();
                lpage.bringToFront();
            }

            if ((view == arrowRight) && ((launchPage + 1) < launchPages.size()))
            {
                FrameLayout lpage = launchPages.get(launchPage + 1);

                LayoutParams lparams = (LayoutParams) lpage.getLayoutParams();
                lparams.leftMargin = xscreen;
                lpage.bringToFront();
            }
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            Log.d(LOGTAG,"funal=" + xscreen + "=>" + xLastTouch);

            FrameLayout lpage = null;
            int finalMargin = 0;

            if ((view == arrowLeft) && (launchPage > 0))
            {
                launchPage--;

                lpage = launchPages.get(launchPage);

                if (xDirTouch < 0)
                {
                    finalMargin = -lpage.getWidth();
                    launchPage++;
                }
            }

            if ((view == arrowRight) && ((launchPage + 1) < launchPages.size()))
            {
                launchPage++;

                lpage = launchPages.get(launchPage);

                if (xDirTouch > 0)
                {
                    finalMargin = lpage.getWidth();
                    launchPage--;
                }
            }

            if (lpage != null)
            {
                LayoutParams from = (LayoutParams) lpage.getLayoutParams();
                LayoutParams toto = new LayoutParams(lpage.getLayoutParams());
                toto.leftMargin = finalMargin;

                DitUndDat.Animator animator = new DitUndDat.Animator();

                animator.setDuration(500);
                animator.setLayout(lpage, from, toto);
                animator.setFinalCall(animationFinished);

                this.startAnimation(animator);
            }
        }

        return true;
    }

    private void adjustArrows()
    {
        arrowLeft.setVisibility((launchPage > 0) ? VISIBLE : INVISIBLE);
        arrowRight.setVisibility(((launchPage + 1) < launchPages.size()) ? VISIBLE : INVISIBLE);

        arrowLeft.bringToFront();
        arrowRight.bringToFront();
    }

    private Runnable animationFinished = new Runnable()
    {
        @Override
        public void run()
        {
            adjustArrows();
        }
    };

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        if (changed)
        {
            Log.d(LOGTAG,"onLayout:" + changed + "=" + left + "=" + top + "=" + right + "=" + bottom);

            positionLaunchItems();
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width  = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if ((width != realWidth) || (height != realHeight))
        {
            realWidth  = width;
            realHeight = height;

            horzItems = realWidth / horzSize;
            vertItems = realHeight / vertSize;

            horzSpace = (realWidth - (horzItems * horzSize)) / (horzItems + 1);
            vertSpace = (realHeight - (vertItems * vertSize)) / vertItems;

            horzStart = ((realWidth - (horzItems * horzSize)) % (horzItems + 1)) / 2;
            vertStart = ((realHeight - (vertItems * vertSize)) % vertItems) / 2;

            horzStart += horzSpace;
            vertStart += vertSpace / 2;

            if (launchItems == null)
            {
                createLaunchItems();
            }
            else
            {
                for (FrameLayout lp : launchPages)
                {
                    LayoutParams lpar = (LayoutParams) lp.getLayoutParams();
                    lpar.width = realWidth;
                    lpar.height = realHeight;
                    lp.setLayoutParams(lpar);
                }
            }

            Log.d(LOGTAG, "onMeasure:" + width + "=" + height);
            Log.d(LOGTAG, "onMeasure:" + horzItems + "/" + vertItems + " <=> " + horzSpace + "/" + vertSpace);
        }

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
            lp.setLayoutParams(new FrameLayout.LayoutParams(realWidth,realHeight));
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
                    lp.setLayoutParams(new FrameLayout.LayoutParams(realWidth,realHeight));
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

        launchPage = 0;
        launchPages.get(launchPage).bringToFront();

        adjustArrows();

        bubble.bringToFront();
    }

    public void setConfig(LaunchItem parent, JSONObject config)
    {
        this.parent = parent;
        this.config = config;
    }

    @Nullable
    public LaunchItem getLaunchItem()
    {
        return this.parent;
    }
}
