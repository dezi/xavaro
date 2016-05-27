package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.VoiceIntent;
import de.xavaro.android.common.WebApp;
import de.xavaro.android.common.WebAppView;

public class LaunchItemWebApp extends LaunchItem
{
    private final static String LOGTAG = LaunchItemWebApp.class.getSimpleName();

    public LaunchItemWebApp(Context context)
    {
        super(context);
    }

    @Override
    protected void setConfig()
    {
        if (subtype != null)
        {
            Json.put(config, "label", WebApp.getLabel(subtype));
            icon.setImageResource(subtype);
        }
        else
        {
            icon.setImageResource(GlobalConfigs.IconResWebApps);

            if (directory == null)
            {
                directory = new LaunchGroupWebApps(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("webapp")) launchWebapp();
    }

    @Override
    public void onBackKeyExecuted()
    {
        Log.d(LOGTAG, "onBackKeyExecuted:");

        if ((subtype != null) && (webappFrame != null))
        {
            WebAppView webappView = webappFrame.getWebAppView();

            if (webappView != null)
            {
                Simple.removeFromParent(webappView);
                webappView.destroy();
            }

            Simple.removeFromParent(webappFrame);
            webappFrame = null;
        }
    }

    private LaunchFrameWebApp webappFrame;
    private WebAppView webappVoice;

    private void launchWebapp()
    {
        if (subtype != null)
        {
            if (webappFrame == null)
            {
                webappFrame = new LaunchFrameWebApp(context);
                webappFrame.setWebAppName(subtype);
                webappFrame.setParent(this);
            }

            if (Json.has(config, "javacall"))
            {
                //
                // Execute this when page is done with loading.
                //

                final String javacall = Json.getString(config, "javacall");

                webappFrame.getWebAppView().webapploader.setOnPageFinishedRunner(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        webappFrame.getWebAppView().evaluateJavascript(javacall, null);
                    }
                });
            }

            String label = Json.getString(config, "label");
            ((HomeActivity) context).addWorkerToBackStack(label, webappFrame);
        }
        else
        {
            ((HomeActivity) context).addViewToBackStack(directory);
        }
    }

    private final Runnable onUnloadVoice = new Runnable()
    {
        @Override
        public void run()
        {
            Log.d(LOGTAG, "onUnloadVoice");

            webappVoice = null;
        }
    };

    private void onDoVoiceIntent(VoiceIntent voiceintent, int index)
    {
        Log.d(LOGTAG, "onDoVoiceIntent");

        if (webappVoice.request != null)
        {
            webappVoice.request.doVoiceIntent(voiceintent, index);
        }
    }

    @Override
    public boolean onExecuteVoiceIntent(VoiceIntent voiceintent, int index)
    {
        if (super.onExecuteVoiceIntent(voiceintent, index))
        {
            final VoiceIntent cbvoiceintent = voiceintent;
            final int cbindex = index;

            if (subtype != null)
            {
                JSONObject intent = voiceintent.getMatch(index);
                Json.put(intent, "command", voiceintent.getCommand());

                if (Json.equals(intent, "mode", "speak") && WebApp.hasVoice(subtype))
                {
                    //
                    // Start voice.js of web app w/o attached view.
                    //

                    getHandler().removeCallbacks(onUnloadVoice);

                    int delay = 0;

                    if (webappVoice == null)
                    {
                        webappVoice = new WebAppView(Simple.getAppContext());
                        webappVoice.loadWebView(subtype, "voice");

                        delay = 1000;
                    }

                    getHandler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            onDoVoiceIntent(cbvoiceintent, cbindex);
                        }
                    }, delay);

                    getHandler().postDelayed(onUnloadVoice, 20 * 1000);
                }
                else
                {
                    //
                    // Start main.js of web app with attached view.
                    //

                    launchWebapp();

                    getHandler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            webappFrame.onExecuteVoiceIntent(cbvoiceintent, cbindex);
                        }
                    }, 1000);
                }
            }

            return true;
        }

        return false;
    }
}
