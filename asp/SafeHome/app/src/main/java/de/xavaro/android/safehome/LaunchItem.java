package de.xavaro.android.safehome;

import android.support.annotation.Nullable;

import android.graphics.Color;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.CacheManager;
import de.xavaro.android.common.Chooser;
import de.xavaro.android.common.CommonConfigs;
import de.xavaro.android.common.ImageSmartView;
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
        if (Simple.equals(type, "battery"     )) item = new LaunchItemBattery(context);
        if (Simple.equals(type, "health"      )) item = new LaunchItemHealth(context);
        if (Simple.equals(type, "alertcall"   )) item = new LaunchItemAlertcall(context);
        if (Simple.equals(type, "calls"       )) item = new LaunchItemCall(context);

        if (Simple.equals(type, "apps"        )) item = new LaunchItemApps(context);
        if (Simple.equals(suty, "media/image" )) item = new LaunchItemMediaImage(context);
        if (Simple.equals(suty, "media/video" )) item = new LaunchItemMediaVideo(context);

        if (Simple.equals(type, "phone"       )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "skype"       )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "whatsapp"    )) item = new LaunchItemComm(context);
        if (Simple.equals(type, "contacts"    )) item = new LaunchItemComm(context);

        if (Simple.equals(type, "social"      )) item = new LaunchItemSocial(context);
        if (Simple.equals(type, "facebook"    )) item = new LaunchItemSocial(context);
        if (Simple.equals(type, "instagram"   )) item = new LaunchItemSocial(context);
        if (Simple.equals(type, "googleplus"  )) item = new LaunchItemSocial(context);
        if (Simple.equals(type, "twitter"     )) item = new LaunchItemSocial(context);

        if (Simple.equals(type, "xavaro"      )) item = new LaunchItemCommXavaro(context);

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

    protected ImageSmartView icon;
    protected TextView label;
    protected String labelText;

    protected FrameLayout overlay;
    protected ImageSmartView overicon;
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

        icon = new ImageSmartView(context);
        icon.setPadding(20, 0, 20, 40);
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

        overicon = new ImageSmartView(context);
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
            label.setLineSpacing(-3 * Simple.getDensity(), 1);

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

                    label.setMaxWidth(laywid + (laywid / 4));
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
                    String iconpath = CacheManager.getWebIconPath(iconname, iconref);
                    icon.setImageResource(iconpath);
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

    @Override
    public void onCollectVoiceIntent(VoiceIntent voiceintent)
    {
        if (directory != null)
        {
            directory.onCollectVoiceIntent(voiceintent);

            return;
        }

        if (config != null)
        {
            if (config.has("intent"))
            {
                JSONObject intent = Json.getObject(config, "intent");
                voiceintent.collectIntent(config, intent);
            }

            if (config.has("intents"))
            {
                JSONArray intents = Json.getArray(config, "intents");
                voiceintent.collectIntents(config, intents);
            }

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
        if (directory != null)
        {
            directory.onResolveVoiceIntent(voiceintent);

            return;
        }

        if (config != null)
        {
            if (config.has("intent"))
            {
                JSONObject intent = Json.getObject(config, "intent");
                voiceintent.evaluateIntent(config, intent);
            }

            if (config.has("intents"))
            {
                JSONArray intents = Json.getArray(config, "intents");
                voiceintent.evaluateIntents(config, intents);
            }

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
                if (Simple.equals(identifier, this.identifier)) return true;
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