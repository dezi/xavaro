package de.xavaro.android.safehome;

import android.annotation.SuppressLint;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

@SuppressLint("RtlHardcoded")
public class HomeBottom extends FrameLayout
{
    private static final String LOGTAG = HomeBottom.class.getSimpleName();

    private int layoutSize;

    private JSONArray baseContacts;
    private JSONArray moreContacts;
    private JSONArray socialLikes;

    private LayoutParams layoutParams;

    private LaunchItem alertLaunchItem;
    private LaunchItem voiceLaunchItem;
    private LinearLayout.LayoutParams peopleLayout;
    private LinearLayout peopleView;

    private ScrollView vertFrame;
    private HorizontalScrollView horzFrame;
    private LayoutParams vertLayout;
    private LayoutParams horzLayout;
    private int orientation;

    public HomeBottom(Context context)
    {
        super(context);

        layoutParams = new LayoutParams(0, 0);
        setLayoutParams(layoutParams);

        JSONObject alertConfig = Json.getObject(LaunchItemAlertcall.getConfig(), 0);

        if (alertConfig != null)
        {
            alertLaunchItem = LaunchItem.createLaunchItem(context, null, alertConfig);
            alertLaunchItem.setFrameLess();
            this.addView(alertLaunchItem);
        }

        JSONObject voiceConfig = Json.getObject(LaunchItemVoice.getConfig(), 0);

        if (voiceConfig != null)
        {
            voiceLaunchItem = LaunchItem.createLaunchItem(context, null, voiceConfig);
            voiceLaunchItem.setFrameLess();
            this.addView(voiceLaunchItem);
        }

        horzLayout = new LayoutParams(Simple.MP, Simple.MP);
        horzFrame = new HorizontalScrollView(context);
        horzFrame.setLayoutParams(horzLayout);
        addView(horzFrame);

        vertLayout = new LayoutParams(Simple.MP, Simple.MP);
        vertFrame = new ScrollView(context);
        vertFrame.setLayoutParams(vertLayout);
        addView(vertFrame);

        peopleLayout = new LinearLayout.LayoutParams(Simple.WC, Simple.WC);
        peopleView = new LinearLayout(context);
        peopleView.setLayoutParams(peopleLayout);

        orientation = Configuration.ORIENTATION_UNDEFINED;
    }

    public void setSize(int pixels)
    {
        layoutSize = pixels;

        if (alertLaunchItem != null) alertLaunchItem.setSize(layoutSize, layoutSize);
        if (voiceLaunchItem != null) voiceLaunchItem.setSize(layoutSize, layoutSize);
    }

    public void setConfig(JSONObject config)
    {
        baseContacts = new JSONArray();
        moreContacts = new JSONArray();
        socialLikes = new JSONArray();

        peopleView.removeAllViews();

        extractConfig(config);

        for (int inx = 0; inx < baseContacts.length(); inx++)
        {
            JSONObject contact = Json.getObject(baseContacts, inx);
            if (contact == null) continue;

            final LaunchItem launchItem = LaunchItem.createLaunchItem(getContext(), null, contact);

            launchItem.setFrameLess();
            launchItem.setSize(layoutSize, layoutSize);

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

        return Simple.equals(subtype, "friend")
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

    private void extractConfig(JSONObject config)
    {
        JSONArray lis = Json.getArray(config, "launchitems");
        if (lis == null) return;

        //
        // First scan. Get every contact located on the home screen.
        // Remove all consumed contacts from original config.
        //

        JSONObject nameList = new JSONObject();
        JSONArray appfolders = new JSONArray();

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

                JSONArray choices = Json.getArray(nameList, label);
                Json.put(choices, Json.clone(li));
                Json.put(li, "used", true);

                //
                // Remove from original launch pages.
                //

                lis.remove(inx--);
            }
            else
            {
                String type = Json.getString(li, "type");

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
                    Json.put(appfolders, li);
                    lis.remove(inx--);
                }
            }
        }

        //
        // Now loop over all app folders inclusive contacts
        // and distribute entrys to people.
        //

        for (int finx = 0; finx < appfolders.length(); finx++)
        {
            JSONObject appfolder = Json.getObject(appfolders, finx);
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

                    JSONArray choices = Json.getArray(nameList, label);
                    Json.put(choices, Json.clone(li));
                    Json.put(li, "used", true);

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

                    JSONArray choices = Json.getArray(nameList, label);
                    Json.put(choices, Json.clone(li));
                    Json.put(li, "used", true);
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
            }

            JSONObject contacts = new JSONObject();

            Json.put(contacts, "type", "contacts");
            Json.put(contacts, "label", "weitere");
            Json.put(contacts, "launchitems", moreContacts);

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
                int width = ((View) getParent()).getWidth();
                int items = ((int) Math.floor(width / layoutSize)) - 1;
                int margin = (width - (items * layoutSize)) / 2;

                layoutParams.width = Simple.MP;
                layoutParams.height = layoutSize;
                layoutParams.gravity = Gravity.BOTTOM;

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
                int height = ((View) getParent()).getHeight();
                int items = ((int) Math.floor(height / layoutSize)) - 1;
                int margin = (height - (items * layoutSize)) / 2;

                layoutParams.width = layoutSize;
                layoutParams.height = Simple.MP;
                layoutParams.gravity = Gravity.RIGHT;

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
}
