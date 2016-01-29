package de.xavaro.android.safehome;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;

public class LaunchItemWebFrame extends LaunchItem
{
    private final static String LOGTAG = LaunchItemWebFrame.class.getSimpleName();

    public LaunchItemWebFrame(Context context)
    {
        super(context);
    }

    private WebFrame webframe;

    @Override
    protected void setConfig()
    {
        if (Simple.equals(type, "webframe"))
        {
            if (config.has("name"))
            {
                String name = Json.getString(config, "name");

                label.setText(WebFrame.getConfigLabel(context, name));
                icon.setImageDrawable(WebFrame.getConfigIconDrawable(context, name));
            }
        }

        if (Simple.equals(type, "webconfig") && config.has("subtype"))
        {
            String subtype = Json.getString(config, "subtype");

            if (Simple.equals(subtype, "newspaper"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigNewspaper);
            }

            if (Simple.equals(subtype, "magazine"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigMagazine);
            }

            if (Simple.equals(subtype, "pictorial"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigPictorial);
            }

            if (Simple.equals(subtype, "shopping"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigShopping);
            }

            if (Simple.equals(subtype, "erotics"))
            {
                icon.setImageResource(GlobalConfigs.IconResWebConfigErotics);
            }
        }
    }

    @Override
    protected void onMyClick()
    {
        if (type.equals("webframe" )) launchWebFrame();
        if (type.equals("webconfig")) launchWebConfig();
    }

    private void launchWebConfig()
    {
        if (!config.has("subtype"))
        {
            Toast.makeText(getContext(), "Nix <subtype> configured.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            if (directory == null)
            {
                String subtype = config.getString("subtype");

                directory = new LaunchGroupWebStream(context, this, "webconfig", subtype);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }

    private void launchWebFrame()
    {
        if (!config.has("name"))
        {
            Toast.makeText(getContext(), "Nix <name> configured.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            if (webframe == null)
            {
                String name = config.getString("name");
                String url = WebFrame.getConfigUrl(context, name);

                webframe = new WebFrame(context);
                webframe.setLoadURL(name, url);
            }

            ((HomeActivity) context).addViewToBackStack(webframe);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
