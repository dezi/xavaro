package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentResolver;

@SuppressLint("RtlHardcoded")
public class HomePeople extends FrameLayout implements VoiceIntentResolver
{
    private static final String LOGTAG = HomePeople.class.getSimpleName();

    private final ArrayList<LaunchItem> launchItems = new ArrayList<>();

    private JSONArray baseContacts;
    private JSONArray moreContacts;
    private JSONArray socialLikes;
    private JSONArray appFolders;

    private LayoutParams layoutParams;

    private LaunchItem alertLaunchItem;
    private LaunchItem voiceLaunchItem;
    private LinearLayout.LayoutParams peopleLayout;
    private LinearLayout peopleView;

    private ScrollView vertFrame;
    private HorizontalScrollView horzFrame;
    private LayoutParams vertLayout;
    private LayoutParams horzLayout;

    private int navbarSize;
    private int orientation;

    public HomePeople(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);

        horzLayout = new LayoutParams(Simple.MP, Simple.MP);

        horzFrame = new HorizontalScrollView(context)
        {
            @Override
            public boolean onTouchEvent(MotionEvent motionEvent)
            {
                onPeopleTouchEvent(motionEvent);
                return super.onTouchEvent(motionEvent);
            }
        };

        horzFrame.setLayoutParams(horzLayout);
        addView(horzFrame);

        vertLayout = new LayoutParams(Simple.MP, Simple.MP);

        vertFrame = new ScrollView(context)
        {
            @Override
            public boolean onTouchEvent(MotionEvent motionEvent)
            {
                onPeopleTouchEvent(motionEvent);
                return super.onTouchEvent(motionEvent);
            }
        };

        vertFrame.setLayoutParams(vertLayout);
        addView(vertFrame);

        peopleLayout = new LinearLayout.LayoutParams(Simple.WC, Simple.WC);
        peopleView = new LinearLayout(context);
        peopleView.setLayoutParams(peopleLayout);

        navbarSize = Simple.getNavigationBarHeight();
        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    private int xLastTouch;
    private int yLastTouch;
    private int xDirTouch;
    private int yDirTouch;

