package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.graphics.Color;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.util.AttributeSet;

import android.util.Log;
import android.view.View;
import android.view.Gravity;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import de.xavaro.android.common.CacheManager;
import de.xavaro.android.common.Chooser;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VersionUtils;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.VoiceIntentResolver;

//
// Launch item view on home screen.
//

public class LaunchItem extends FrameLayout implements
        Chooser.ChooserResultCallback,
        VoiceIntentResolver
{
    private final static String LOGTAG = LaunchItem.class.getSimpleName();

    public static LaunchItem createLaunchItem(Context context, LaunchGroup parent, JSONObject config)
    {
        LaunchItem item = null;

        String type = Json.getString(config, "type");
        String subtype = Json.getString(config, "subtype");
        String suty = type + ((subtype == null) ? "" : "/" + subtype);

        // @formatter:off
        if (Simple.equals(type, "prev"        )) item = new LaunchItemNextPrev(context);
        if (Simple.equals(type, "next"        )) item = new LaunchItemNextPrev(context);

        if (Simple.equals(type, "beta"        )) item = new LaunchItemBeta(context);
        if (Simple.equals(type, "today"       )) item = new LaunchItemToday(context);
        if (Simple.equals(type, "voice"       )) item = new LaunchItemVoice(context);
        if (Simple.equals(type, "health"      )) item = new LaunchItemHealth(context);
        if (Simple.equals(type, "alertcall"   )) item = new LaunchItemAlertcall(context);

        if (Simple.equals(type, "apps"        )) item = new LaunchItemApps(context);
        if (Simple.equals(suty, "media/image" )) item = new LaunchItemMediaImage(context);
        if (Simple.equals(suty, "media/video" )) item = new LaunchItemMediaVideo(context);

        if (Simple.equals(type, "phone"       )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "skype"       )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "xavaro"      )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "whatsapp"    )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "contacts"    )) item = new LaunchItemComm(context);

        if (Simple.equals(type, "webapp"      )) item = new LaunchItemWebApp(context);
        if (Simple.equals(type, "webframe"    )) item = new LaunchItemWebFrame(context);
        if (Simple.equals(type, "webconfig"   )) item = new LaunchItemWebFrame(context);

        if (Simple.equals(type, "ioc"         )) item = new LaunchItemWebFrame(context);
        if (Simple.equals(type, "iptv"        )) item = new LaunchItemWebStream(context);
        if (Simple.equals(type, "iprd"        )) item = new LaunchItemWebStream(context);
        if (Simple.equals(type, "audioplayer" )) item = new LaunchItemWebStream(context);
        if (Simple.equals(type, "videoplayer" )) item = new LaunchItemWebStream(context);

        if (Simple.equals(type, "select"      )) item = new LaunchItemAdmin(context);
        if (Simple.equals(type, "install"     )) item = new LaunchItemAdmin(context);
        if (Simple.equals(type, "settings"    )) item = new LaunchItemAdmin(context);

        if (Simple.equals(type, "directory"   )) item = new LaunchItemGeneric(context);
        if (Simple.equals(type, "genericapp"  )) item = new LaunchItemGeneric(context);

        if (Simple.equals(type, "developer"   )) item = new LaunchItemDeveloper(context);
        // @formatter:on

        if (item == null) item = new LaunchItem(context);

        item.setConfig(parent, config);

        return item;
    }

    protected Context context;
    protected Handler handler;
    protected String identifier;

    protected LayoutParams layout;
    protected LayoutParams oversize;
    protected int textsize;
    protected int padding;

    protected LaunchGroup parent;
    protected JSONObject config;
    protected String type;
    protected String subtype;

    protected ImageView icon;
    protected TextView label;
    protected String labelText;

    protected FrameLayout overlay;
    protected ImageView overicon;
    protected TextView overtext;
    protected FrameLayout dimmer;

    protected LaunchGroup directory;

    public LaunchItem(Context context)
    {
        super(context);

        myInit(context);
    }

    public LaunchItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        myInit(context);
    }

    public LaunchItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        myInit(context);
    }

    private void myInit(Context context)
    {
        this.context = context;

        setBackgroundResource(R.drawable.shadow_black_400x400);

        layout = new LayoutParams(0, 0);
        setLayoutParams(layout);

        icon = new ImageView(context);
        icon.setPadding(0, 0, 0, 40);
        addView(icon);

        label = new TextView(context);
        label.setLayoutParams(Simple.layoutParamsWW(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
        label.setGravity(Gravity.CENTER_HORIZONTAL);
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        addView(label);

        oversize = new LayoutParams(0, 0);
        oversize.gravity = Gravity.END + Gravity.TOP;

        overlay = new FrameLayout(context);
        overlay.setLayoutParams(oversize);
        overlay.setVisibility(INVISIBLE);
        addView(overlay);

        overicon = new DitUndDat.ImageAntiAliasView(context);
        overlay.addView(overicon);

        overtext = new TextView(context);
        overtext.setLayoutParams(Simple.layoutParamsMM());
        overtext.setGravity(Gravity.CENTER);
        overtext.setTextSize(Simple.getDeviceTextSize(24f));
        overtext.setPadding(2, 0, 0, 4);
        overtext.setTextColor(Color.WHITE);
        overtext.setTypeface(null, Typeface.BOLD);
        overlay.addView(overtext);

        dimmer = new FrameLayout(context);
        dimmer.setBackgroundColor(Color.TRANSPARENT);
        addView(dimmer);

        setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onMyClick();
            }
        });

        setLongClickable(true);
        setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                return onMyLongClick();
            }
        });

        overlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onMyOverlayClick();
            }
        });
    }

    public void setSize(int width, int height)
    {
        layout.width = width;
        layout.height = height;

        oversize.width = layout.width / 4;
        oversize.height = layout.height / 4;

        //
        // Nine patch background does not scale implicit
        // padding according to background itself. Bug
        // in Android. Set padding to some reasonable
        // value if dimensions are known.
        //

        padding = layout.width / 12;
        setPadding(padding, padding, padding, padding);

        //
        // Now fill in label text if present.
        //

        if (config.has("label")) setLabelText(Json.getString(config, "label"));
    }

    public void setPosition(int left, int top)
    {
        layout.leftMargin = left;
        layout.topMargin = top;
    }

    public void setLabelText(String text)
    {
        if (labelText != null) text = labelText;
        if (text == null) return;

        label.setText(text);

        //
        // Set reasonable size of text according to height.
        //

        textsize = layout.height / 10;
        label.setTextSize(Simple.getDeviceTextSize(textsize));

        //
        // Figure out the layout of this label.
        //

        label.setMaxWidth(Integer.MAX_VALUE);
        label.measure(0, 0);
        int onelineHeight = label.getMeasuredHeight();

        int laywid = layout.width - padding * 2;

        label.setMaxWidth(laywid);
        label.measure(0, 0);
        int layoutHeight = label.getMeasuredHeight();

        if (layoutHeight > onelineHeight)
        {
            //
            // Text will use at least two lines in
            // layout. Make it a little bit smaller.
            //

            textsize = layout.height / 12;
            label.setTextSize(Simple.getDeviceTextSize(textsize));

            //
            // This might lead to a shitty breaking
            // of words. Reduce the width furthermore
            // as long as the height stays the same.
            // Means, that all words are evenly on
            // their corresponding lines.
            //

            int targetHeight = layoutHeight;

            while (laywid > 0)
            {
                label.setMaxWidth(--laywid);
                label.measure(0, 0);
                layoutHeight = label.getMeasuredHeight();

                if (layoutHeight > targetHeight)
                {
                    //
                    // We just got another line. Release
                    // maximum width to last good value.
                    //

                    label.setMaxWidth(++laywid);
                    break;
                }
            }
        }
    }

    public void setConfig(LaunchGroup parent, JSONObject config)
    {
        this.config = config;
        this.parent = parent;

        type = config.has("type") ? Json.getString(config, "type") : null;
        subtype = config.has("subtype") ? Json.getString(config, "subtype") : null;
        identifier = config.has("identifier") ? Json.getString(config, "identifier") : null;

        if (config.has("iconres"))
        {
            int resourceId = Json.getInt(config, "iconres");
            if (resourceId > 0) icon.setImageResource(resourceId);
        }

        if (config.has("icon"))
        {
            String iconref = Json.getString(config, "icon");

            if (Simple.startsWith(iconref, "http://") || Simple.startsWith(iconref, "https://"))
            {
                if (config.has("name"))
                {
                    String iconname = Json.getString(config, "name");
                    Drawable drawable = CacheManager.getWebIcon(iconname, iconref);
                    icon.setImageDrawable(drawable);
                }
            }
            else
            {
                int resourceId = Simple.getIconResourceId(iconref);
                if (resourceId > 0) icon.setImageResource(resourceId);
            }
        }

        if (config.has("packagename"))
        {
            String packageName = Json.getString(config, "packagename");

            if (packageName != null)
            {
                CommonConfigs.weLikeThis(packageName);
                Drawable appIcon = VersionUtils.getIconFromApplication(packageName);

                if (appIcon != null)
                {
                    icon.setImageDrawable(appIcon);
                }
                else
                {
                    Drawable drawable = CacheManager.getAppIcon(packageName);

                    if (drawable != null)
                    {
                        icon.setImageDrawable(drawable);
                    }
                    else
                    {
                        icon.setImageResource(R.drawable.stop_512x512);
                    }
                }
            }
        }

        setConfig();
    }

    @Nullable
    public LaunchGroup getLaunchGroup()
    {
        return parent;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    protected void setConfig()
    {
        //
        // To be overridden...
        //
    }

    protected void onMyClick()
    {
        //
        // To be overridden...
        //
    }

    protected boolean onMyLongClick()
    {
        //
        // To be overridden...
        //

        return false;
    }

    protected void onMyOverlayClick()
    {
        //
        // To be overridden...
        //
    }

    public void onChooserResult(String key)
    {
        //
        // To be overridden...
        //
    }

    public void onBackKeyExecuted()
    {
        //
        // To be overridden...
        //
    }

    @Nullable
    private JSONArray prepareIntents(JSONObject myconfig, JSONArray intents)
    {
        if (intents != null)
        {
            for (int inx = 0; inx < intents.length(); inx++)
            {
                prepareIntent(myconfig, Json.getObject(intents, inx));
            }
        }

        return intents;
    }

    private JSONObject prepareIntent(JSONObject myconfig, JSONObject intent)
    {
        if (intent != null)
        {
            if (myconfig.has("type"))
            {
                Json.put(intent, "type", Json.getString(myconfig, "type"));
            }

            if (myconfig.has("subtype"))
            {
                Json.put(intent, "subtype", Json.getString(myconfig, "subtype"));
            }

            if (myconfig.has("name"))
            {
                Json.put(intent, "subtypetag", Json.getString(myconfig, "name"));
            }

            if (intent.has("tag"))
            {
                Json.put(intent, "subtypetag", Json.getString(intent, "tag"));
            }

            if (myconfig.has("iconres"))
            {
                int iconres = Json.getInt(myconfig, "iconres");

                if (iconres != 0)
                {
                    Json.put(intent, "iconres", iconres);
                }
            }

            if (myconfig.has("icon"))
            {
                String iconref = Json.getString(myconfig, "icon");

                if (Simple.startsWith(iconref, "http://") || Simple.startsWith(iconref, "https://"))
                {
                    if (myconfig.has("name"))
                    {
                        String iconname = Json.getString(myconfig, "name");
                        String iconpath = CacheManager.getWebIconPath(iconname, iconref);

                        Json.put(intent, "icon", "local://" + iconpath);
                    }
                }
                else
                {
                    int resourceId = Simple.getIconResourceId(iconref);

                    if (resourceId > 0)
                    {
                        Json.put(intent, "icon", resourceId);
                    }
                }
            }

            /*
            if (myconfig.has("packagename"))
            {
                String packageName = Json.getString(myconfig, "packagename");

                if (packageName != null)
                {
                    CommonConfigs.weLikeThis(packageName);
                    Drawable appIcon = VersionUtils.getIconFromApplication(context, packageName);

                    if (appIcon != null)
                    {
                        icon.setImageDrawable(appIcon);
                    }
                    else
                    {
                        Drawable drawable = CacheManager.getAppIcon(packageName);

                        if (drawable != null)
                        {
                            icon.setImageDrawable(drawable);
                        }
                        else
                        {
                            icon.setImageResource(R.drawable.stop_512x512);
                        }
                    }
                }
            }
            */
        }

        return intent;
    }

    @Override
    public boolean onCollectVoiceIntent(VoiceIntent voiceintent)
    {
        if (directory != null)
        {
            return directory.onCollectVoiceIntent(voiceintent);
        }

        if (config != null)
        {
            if (config.has("intent"))
            {
                JSONObject intent = Json.getObject(config, "intent");
                return voiceintent.collectIntent(prepareIntent(config, intent), identifier);
            }

            if (config.has("intents"))
            {
                JSONArray intents = Json.getArray(config, "intents");
                return voiceintent.collectIntents(prepareIntents(config, intents), identifier);
            }

            if (config.has("launchitems"))
            {
                //
                // Inspect configured launch items.
                //

                JSONArray launchitems = Json.getArray(config, "launchitems");
                if (launchitems == null) return false;

                boolean foundone = false;

                for (int inx = 0; inx < launchitems.length(); inx++)
                {
                    JSONObject launchitem = Json.getObject(launchitems, inx);
                    String identifier = Json.getString(launchitem, "identifier");
                    if (identifier == null) continue;

                    JSONObject intent = Json.getObject(launchitem, "intent");

                    if (voiceintent.collectIntent(prepareIntent(launchitem, intent), identifier))
                    {
                        foundone = true;
                    }

                    JSONArray intents = Json.getArray(launchitem, "intents");

                    if (voiceintent.collectIntents(prepareIntents(launchitem, intents), identifier))
                    {
                        foundone = true;
                    }
                }

                return foundone;
            }
        }

        return false;
    }

    @Override
    public boolean onResolveVoiceIntent(VoiceIntent voiceintent)
    {
        if (directory != null)
        {
            return directory.onResolveVoiceIntent(voiceintent);
        }

        if (config != null)
        {
            if (config.has("intent"))
            {
                JSONObject intent = Json.getObject(config, "intent");
                return voiceintent.evaluateIntent(intent, identifier);
            }

            if (config.has("intents"))
            {
                JSONArray intents = Json.getArray(config, "intents");
                return voiceintent.evaluateIntents(intents, identifier);
            }

            if (config.has("launchitems"))
            {
                //
                // Inspect configured launch items.
                //

                JSONArray launchitems = Json.getArray(config, "launchitems");
                if (launchitems == null) return false;

                boolean foundone = false;

                for (int inx = 0; inx < launchitems.length(); inx++)
                {
                    JSONObject launchitem = Json.getObject(launchitems, inx);
                    String identifier = Json.getString(launchitem, "identifier");
                    if (identifier == null) continue;

                    JSONObject intent = Json.getObject(launchitem, "intent");
                    if (voiceintent.evaluateIntent(intent, identifier)) foundone = true;

                    JSONArray intents = Json.getArray(launchitem, "intents");
                    if (voiceintent.evaluateIntents(intents, identifier)) foundone = true;
                }

                return foundone;
            }
        }

        return false;
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (directory != null)
        {
            return directory.onExecuteVoiceIntent(voiceintent, index);
        }

        if (config != null)
        {
            JSONArray matches = voiceintent.getMatches();
            if ((matches == null) || (index >= matches.length())) return false;

            JSONObject match = Json.getObject(matches, index);
            if (match == null) return false;

            String identifier = Json.getString(match, "identifier");
            if (identifier == null) return false;

            if (config.has("intent") || config.has("intents"))
            {
                return Simple.equals(identifier, this.identifier);
            }

            if (config.has("launchitems"))
            {
                //
                // Inspect configured launch items.
                //

                JSONArray launchitems = Json.getArray(config, "launchitems");
                if (launchitems == null) return false;

                for (int inx = 0; inx < launchitems.length(); inx++)
                {
                    JSONObject launchitem = Json.getObject(launchitems, inx);
                    if (launchitem == null) continue;

                    String configidentifier = Json.getString(launchitem, "identifier");
                    if (configidentifier == null) continue;

                    if (identifier.equals(configidentifier))
                    {
                        //
                        // Create and click an unattached launch icon.
                        //

                        LaunchItem.createLaunchItem(context, null, launchitem).onMyClick();

                        return true;
                    }
                }
            }
        }

        return false;
    }
}