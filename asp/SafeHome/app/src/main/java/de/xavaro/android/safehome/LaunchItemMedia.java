package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.ActivityManager;
import de.xavaro.android.common.CommSender;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class LaunchItemMedia extends LaunchItem
{
    private final static String LOGTAG = LaunchItemMedia.class.getSimpleName();

    public LaunchItemMedia(Context context)
    {
        super(context);
    }

    private int numberImages;

    @Override
    protected void setConfig()
    {
        boolean found = false;

        if (config.has("mediaitem"))
        {
            String imagefile = Json.getString(config, "mediaitem");
            icon.setImageDrawable(Simple.getDrawableThumbnailFromFile(imagefile, 200));

            found = true;
        }

        if (config.has("mediadir"))
        {
            String mediadir = Json.getString(config, "mediadir");
            File mediapath = Simple.getMediaPath(mediadir);

            if (mediapath != null)
            {
                if (! mediapath.exists())
                {
                    //noinspection ResultOfMethodCallIgnored
                    mediapath.mkdir();
                }

                if (mediapath.isDirectory())
                {
                    JSONArray images = Simple.getDirectorySortedByAge(
                            mediapath, new Simple.ImageFileFilter(), true);

                    if ((images != null) && (images.length() > 0))
                    {
                        JSONObject newest = Json.getObject(images, 0);
                        String file = Json.getString(newest, "file");
                        icon.setImageDrawable(Simple.getDrawableThumbnailFromFile(file, 200));

                        numberImages = images.length();

                        labelText = Json.getString(config, "label");
                        labelText += " (" + numberImages + ")";
                        setLabelText(labelText);

                        found = true;
                    }
                }
            }
        }

        if (! found)
        {
            if (Simple.equals(subtype, "image"))
            {
                icon.setImageResource(GlobalConfigs.IconResMediaImage);
            }
        }
    }

    @Override
    protected boolean onMyLongClick()
    {
        if (Simple.equals(subtype, "image") && config.has("mediaitem"))
        {
            if (Simple.getSharedPrefBoolean("media.image.simplesend.enable"))
            {
                String imagefile = Json.getString(config, "mediaitem");

                String prefix = "media.image.simplesend.contact.";
                Map<String, Object> contacts = Simple.getAllPreferences(prefix);

                ArrayList<String> receivers = new ArrayList<>();

                for (Map.Entry<String, Object> entry : contacts.entrySet())
                {
                    if (! (entry.getValue() instanceof Boolean)) continue;
                    if (! (boolean) entry.getValue()) continue;

                    String prefkey = entry.getKey();
                    String idremote = prefkey.substring(prefix.length(),prefkey.length());

                    String name = RemoteContacts.getDisplayName(idremote);

                    CommSender.sendFile(imagefile, "incoming", idremote);

                    receivers.add(name);
                }

                if (receivers.size() > 0)
                {
                    String receivermsg = "";

                    for (int inx = 0; inx < receivers.size(); inx++)
                    {
                        if ((inx > 0) && ((inx + 1) < receivers.size())) receivermsg += ", ";

                        if ((inx > 0) && ((inx + 1) == receivers.size()))
                        {
                            receivermsg += " " + Simple.getTrans(R.string.simple_and) + " ";
                        }

                        receivermsg += receivers.get(inx);
                    }

                    String dm = Simple.getTrans(R.string.launch_media_simple_send, receivermsg);
                    Speak.speak(dm);

                    String am = Simple.getTrans(R.string.launch_media_simple_yousend, receivermsg);

                    JSONObject actmess = new JSONObject();
                    Json.put(actmess, "message", am);
                    ActivityManager.onOutgoingMessage(actmess);
                }
            }
        }

        return false;
    }

    @Override
    protected void onMyClick()
    {
        if (Simple.equals(subtype, "image")) launchImage();
    }

    private void launchImage()
    {
        if (config.has("mediaitem"))
        {
            // todo display image.

            return;
        }

        if (directory == null)
        {
            if (config.has("mediadir"))
            {
                if (numberImages == 0)
                {
                    String message = "Es sind keine Bilder enthalten.";
                    Speak.speak(message);

                    return;
                }
                else
                {
                    String mediadir = Json.getString(config, "mediadir");

                    directory = new LaunchGroupMedia.ImageGroup(context, mediadir);
                }
            }
            else
            {
                directory = new LaunchGroupMedia.ImageGroup(context);
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
