package de.xavaro.android.safehome;

import android.graphics.Color;
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
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;

import de.xavaro.android.common.Animator;
import de.xavaro.android.common.BackKeyMaster;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
import de.xavaro.android.common.VersionUtils;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentResolver;

//
// Launch item view on home screen.
//

public class LaunchGroup extends FrameLayout implements
        View.OnTouchListener,
        VoiceIntentResolver
{
    private static final String LOGTAG = LaunchGroup.class.getSimpleName();

    protected Context context;

    protected LaunchItem parent;
    protected JSONObject config;
    protected String configTree;

    protected int horzSize = CommonConfigs.LaunchItemSize;
    protected int vertSize = CommonConfigs.LaunchItemSize;

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

    protected ArrayList<FrameLayout> launchPages;
    protected ArrayList<LaunchItem> launchItems;

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
        arrowLeft = new FrameLayout(getContext());
        arrowLeft.setLayoutParams(new FrameLayout.LayoutParams(32, Simple.MP, Gravity.START));
        arrowLeft.setOnTouchListener(this);

        this.addView(arrowLeft);

        arrowRight = new FrameLayout(getContext());
        arrowRight.setLayoutParams(new FrameLayout.LayoutParams(32, Simple.MP, Gravity.END));
        arrowRight.setOnTouchListener(this);

        this.addView(arrowRight);
    }

    public void animateNextPage()
    {
        animatePage(arrowRight, true);
    }

    public void animatePrevPage()
    {
        if (launchPage == 0)
        {
            HomeActivity.getInstance().onBackKeyExecuteNow();

            return;
        }

        animatePage(arrowLeft, true);
    }

    private void animatePage(View view, boolean clickmove)
    {
        FrameLayout lpage = null;
        int finalMargin = 0;

        if ((view == arrowLeft) && (launchPage > 0))
        {
            launchPage--;

            lpage = launchPages.get(launchPage);

            if (clickmove)
            {
                LayoutParams lparams = (LayoutParams) lpage.getLayoutParams();
                lparams.leftMargin = -getWidth();
                lpage.bringToFront();
            }
            else
            {
                if (xDirTouch < 0)
                {
                    finalMargin = -getWidth();
                    launchPage++;
                }
            }
        }

        if ((view == arrowRight) && ((launchPage + 1) < launchPages.size()))
        {
            launchPage++;

            lpage = launchPages.get(launchPage);

            if (clickmove)
            {
                LayoutParams lparams = (LayoutParams) lpage.getLayoutParams();
                lparams.leftMargin = getWidth();
                lpage.bringToFront();
            }
            else
            {
                if (xDirTouch > 0)
                {
                    finalMargin = getWidth();
                    launchPage--;
                }
            }
        }

        if (lpage != null)
        {
            LayoutParams from = (LayoutParams) lpage.getLayoutParams();
            LayoutParams toto = new LayoutParams(lpage.getLayoutParams());
            toto.leftMargin = finalMargin;

            Animator animator = new Animator();

            animator.setDuration(500);
            animator.setLayout(lpage, from, toto);
            animator.setFinalCall(animationFinished);

            this.startAnimation(animator);
        }
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

            return true;
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
            if (!touchValid)
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
                lparams.leftMargin = xscreen - getWidth();
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
            animatePage(view, false);
        }

        return true;
    }

    private void adjustArrows()
    {
        arrowLeft.setVisibility(((launchPage - 1) >= 0) ? VISIBLE : INVISIBLE);
        arrowRight.setVisibility(((launchPage + 1) < launchPages.size()) ? VISIBLE : INVISIBLE);

        arrowLeft.bringToFront();
        arrowRight.bringToFront();
    }

    private final Runnable animationFinished = new Runnable()
    {
        @Override
        public void run()
        {
            adjustArrows();
        }
    };

    private final Runnable afterMeasure = new Runnable()
    {
        @Override
        public void run()
        {
            if (((measuredWidth != realWidth) || (measuredHeight != realHeight)) &&
                    ((measuredWidth / horzSize) != 0) && ((measuredHeight / vertSize) != 0))
            {
                realWidth = measuredWidth;
                realHeight = measuredHeight;

                horzItems = realWidth / horzSize;
                vertItems = realHeight / vertSize;

                while (true)
                {
                    horzSpace = (realWidth - (horzItems * horzSize)) / (horzItems + 1);
                    vertSpace = (realHeight - (vertItems * vertSize)) / vertItems;

                    horzStart = ((realWidth - (horzItems * horzSize)) % (horzItems + 1)) / 2;
                    vertStart = ((realHeight - (vertItems * vertSize)) % vertItems) / 2;

                    horzStart += horzSpace;
                    vertStart += vertSpace / 2;

                    if ((horzSpace >= 4) && (vertSpace >= 4)) break;

                    if (horzSpace < 6) horzItems--;
                    if (vertSpace < 6) vertItems--;
                }

                if (launchItems == null) createLaunchItems();

                positionLaunchItems();
            }
        }
    };

    protected int measuredWidth;
    protected int measuredHeight;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        getHandler().post(afterMeasure);
    }

    public void deleteLaunchItem(LaunchItem item)
    {
        if (!launchItems.contains(item)) return;

        ((ViewGroup) item.getParent()).removeView(item);
        launchItems.remove(item);
        positionLaunchItems();
    }

    private int getXpos(int slot)
    {
        return horzStart + (slot % horzItems) * (horzSize + horzSpace);
    }

    private int getYpos(int slot)
    {
        return vertStart + (slot / horzItems) * (vertSize + vertSpace);
    }

    private FrameLayout recycleLaunchPage()
    {
        if (launchPages.size() > 0)
        {
            FrameLayout lp = launchPages.remove(0);

            int childs = lp.getChildCount();

            for (int inx = 0; inx < childs; inx++)
            {
                if (lp.getChildAt(inx) instanceof LaunchItemNextPrev)
                {
                    lp.removeView(lp.getChildAt(inx--));
                }
            }

            return lp;
        }

        FrameLayout lp = new FrameLayout(context);
        lp.setBackgroundColor(Color.WHITE);
        this.addView(lp);

        return lp;
    }

    protected void positionLaunchItems()
    {
        ArrayList<FrameLayout> newPages = new ArrayList<>();

        int maxSlots = horzItems * vertItems;

        int nextSlot = 0;

        FrameLayout lp = recycleLaunchPage();
        newPages.add(lp);

        for (LaunchItem li : launchItems)
        {
            if ((nextSlot == 0) && (parent != null))
            {
                JSONObject prev = new JSONObject();
                Json.put(prev, "type", "prev");
                Json.put(prev, "label", "Zurück");

                LaunchItem liprev = LaunchItem.createLaunchItem(context, this, prev);

                liprev.setSize(horzSize, vertSize);
                liprev.setPosition(getXpos(nextSlot), getYpos(nextSlot));

                lp.addView(liprev);

                nextSlot++;
            }

            if (li.getParent() != lp)
            {
                Simple.removeFromParent(li);
                lp.addView(li);
            }

            li.setPosition(getXpos(nextSlot), getYpos(nextSlot));

            nextSlot++;

            if ((nextSlot + 1) >= maxSlots)
            {
                //
                // Next icon on last position of page.
                //

                JSONObject next = new JSONObject();
                Json.put(next, "type", "next");
                Json.put(next, "label", "Weiter");

                LaunchItem linext = LaunchItem.createLaunchItem(context, this, next);

                linext.setSize(horzSize, vertSize);
                linext.setPosition(getXpos(nextSlot), getYpos(nextSlot));

                lp.addView(linext);

                //
                // Create new page.
                //

                nextSlot = 0;

                lp = recycleLaunchPage();
                newPages.add(lp);

                //
                // Prev icon on first position of page.
                //

                JSONObject prev = new JSONObject();
                Json.put(prev, "type", "prev");
                Json.put(prev, "label", "Zurück");

                LaunchItem liprev = LaunchItem.createLaunchItem(context, this, prev);

                liprev.setSize(horzSize, vertSize);
                liprev.setPosition(getXpos(nextSlot), getYpos(nextSlot));
                lp.addView(liprev);

                nextSlot++;
            }
        }

        while (launchPages.size() > 0)
        {
            lp = launchPages.remove(0);
            Simple.removeFromParent(lp);
        }

        launchPages = newPages;

        launchPage = 0;
        launchPages.get(launchPage).bringToFront();

        adjustArrows();
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

            int numItems = lis.length();

            for (int inx = 0; inx < numItems; inx++)
            {
                JSONObject lit = lis.getJSONObject(inx);

                if (lit.has("enable"))
                {
                    String key = lit.getString("enable");

                    if (! StaticUtils.getSharedPrefsBoolean(context, key))
                    {
                        continue;
                    }
                }

                LaunchItem li = LaunchItem.createLaunchItem(context, this, lit);
                li.setSize(horzSize, vertSize);
                launchItems.add(li);
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    public void setConfig(LaunchItem parent, JSONObject config)
    {
        this.parent = parent;
        this.config = config;
    }

    public void setConfig(LaunchItem parent, JSONArray launchitems)
    {
        JSONObject config = new JSONObject();
        Json.put(config, "launchitems", launchitems);

        this.parent = parent;
        this.config = config;
    }

    public void activateLaunchItem(String type, String subtype)
    {
        activateLaunchItem(this, type, subtype);
    }

    private void activateLaunchItem(LaunchGroup directory, String type, String subtype)
    {
        if (directory != null)
        {
            if (directory.launchItems != null)
            {
                //
                // Find already created launch items.
                //

                for (LaunchItem launchItem : directory.launchItems)
                {
                    if (Simple.equals(type, launchItem.type)
                            && Simple.equals(subtype, launchItem.subtype))
                    {
                        launchItem.onMyClick();
                    }

                    activateLaunchItem(launchItem.directory, type, subtype);
                }
            }

            if ((directory.config != null) && directory.config.has("launchitems"))
            {
                //
                // Find configured launch items.
                //

                JSONArray launchitems = Json.getArray(directory.config, "launchitems");

                if (launchitems == null) return;

                for (int inx = 0; inx < launchitems.length(); inx++)
                {
                    JSONObject launchitem = Json.getObject(launchitems, inx);
                    if (launchitem == null) continue;

                    String itype = Json.getString(launchitem, "type");
                    String isubtype = Json.getString(launchitem, "subtype");
                }
            }
        }
    }

    @Nullable
    public LaunchItem getLaunchItem()
    {
        return this.parent;
    }

    @Override
    public void onCollectVoiceIntent(VoiceIntent voiceintent)
    {
        Log.d(LOGTAG,"onCollectVoiceIntent:" + voiceintent.getCommand());

        if (launchItems != null)
        {
            //
            // Find already created launch items.
            //

            for (LaunchItem launchItem : launchItems)
            {
                launchItem.onCollectVoiceIntent(voiceintent);
            }

            return;
        }

        if (config != null)
        {
            if (config.has("launchitems"))
            {
                //
                // Inspect configured launch items.
                //

                JSONArray launchitems = Json.getArray(config, "launchitems");
                if (launchitems == null) return;

                for (int inx = 0; inx < launchitems.length(); inx++)
                {
                    JSONObject launchitem = Json.getObject(launchitems, inx);

                    JSONObject intent = Json.getObject(launchitem, "intent");
                    voiceintent.collectIntent(launchitem, intent);

                    JSONArray intents = Json.getArray(launchitem, "intents");
                    voiceintent.collectIntents(launchitem, intents);
                }
            }
        }
    }

    @Override
    public void onResolveVoiceIntent(VoiceIntent voiceintent)
    {
        Log.d(LOGTAG,"onResolveVoiceIntent:" + voiceintent.getCommand());

        if (launchItems != null)
        {
            //
            // Find already created launch items.
            //

            for (LaunchItem launchItem : launchItems)
            {
                launchItem.onResolveVoiceIntent(voiceintent);
            }
        }
        else
        {
            if ((config != null) && config.has("launchitems"))
            {
                //
                // Inspect configured launch items.
                //

                JSONArray launchitems = Json.getArray(config, "launchitems");

                if (launchitems == null) return;

                for (int inx = 0; inx < launchitems.length(); inx++)
                {
                    JSONObject launchitem = Json.getObject(launchitems, inx);
                    if (launchitem == null) continue;

                    JSONObject intent = Json.getObject(launchitem, "intent");
                    voiceintent.evaluateIntent(launchitem, intent);

                    JSONArray intents = Json.getArray(launchitem, "intents");
                    voiceintent.evaluateIntents(launchitem, intents);
                }
            }
        }
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        Log.d(LOGTAG,"onExecuteVoiceIntent:" + voiceintent.getCommand());

        if (launchItems != null)
        {
            //
            // Find already created launch items.
            //

            for (LaunchItem launchItem : launchItems)
            {
                if (launchItem.onExecuteVoiceIntent(voiceintent, index))
                {
                    return true;
                }
            }
        }
        else
        {
        }

        return false;
    }
}
