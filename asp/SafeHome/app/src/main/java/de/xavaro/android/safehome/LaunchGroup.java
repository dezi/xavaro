package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.util.AttributeSet;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;

import de.xavaro.android.common.Animator;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;
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

    protected int horzSize = CommonStatic.LaunchItemSize;
    protected int vertSize = CommonStatic.LaunchItemSize;

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

    protected String launchTitle;
    protected ArrayList<FrameLayout> launchPages;
    protected ArrayList<LaunchItem> launchItems;

    protected ArrayList<LaunchItem> recyledNextItems = new ArrayList<>();
    protected ArrayList<LaunchItem> recyledPrevItems = new ArrayList<>();

    protected int launchPage;

    public LaunchGroup(Context context)
    {
        super(context);

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
            lpage.setVisibility(VISIBLE);

            if (clickmove)
            {
                LayoutParams lparams = (LayoutParams) lpage.getLayoutParams();
                lparams.width = getWidth();
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
            lpage.setVisibility(VISIBLE);

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
            final FrameLayout cblpage = lpage;

            LayoutParams from = (LayoutParams) lpage.getLayoutParams();
            LayoutParams toto = new LayoutParams(lpage.getLayoutParams());
            toto.leftMargin = finalMargin;

            Animator animator = new Animator();

            animator.setDuration(500);
            animator.setLayout(lpage, from, toto);

            animator.setFinalCall(new Runnable()
            {
                @Override
                public void run()
                {
                    //
                    // Set page back to match parent.
                    //

                    LayoutParams lparams = (LayoutParams) cblpage.getLayoutParams();
                    lparams.width = Simple.MP;

                    adjustArrows();
                }
            });

            this.startAnimation(animator);
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        adjustArrows();
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
                lparams.width = getWidth();
                lparams.leftMargin = xscreen - getWidth();
                lpage.setVisibility(VISIBLE);
                lpage.bringToFront();
            }

            if ((view == arrowRight) && ((launchPage + 1) < launchPages.size()))
            {
                FrameLayout lpage = launchPages.get(launchPage + 1);

                LayoutParams lparams = (LayoutParams) lpage.getLayoutParams();
                lparams.leftMargin = xscreen;
                lpage.setVisibility(VISIBLE);
                lpage.bringToFront();
            }
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            animatePage(view, false);
        }

        return true;
    }

    public void adjustArrows()
    {
        if (launchPages == null) return;

        arrowLeft.setVisibility(((launchPage - 1) >= 0) ? VISIBLE : GONE);
        arrowRight.setVisibility(((launchPage + 1) < launchPages.size()) ? VISIBLE : GONE);

        arrowLeft.bringToFront();
        arrowRight.bringToFront();

        //
        // Adjust pages bullets.
        //

        Log.d(LOGTAG, "adjustArrows:" + launchTitle);

        ViewParent parentview = getParent();

        while (parentview != null)
        {
            if (parentview instanceof HomeFrame)
            {
                String label = (parent != null) ? (String) parent.label.getText() : launchTitle;
                ((HomeFrame) parentview).setActivePage(launchPage, launchPages.size(), label);

                break;
            }

            parentview = parentview.getParent();
        }
    }

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

    private FrameLayout getRecycledLaunchPage()
    {
        if (launchPages.size() > 0)
        {
            FrameLayout lp = launchPages.remove(0);
            lp.setVisibility(GONE);

            int childs = lp.getChildCount();

            for (int inx = 0; inx < childs; inx++)
            {
                if (lp.getChildAt(inx) instanceof LaunchItemNextPrev)
                {
                    LaunchItemNextPrev li = (LaunchItemNextPrev) lp.getChildAt(inx--);
                    lp.removeView(li);

                    if (Simple.equals(li.getType(), "next"))
                    {
                        recyledNextItems.add(li);
                    }
                    else
                    {
                        recyledPrevItems.add(li);
                    }
                }
            }

            return lp;
        }

        FrameLayout lp = new FrameLayout(context);
        lp.setBackgroundColor(Color.WHITE);
        lp.setVisibility(GONE);
        this.addView(lp);

        return lp;
    }

    private LaunchItem getRecycledNextPrevItem(String type)
    {
        JSONObject config = new JSONObject();

        if (type.equals("next"))
        {
            if (recyledNextItems.size() > 0) return recyledNextItems.remove(0);

            Json.put(config, "type", "next");
            Json.put(config, "label", "weiter");
        }

        if (type.equals("prev"))
        {
            if (recyledPrevItems.size() > 0) return recyledPrevItems.remove(0);

            Json.put(config, "type", "prev");
            Json.put(config, "label", "zurück");
        }

        return LaunchItem.createLaunchItem(context, this, config);
    }

    protected void positionLaunchItems()
    {
        ArrayList<FrameLayout> newPages = new ArrayList<>();

        int maxSlots = horzItems * vertItems;
        int allSlots = launchItems.size();

        int nextSlot = 0;

        FrameLayout lp = getRecycledLaunchPage();
        newPages.add(lp);

        for (int inx = 0; inx < allSlots; inx++)
        {
            LaunchItem li = launchItems.get(inx);

            /*
            if ((nextSlot == 0) && (parent != null) && (maxSlots > 4))
            {
                LaunchItem liprev = getRecycledNextPrevItem("prev");
                liprev.setSize(horzSize, vertSize);
                liprev.setPosition(getXpos(nextSlot), getYpos(nextSlot));

                lp.addView(liprev);

                nextSlot++;
            }
            */

            if (li.getParent() != lp)
            {
                Simple.removeFromParent(li);
                lp.addView(li);
            }

            li.setPosition(getXpos(nextSlot), getYpos(nextSlot));

            nextSlot++;

            if ((nextSlot >= maxSlots) && (maxSlots > 4) && ((inx + 1) < allSlots))
            {
                Log.d(LOGTAG, "=================>positionLaunchItems:"
                    + " inx=" + inx
                    + " maxSlots=" + maxSlots
                    + " allSlots=" + allSlots
                    + " nextSlot=" + nextSlot
                );

                //
                // Next icon on last position of page.
                //

                LaunchItem linext = getRecycledNextPrevItem("next");

                linext.setSize(horzSize, vertSize);
                linext.setPosition(getXpos(nextSlot), getYpos(nextSlot));

                lp.addView(linext);

                //
                // Create new page.
                //

                nextSlot = 0;

                lp = getRecycledLaunchPage();
                newPages.add(lp);

                //
                // Prev icon on first position of page.
                //

                JSONObject prev = new JSONObject();
                Json.put(prev, "type", "prev");
                Json.put(prev, "label", "zurück");

                LaunchItem liprev = LaunchItem.createLaunchItem(context, this, prev);

                liprev.setSize(horzSize, vertSize);
                liprev.setPosition(getXpos(nextSlot), getYpos(nextSlot));
                lp.addView(liprev);

                nextSlot++;

                if ((maxSlots <= 4) && (nextSlot == maxSlots))
                {
                    //
                    // Small preview page. Break after 4 items.
                    //

                    break;
                }
            }
        }

        while (launchPages.size() > 0)
        {
            lp = launchPages.remove(0);
            Simple.removeFromParent(lp);
        }

        launchPages = newPages;

        launchPage = 0;
        lp = launchPages.get(launchPage);

        lp.setVisibility(VISIBLE);
        lp.bringToFront();

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

    public void setTitle(String title)
    {
        launchTitle = title;

        Log.d(LOGTAG, "setTitle:" + title);
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
                    JSONObject liconfig = Json.getObject(launchitems, inx);

                    JSONObject intent = Json.getObject(liconfig, "intent");
                    voiceintent.collectIntent(liconfig, intent);

                    JSONArray intents = Json.getArray(liconfig, "intents");
                    voiceintent.collectIntents(liconfig, intents);
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