    private void onPeopleTouchEvent(MotionEvent motionEvent)
    {
        int xscreen = (int) motionEvent.getRawX();
        int yscreen = (int) motionEvent.getRawY();

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            xDirTouch = 0;
            yDirTouch = 0;

            xLastTouch = xscreen;
            yLastTouch = yscreen;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
        {
            xDirTouch = (xLastTouch > xscreen) ? -1 : 1;
            yDirTouch = (yLastTouch > yscreen) ? -1 : 1;

            xLastTouch = xscreen;
            yLastTouch = yscreen;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
        {
            Simple.makePost(onPeopleScrollEnded);
        }
    }

    private final Runnable onPeopleScrollEnded = new Runnable()
    {
        @Override
        public void run()
        {
            if (Simple.isPortrait())
            {
                int xoffset = horzFrame.getScrollX();
                int leftrest = xoffset % HomeActivity.personSize;
                int rightrest = HomeActivity.personSize - leftrest;

                horzFrame.smoothScrollBy((xDirTouch > 0) ? -leftrest : rightrest, 0);
            }
            else
            {
                int yoffset = vertFrame.getScrollX();
                int toprest = yoffset % HomeActivity.personSize;
                int bottomrest = HomeActivity.personSize - toprest;

                vertFrame.smoothScrollBy((yDirTouch > 0) ? -toprest : bottomrest, 0);
            }
        }
    };

    public void setConfig(JSONObject config)
    {
        launchItems.clear();

        JSONObject alertConfig = Json.getObject(LaunchItemAlertcall.getConfig(), 0);

        if (alertConfig != null)
        {
            if (alertLaunchItem == null)
            {
                alertLaunchItem = LaunchItem.createLaunchItem(getContext(), null, alertConfig);
                alertLaunchItem.setSize(HomeActivity.personSize, HomeActivity.personSize);
                alertLaunchItem.setFrameLess(true);

                addView(alertLaunchItem);
            }

            launchItems.add(alertLaunchItem);
        }
        else
        {
            if (alertLaunchItem != null)
            {
                Simple.removeFromParent(alertLaunchItem);
                alertLaunchItem = null;
            }
        }

        JSONObject voiceConfig = Json.getObject(LaunchItemVoice.getConfig(), 0);

        if (voiceConfig != null)
        {
            if (voiceLaunchItem == null)
            {
                voiceLaunchItem = LaunchItem.createLaunchItem(getContext(), null, voiceConfig);
                voiceLaunchItem.setSize(HomeActivity.personSize, HomeActivity.personSize);
                voiceLaunchItem.setFrameLess(true);
                addView(voiceLaunchItem);
            }

            launchItems.add(voiceLaunchItem);
        }
        else
        {
            if (voiceLaunchItem != null)
            {
                Simple.removeFromParent(voiceLaunchItem);
                voiceLaunchItem = null;
            }
        }

        baseContacts = new JSONArray();
        moreContacts = new JSONArray();
        socialLikes = new JSONArray();
        appFolders = new JSONArray();

        peopleView.removeAllViews();

        extractConfig(config);

        for (int inx = 0; inx < baseContacts.length(); inx++)
        {
            JSONObject contact = Json.getObject(baseContacts, inx);
            if (contact == null) continue;

            LaunchItem launchItem = LaunchItem.createLaunchItem(getContext(), null, contact);

            launchItem.setFrameLess(true);
            launchItem.setSize(HomeActivity.personSize, HomeActivity.personSize);

            launchItems.add(launchItem);
            peopleView.addView(launchItem);
        }
    }

    private boolean isCommUser(JSONObject li)
    {
        String subtype = Json.getString(li, "subtype");

        return Simple.equals(subtype, "chat")
            || Simple.equals(subtype, "text")
            || Simple.equals(subtype, "voip")
            || Simple.equals(subtype, "vica");
    }

    private boolean isSocialUser(JSONObject li)
    {
        String pfid = Json.getString(li, "pfid");
        if (pfid == null) return false;

        String type = Json.getString(li, "type");

        return Simple.equals(type, "twitter")
            || Simple.equals(type, "facebook")
            || Simple.equals(type, "instagram")
            || Simple.equals(type, "googleplus");
    }

    private boolean isSocialFriend(JSONObject li)
    {
        String pfid = Json.getString(li, "pfid");
        if (pfid == null) return false;

        String type = Json.getString(li, "type");
        String subtype = Json.getString(li, "subtype");

        return (Simple.equals(subtype, "friend") || Simple.equals(subtype, "owner"))
                && (Simple.equals(type, "twitter")
                || Simple.equals(type, "facebook")
                || Simple.equals(type, "instagram")
                || Simple.equals(type, "googleplus"));
    }

    private boolean isSocialLike(JSONObject li)
    {
        String pfid = Json.getString(li, "pfid");
        if (pfid == null) return false;

        String type = Json.getString(li, "type");
        String subtype = Json.getString(li, "subtype");

        return Simple.equals(subtype, "like")
                && (Simple.equals(type, "twitter")
                || Simple.equals(type, "facebook")
                || Simple.equals(type, "instagram")
                || Simple.equals(type, "googleplus"));
    }

    private void addChoices(JSONArray choices, JSONObject li)
    {
        if ((choices == null) || (li == null)) return;

        //
        // Clone launch item config and mark as noprofile
        // display, means the function of the launch item
        // becomes the label and large icon.
        //

        JSONObject liclone = Json.clone(li);
        Json.put(liclone, "noprofile", true);

        //
        // Add to user chcoices.
        //

        Json.put(choices, liclone);

        //
        // Mark entry as used.
        //

        Json.put(li, "used", true);
    }

    private void extractConfig(JSONObject config)
    {
        JSONArray lis = Json.getArray(config, "launchitems");
        if (lis == null) return;

        //
        // First scan. Get every contact located on the home screen.
        // Remove all consumed contacts from original config.
        //

        JSONObject nameList = new JSONObject();

        for (int inx = 0; inx < lis.length(); inx++)
        {
            JSONObject li = Json.getObject(lis, inx);
            if (Json.getBoolean(li, "used")) continue;

            String label = Json.getString(li, "label");
            if (label == null) continue;
            label = label.toLowerCase();

            if (isCommUser(li) || isSocialUser(li))
            {
                if (! Json.has(nameList, label))
                {
                    //
                    // Initial occurrence.
                    //

                    Json.put(nameList, label, new JSONArray());
                    Json.put(baseContacts, li);
                }

                //
                // Add every item also to communication choices.
                //

                addChoices(Json.getArray(nameList, label), li);

                //
                // Remove from original launch pages.
                //

                lis.remove(inx--);
            }
            else
            {
                String type = Json.getString(li, "type");
                String subtype = Json.getString(li, "subtype");

                if (Simple.equals(type, "xavaro") && Simple.equals(subtype, "padm"))
                {
                    continue;
                }

                if (Simple.equals(type, "contacts")
                        || Simple.equals(type, "phone")
                        || Simple.equals(type, "skype")
                        || Simple.equals(type, "xavaro")
                        || Simple.equals(type, "whatsapp")
                        || Simple.equals(type, "twitter")
                        || Simple.equals(type, "facebook")
                        || Simple.equals(type, "instagram")
                        || Simple.equals(type, "googleplus"))
                {
                    Json.put(appFolders, li);
                    lis.remove(inx--);
                }
            }
        }

        //
        // Now loop over all app folders inclusive contacts
        // and distribute entrys to people.
        //

        for (int finx = 0; finx < appFolders.length(); finx++)
        {
            JSONObject appfolder = Json.getObject(appFolders, finx);
            JSONArray applis = Json.getArray(appfolder, "launchitems");
            if (applis == null) continue;

            for (int inx = 0; inx < applis.length(); inx++)
            {
                JSONObject li = Json.getObject(applis, inx);
                if (Json.getBoolean(li, "used")) continue;

                String label = Json.getString(li, "label");
                if (label == null) continue;
                label = label.toLowerCase();

                if (isCommUser(li) || isSocialFriend(li))
                {
                    if (! Json.has(nameList, label))
                    {
                        //
                        // Initial occurrence.
                        //

                        Json.put(nameList, label, new JSONArray());
                        Json.put(moreContacts, li);
                    }

                    //
                    // Add every item also to communication choices.
                    //

                    addChoices(Json.getArray(nameList, label), li);

                    continue;
                }

                if (isSocialLike(li))
                {
                    if (! Json.has(nameList, label))
                    {
                        //
                        // Initial occurrence.
                        //

                        Json.put(nameList, label, new JSONArray());
                        Json.put(socialLikes, li);
                    }

                    //
                    // Add every item also to communication choices.
                    //

                    addChoices(Json.getArray(nameList, label), li);
                }
            }
        }

        if (baseContacts.length() > 0)
        {
            for (int inx = 0; inx < baseContacts.length(); inx++)
            {
                JSONObject li = Json.getObject(baseContacts, inx);
                String label = Json.getString(li, "label");
                if (label == null) continue;
                label = label.toLowerCase();

                JSONArray choices = Json.getArray(nameList, label);
                Json.put(li, "launchitems", choices);

                //
                // Add copy to all contacts at end of other contacts.
                //

                Json.put(moreContacts, Json.clone(li));
            }
        }

        if (moreContacts.length() > 0)
        {
            for (int inx = 0; inx < moreContacts.length(); inx++)
            {
                JSONObject li = Json.getObject(moreContacts, inx);
                String label = Json.getString(li, "label");
                if (label == null) continue;
                label = label.toLowerCase();

                JSONArray choices = Json.getArray(nameList, label);
                Json.put(li, "launchitems", choices);
                Json.put(li, "nofunction", true);
            }

            JSONObject contacts = new JSONObject();

            Json.put(contacts, "type", "contacts");
            Json.put(contacts, "label", "weitere");
            Json.put(contacts, "launchitems", moreContacts);

            Json.put(baseContacts, contacts);
        }

        if (socialLikes.length() > 0)
        {
            for (int inx = 0; inx < socialLikes.length(); inx++)
            {
                JSONObject li = Json.getObject(socialLikes, inx);
                String label = Json.getString(li, "label");
                if (label == null) continue;
                label = label.toLowerCase();

                JSONArray choices = Json.getArray(nameList, label);
                Json.put(li, "launchitems", choices);
                Json.put(li, "nofunction", true);
            }

            JSONObject contacts = new JSONObject();

            Json.put(contacts, "type", "likes");
            Json.put(contacts, "label", "Abonnements");
            Json.put(contacts, "launchitems", socialLikes);

            Json.put(baseContacts, contacts);
        }
    }

    private Runnable changeOrientation = new Runnable()
    {
        @Override
        public void run()
        {
            if ((orientation != Configuration.ORIENTATION_PORTRAIT) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_PORTRAIT))
            {
                int items = 3;
                int width = ((View) getParent()).getWidth();
                int margin = (width - (items * HomeActivity.personSize)) / 2;

                setPadding(0, (HomeActivity.peopleSize - HomeActivity.personSize - navbarSize) / 2, 0, 0);

                layoutParams.width = Simple.MP;
                layoutParams.height = HomeActivity.peopleSize - navbarSize;
                layoutParams.gravity = Gravity.BOTTOM;
                layoutParams.bottomMargin = navbarSize;
                layoutParams.rightMargin = 0;

                if (alertLaunchItem != null)
                {
                    alertLaunchItem.setGravity(Gravity.LEFT);
                    horzLayout.leftMargin = margin;
                }

                if (voiceLaunchItem != null)
                {
                    voiceLaunchItem.setGravity(Gravity.RIGHT);
                    horzLayout.rightMargin = margin;
                }

                vertFrame.removeAllViews();
                horzFrame.addView(peopleView);

                peopleLayout.width = Simple.WC;
                peopleLayout.height = Simple.MP;
                peopleView.setOrientation(LinearLayout.HORIZONTAL);

                orientation = Configuration.ORIENTATION_PORTRAIT;
            }

            if ((orientation != Configuration.ORIENTATION_LANDSCAPE) &&
                    (Simple.getOrientation() == Configuration.ORIENTATION_LANDSCAPE))
            {
                int items = 3;
                int height = ((View) getParent()).getHeight();
                int margin = (height - (items * HomeActivity.personSize)) / 2;

                setPadding((HomeActivity.peopleSize - HomeActivity.personSize - navbarSize) / 2, 0, 0, 0);

                layoutParams.width = HomeActivity.peopleSize - navbarSize;
                layoutParams.height = Simple.MP;
                layoutParams.gravity = Gravity.RIGHT;
                layoutParams.rightMargin = navbarSize;
                layoutParams.bottomMargin = 0;

                if (alertLaunchItem != null)
                {
                    alertLaunchItem.setGravity(Gravity.TOP);
                    vertLayout.topMargin = margin;
                }

                if (voiceLaunchItem != null)
                {
                    voiceLaunchItem.setGravity(Gravity.BOTTOM);
                    vertLayout.bottomMargin = margin;
                }

                horzFrame.removeAllViews();
                vertFrame.addView(peopleView);

                peopleLayout.width = Simple.MP;
                peopleLayout.height = Simple.WC;
                peopleView.setOrientation(LinearLayout.VERTICAL);

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

    @Override
    public void onCollectVoiceIntent(VoiceIntent voiceintent)
    {
        //
        // Recursivly collect from configured launch items.
        //

        for (LaunchItem launchItem : launchItems)
        {
            launchItem.onCollectVoiceIntent(voiceintent);
        }

        //
        // Collect from disconfigured app folder launch items.
        //

        for (int inx = 0; inx < appFolders.length(); inx++)
        {
            JSONObject liconfig = Json.getObject(appFolders, inx);

            JSONObject intent = Json.getObject(liconfig, "intent");
            voiceintent.collectIntent(liconfig, intent);

            JSONArray intents = Json.getArray(liconfig, "intents");
            voiceintent.collectIntents(liconfig, intents);
        }
    }

    @Override
    public void onResolveVoiceIntent(VoiceIntent voiceintent)
    {
        //
        // Recursivly resolve from configured launch items.
        //

        for (LaunchItem launchItem : launchItems)
        {
            launchItem.onResolveVoiceIntent(voiceintent);
        }

        //
        // Reolve from disconfigured app folder launch items.
        //

        for (int inx = 0; inx < appFolders.length(); inx++)
        {
            JSONObject liconfig = Json.getObject(appFolders, inx);
            if (liconfig == null) continue;

            JSONObject intent = Json.getObject(liconfig, "intent");
            voiceintent.evaluateIntent(liconfig, intent);

            JSONArray intents = Json.getArray(liconfig, "intents");
            voiceintent.evaluateIntents(liconfig, intents);
        }
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        //
        // Recursivly try to execute from configured launch items.
        //

        for (LaunchItem launchItem : launchItems)
        {
            if (launchItem.onExecuteVoiceIntent(voiceintent, index))
            {
                return true;
            }
        }

        return false;
    }
}
