package de.xavaro.android.safehome;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.ActivityManager;
import de.xavaro.android.common.Chooser;
import de.xavaro.android.common.CommSender;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.NewFileManager;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public class LaunchItemMediaImage extends LaunchItemMedia
{
    private final static String LOGTAG = LaunchItemMediaImage.class.getSimpleName();

    public LaunchItemMediaImage(Context context)
    {
        super(context);
    }

    private int numberImages;
    private int newImages;

    @Override
    protected void setConfig()
    {
        super.setConfig();

        boolean found = false;

        if (config.has("mediaitem"))
        {
            String imagefile = Json.getString(config, "mediaitem");
            icon.setImageDrawable(Simple.getDrawableSquare(imagefile, 200));

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
                        icon.setImageDrawable(Simple.getDrawableSquare(file, 200));

                        numberImages = images.length();

                        labelText = Json.getString(config, "label");
                        labelText += " (" + numberImages + ")";
                        setLabelText(labelText);

                        found = true;
                    }

                    newImages = NewFileManager.checkDirectoryContent(mediapath.toString(), images);

                    Log.d(LOGTAG, "===========================> new=" + newImages);
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
    protected void onFileObserverEvent(int event, String path)
    {
        super.onFileObserverEvent(event, path);

        if (event == FileObserver.CREATE)
        {
            //
            // Update collection thumbnail image if required.
            //

            setConfig();
        }
    }

    @Override
    protected boolean onMyLongClick()
    {
        if (config.has("mediaitem"))
        {
            Map<String, String> options = Simple.getTransMap(
                    R.array.launch_media_image_longclick_keys);
            if (options == null) return false;

            if (! Simple.getSharedPrefBoolean("media.image.delete"))
            {
                options.remove("delete");
            }

            if (! Simple.getSharedPrefBoolean("media.image.simplesend.enable"))
            {
                options.remove("send");
            }

            if (options.size() > 0)
            {
                Chooser chooser = new Chooser("Dieses Bild", options);
                chooser.setOnChooserResult(this);
                chooser.setDefault("send");
                chooser.showDialog();

                return true;
            }
        }

        return false;
    }

    @Override
    public void onChooserResult(String key)
    {
        if (Simple.equals(key, "send")) onSendImage();
        if (Simple.equals(key, "delete")) onDeleteImage();
    }

    private void onDeleteImage()
    {
        String mediaitem = Json.getString(config, "mediaitem");
        if (mediaitem == null) return;

        File mediafile = new File(mediaitem);

        if (mediafile.delete())
        {
            Log.d(LOGTAG, "onDeleteImage: deleted=" + mediaitem);
        }
    }

    private void onSendImage()
    {
        String imagefile = Json.getString(config, "mediaitem");

        String prefix = "media.image.simplesend.contact.";
        Map<String, Object> contacts = Simple.getAllPreferences(prefix);

        ArrayList<String> receivers = new ArrayList<>();

        for (Map.Entry<String, Object> entry : contacts.entrySet())
        {
            if (!(entry.getValue() instanceof Boolean)) continue;
            if (!(boolean) entry.getValue()) continue;

            String prefkey = entry.getKey();
            String idremote = prefkey.substring(prefix.length(), prefkey.length());

            String name = RemoteContacts.getDisplayName(idremote);

            CommSender.sendFile(imagefile, "incoming", idremote);

            receivers.add(name);
        }

        if (receivers.size() > 0)
        {
            String rmsg = "";

            for (int inx = 0; inx < receivers.size(); inx++)
            {
                if ((inx > 0) && ((inx + 1) < receivers.size())) rmsg += ", ";

                if ((inx > 0) && ((inx + 1) == receivers.size()))
                {
                    rmsg += " " + Simple.getTrans(R.string.simple_and) + " ";
                }

                rmsg += receivers.get(inx);
            }

            String sm = Simple.getTrans(R.string.launch_media_image_simple_send, rmsg);
            String am = Simple.getTrans(R.string.launch_media_image_simple_yousend, rmsg);

            Speak.speak(sm);
            ActivityManager.recordActivity(am, imagefile);
        }
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

                    directory = new LaunchGroupMediaImage(context, mediadir);
                }
            }
            else
            {
                directory = new LaunchGroupMediaImage(context);
                directory.setConfig(this, Json.getArray(config, "launchitems"));
            }
        }

        ((HomeActivity) context).addViewToBackStack(directory);
    }
}
