package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;

import de.xavaro.android.common.ActivityManager;
import de.xavaro.android.common.Chooser;
import de.xavaro.android.common.CommSender;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.KnownFileManager;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Speak;

public abstract class LaunchItemMedia extends LaunchItemProxyPlayer
{
    private final static String LOGTAG = LaunchItemMedia.class.getSimpleName();

    public LaunchItemMedia(Context context)
    {
        super(context);
    }

    protected String mediatype;
    protected FileObserver observer;
    protected String obserdir;
    protected int numberItems;

    @Nullable
    protected Drawable getBestImage(String file)
    {
        if (Simple.isVideo(file))
        {
            String imgfile = Simple.changeExtension(file, ".jpg");
            if (new File(imgfile).exists()) file = imgfile;
        }

        if (Simple.isImage(file)) return Simple.getDrawableSquare(file, 200);

        return null;
    }

    @Override
    protected void setConfig()
    {
        if (config.has("mediadir"))
        {
            String mediadir = Json.getString(config, "mediadir");
            File mediapath = Simple.getMediaPath(mediadir);

            if (mediapath != null)
            {
                Simple.makeDirectory(mediapath);

                if (mediapath.isDirectory())
                {
                    int mask = FileObserver.CREATE | FileObserver.DELETE;
                    obserdir = mediapath.toString();

                    observer = new FileObserver(obserdir, mask)
                    {
                        @Override
                        public void onEvent(int event, String path)
                        {
                            final int pevent = event;
                            final String ppath = path;

                            post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    onFileObserverEvent(pevent, ppath);
                                }
                            });
                        }
                    };

                    observer.startWatching();
                }
            }
        }

        if (Simple.equals(subtype, "image")) icon.setImageResource(GlobalConfigs.IconResMediaImage);
        if (Simple.equals(subtype, "video")) icon.setImageResource(GlobalConfigs.IconResMediaVideo);
        if (Simple.equals(subtype, "audio")) icon.setImageResource(GlobalConfigs.IconResMediaAudio);
        if (Simple.equals(subtype, "ebook")) icon.setImageResource(GlobalConfigs.IconResMediaEbook);

        if (config.has("mediaitem"))
        {
            String mediaitem = Json.getString(config, "mediaitem");
            icon.setImageDrawable(getBestImage(mediaitem));

            boolean known = KnownFileManager.getKnownFileStatus(mediaitem);

            if (! known)
            {
                overicon.setImageResource(R.drawable.circle_green_256x256);
                overtext.setText(R.string.simple_new);
                overlay.setVisibility(VISIBLE);
            }
        }

        if (config.has("mediadir"))
        {
            String mediadir = Json.getString(config, "mediadir");
            File mediapath = Simple.getMediaPath(mediadir);

            if (mediapath != null)
            {
                if (!mediapath.exists()) Simple.makeDirectory(mediapath.getAbsolutePath());

                if (mediapath.isDirectory())
                {
                    FilenameFilter filter = new Simple.FileFilter();

                    if (mediatype.equals("image")) filter = new Simple.ImageFileFilter();
                    if (mediatype.equals("video")) filter = new Simple.VideoFileFilter();

                    JSONArray items = Simple.getDirectorySortedByAge(mediapath, filter, true);

                    if ((items != null) && (items.length() > 0))
                    {
                        JSONObject newest = Json.getObject(items, 0);
                        String file = Json.getString(newest, "file");
                        icon.setImageDrawable(getBestImage(file));

                        numberItems = items.length();

                        labelText = Json.getString(config, "label");
                        labelText += " (" + numberItems + ")";
                        setLabelText(labelText);
                    }

                    int newItems = KnownFileManager.checkDirectory(mediapath, items);

                    if (newItems == 0)
                    {
                        overlay.setVisibility(INVISIBLE);
                    }
                    else
                    {
                        String display = Integer.toString(newItems);

                        overicon.setImageResource(R.drawable.circle_green_256x256);
                        overtext.setText(display);
                        overlay.setVisibility(VISIBLE);
                    }
                }
            }
        }
    }

    protected void onFileObserverEvent(int event, String path)
    {
        if ((event == FileObserver.CREATE) || (event == FileObserver.DELETE))
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
            Map<String, String> options = Simple.getTransMap(R.array.launch_media_longclick_keys);
            if (options == null) return false;

            if (! Simple.getSharedPrefBoolean("media." + mediatype + ".simplesend.enable"))
            {
                options.remove("send");
            }

            if (! Simple.getSharedPrefBoolean("media." + mediatype + ".delete"))
            {
                options.remove("delete");
            }

            if (options.size() > 0)
            {
                String otext = Simple.getTransVal(R.array.launch_media_options_keys, mediatype);

                Chooser chooser = new Chooser(otext, options);
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
        if (Simple.equals(key, "send")) onSendMediaItem();
        if (Simple.equals(key, "delete")) onDeleteMediaItem();
    }

    private void onDeleteMediaItem()
    {
        String mediaitem = Json.getString(config, "mediaitem");
        if (mediaitem == null) return;

        File mediafile = new File(mediaitem);

        if (Simple.isVideo(mediafile.getAbsolutePath()))
        {
            File jpegfile = Simple.changeExtension(mediafile, ".jpg");
            File jsonfile = Simple.changeExtension(mediafile, ".json");

            if (jpegfile.delete()) Log.d(LOGTAG, "onDeleteMediaItem: del=" + jpegfile.toString());
            if (jsonfile.delete()) Log.d(LOGTAG, "onDeleteMediaItem: del=" + jsonfile.toString());
        }

        if (mediafile.delete())
        {
            parent.deleteLaunchItem(this);

            Log.d(LOGTAG, "onDeleteMediaItem: del=" + mediaitem);
        }
    }

    private void onSendMediaItem()
    {
        String mediaitem = Json.getString(config, "mediaitem");

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

            CommSender.sendFile(mediaitem, "incoming", idremote);

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

            //
            // Todo: generic...
            //

            String sm = Simple.getTrans(R.string.launch_media_image_simple_send, rmsg);
            String am = Simple.getTrans(R.string.launch_media_image_simple_yousend, rmsg);

            Speak.speak(sm);
            ActivityManager.recordActivity(am, mediaitem);
        }
    }
}
