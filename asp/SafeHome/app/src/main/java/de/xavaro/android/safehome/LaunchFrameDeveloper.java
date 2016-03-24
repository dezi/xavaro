package de.xavaro.android.safehome;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.IdentityManager;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.PersistManager;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;

public class LaunchFrameDeveloper extends LaunchFrame
{
    private static final String LOGTAG = LaunchFrameDeveloper.class.getSimpleName();

    private String subtype;
    private ImageView update;
    private ListView listview;
    private ScrollView scrollview;

    private TextView jsonListing;
    private JSONAdapter jsonAdapter;

    private JSONArray jsonprefs;

    public LaunchFrameDeveloper(Context context)
    {
        super(context);

        FrameLayout.LayoutParams lp;

        lp = new FrameLayout.LayoutParams(
                Simple.getActionBarHeight(),
                Simple.getActionBarHeight(),
                Gravity.END + Gravity.BOTTOM);

        lp.setMargins(8, 8, 8, 8);

        update = new ImageView(getContext());
        update.setLayoutParams(lp);
        update.setImageResource(R.drawable.sendmessage_430x430);

        update.setLongClickable(true);
        update.setOnClickListener(onUpdateClick);
        update.setOnLongClickListener(onUpdateLongClick);

        addView(update);
    }

    public void setSubtype(String subtype)
    {
        this.subtype = subtype;

        reload();
    }

    private void reload()
    {
        if (Simple.equals(subtype,"preferences")) loadPreferences();
        if (Simple.equals(subtype,"settings")) loadSettings();
        if (Simple.equals(subtype,"identities")) loadIdentities();

        if (Simple.equals(subtype,"contacts")) loadContacts();

        if (Simple.equals(subtype,"rcontacts")) loadRemoteContacts();
        if (Simple.equals(subtype,"rgroups")) loadRemoteGroups();

        if (Simple.equals(subtype,"cache")) loadStorageCache();
        if (Simple.equals(subtype,"sdcard")) loadStorageSDCard();
        if (Simple.equals(subtype,"known")) loadStorageKnown();

        if (Simple.equals(subtype,"webappcache")) loadWebappCache();
        if (Simple.equals(subtype,"events")) loadEvents();
        if (Simple.equals(subtype,"activity")) loadActivity();
    }

    private View.OnClickListener onUpdateClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            reload();
        }
    };

    private View.OnLongClickListener onUpdateLongClick = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View view)
        {
            if (Simple.equals(subtype,"events")) clearEvents();
            if (Simple.equals(subtype,"activitiy")) clearActivity();
            if (Simple.equals(subtype,"webappcache")) clearWebappCache();

            return true;
        }
    };

    private void loadSettings()
    {
        if (scrollview == null)
        {
            scrollview = new ScrollView(getContext());
            scrollview.setBackgroundColor(0xffffc080);
            addView(scrollview, 0);

            jsonListing = new TextView(getContext());
            jsonListing.setPadding(16, 16, 16, 16);
            jsonListing.setTextSize(Simple.getDeviceTextSize(18f));

            scrollview.addView(jsonListing);
        }

        JSONObject root = PersistManager.getRoot();
        jsonListing.setText(Json.toPretty(root));
    }

    private void loadStorageKnown()
    {
        if (scrollview == null)
        {
            scrollview = new ScrollView(getContext());
            scrollview.setBackgroundColor(0xffffc080);
            addView(scrollview, 0);

            jsonListing = new TextView(getContext());
            jsonListing.setPadding(16, 16, 16, 16);
            jsonListing.setTextSize(Simple.getDeviceTextSize(18f));

            scrollview.addView(jsonListing);
        }

        File file = new File(Simple.getFilesDir(), "filestats.act.json");
        String json = Simple.getFileContent(file);
        jsonListing.setText(json);
    }

    private void clearWebappCache()
    {
        Log.d(LOGTAG, "clearWebappCache: ...");

        File file = new File(Simple.getCacheDir(), "webappcache");
        removeDirectories(file);

        JSONObject empty = new JSONObject();
        File act = new File(Simple.getFilesDir(), "webappcache.act.json");
        Simple.putFileContent(act, Json.toPretty(empty));

        loadWebappCache();
    }

    private void loadWebappCache()
    {
        if (scrollview == null)
        {
            scrollview = new ScrollView(getContext());
            scrollview.setBackgroundColor(0xffffc080);
            addView(scrollview, 0);

            jsonListing = new TextView(getContext());
            jsonListing.setPadding(16, 16, 16, 16);
            jsonListing.setTextSize(Simple.getDeviceTextSize(18f));

            scrollview.addView(jsonListing);
        }

        File file = new File(Simple.getFilesDir(), "webappcache.act.json");
        String json = Simple.getFileContent(file);
        jsonListing.setText(json);
    }

    private void clearEvents()
    {
        Log.d(LOGTAG, "clearEvents: ...");

        JSONObject empty = new JSONObject();
        File act = new File(Simple.getFilesDir(), "events.act.json");
        Simple.putFileContent(act, Json.toPretty(empty));

        loadEvents();
    }

    private void loadEvents()
    {
        if (scrollview == null)
        {
            scrollview = new ScrollView(getContext());
            scrollview.setBackgroundColor(0xffffc080);
            addView(scrollview, 0);

            jsonListing = new TextView(getContext());
            jsonListing.setPadding(16, 16, 16, 16);
            jsonListing.setTextSize(Simple.getDeviceTextSize(18f));

            scrollview.addView(jsonListing);
        }

        File file = new File(Simple.getFilesDir(), "events.act.json");
        String json = Simple.getFileContent(file);
        jsonListing.setText(json);
    }

    private void clearActivity()
    {
        Log.d(LOGTAG, "clearActivity: ...");

        JSONObject empty = new JSONObject();
        File act = new File(Simple.getFilesDir(), Simple.getPackageName() + ".activities.act.json");
        Simple.putFileContent(act, Json.toPretty(empty));

        loadActivity();
    }

    private void loadActivity()
    {
        if (scrollview == null)
        {
            scrollview = new ScrollView(getContext());
            scrollview.setBackgroundColor(0xffffc080);
            addView(scrollview, 0);

            jsonListing = new TextView(getContext());
            jsonListing.setPadding(16, 16, 16, 16);
            jsonListing.setTextSize(Simple.getDeviceTextSize(18f));

            scrollview.addView(jsonListing);
        }

        File file = new File(Simple.getFilesDir(), Simple.getPackageName() + ".activities.act.json");
        String json = Simple.getFileContent(file);
        jsonListing.setText(json);
    }

    private void removeDirectories(File dir)
    {
        File[] list = dir.listFiles();

        for (File item : list)
        {
            if (item.isDirectory()) removeDirectories(item);

            item.delete();
        }
    }

    private void recurseDirectories(File dir, int plen)
    {
        File[] list = dir.listFiles();

        for (File item : list)
        {
            String path = item.toString();
            JSONObject jfile = new JSONObject();
            Json.put(jfile, "k", "~" + path.substring(plen));
            jsonprefs.put(jfile);

            Log.d(LOGTAG, path);

            if (item.isDirectory()) recurseDirectories(item, plen);
        }
    }

    private void loadStorage(File root)
    {
        Log.d(LOGTAG, "loadStorage: " + root.toString());

        if (listview == null)
        {
            listview = new ListView(getContext());
            listview.setBackgroundColor(0xffffc080);

            addView(listview, 0);
        }

        jsonprefs = new JSONArray();

        recurseDirectories(root, root.toString().length());

        jsonprefs = Json.sort(jsonprefs, "k", false);

        jsonAdapter = new JSONAdapter();
        listview.setAdapter(jsonAdapter);
        listview.setOnItemClickListener(jsonAdapter);
        listview.setOnItemLongClickListener(jsonAdapter);
    }

    void deleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void loadStorageCache()
    {
        File cache = Simple.getCacheDir();

        Simple.removeFiles(cache, "thumbnail.");

        deleteRecursive(new File(cache, "webappcache/tvscrape"));
        deleteRecursive(new File(cache, "org.chromium.android_webview"));

        loadStorage(cache);
    }

    private void loadStorageSDCard()
    {
        loadStorage(Simple.getAnyContext().getFilesDir());
    }

    private void loadContacts()
    {
        if (scrollview == null)
        {
            scrollview = new ScrollView(getContext());
            scrollview.setBackgroundColor(0xffffc080);
            addView(scrollview, 0);

            jsonListing = new TextView(getContext());
            jsonListing.setPadding(16, 16, 16, 16);
            jsonListing.setTextSize(Simple.getDeviceTextSize(18f));

            scrollview.addView(jsonListing);
        }

        JSONObject root = ContactsHandler.getJSONData(getContext());
        jsonListing.setText(Json.toPretty(root));
    }

    private void loadRemoteContacts()
    {
        if (listview == null)
        {
            listview = new ListView(getContext());
            listview.setBackgroundColor(0xffffc080);

            addView(listview, 0);
        }

        jsonprefs = new JSONArray();

        JSONObject records = PersistManager.getXpathJSONObject("RemoteContacts/identities");

        if (records != null)
        {
            Iterator<String> keysIterator = records.keys();

            while (keysIterator.hasNext())
            {
                String identity = keysIterator.next();
                JSONObject record = Json.getObject(records, identity);
                if (record == null) continue;

                Json.put(record, "identity", identity);
                Json.put(jsonprefs, record);
            }
        }

        jsonAdapter = new JSONAdapter();
        listview.setAdapter(jsonAdapter);
        listview.setOnItemClickListener(jsonAdapter);
        listview.setOnItemLongClickListener(jsonAdapter);
    }

    private void loadRemoteGroups()
    {
        if (listview == null)
        {
            listview = new ListView(getContext());
            listview.setBackgroundColor(0xffffc080);

            addView(listview, 0);
        }

        jsonprefs = new JSONArray();

        JSONObject records = PersistManager.getXpathJSONObject("RemoteGroups/groupidentities");

        if (records != null)
        {
            Iterator<String> keysIterator = records.keys();

            while (keysIterator.hasNext())
            {
                String uuid = keysIterator.next();
                JSONObject record = Json.getObject(records, uuid);
                if (record == null) continue;

                Json.put(jsonprefs, record);
            }
        }

        jsonAdapter = new JSONAdapter();
        listview.setAdapter(jsonAdapter);
        listview.setOnItemClickListener(jsonAdapter);
        listview.setOnItemLongClickListener(jsonAdapter);
    }

    private void loadIdentities()
    {
        if (listview == null)
        {
            listview = new ListView(getContext());
            listview.setBackgroundColor(0xffffc080);

            addView(listview, 0);
        }

        jsonprefs = new JSONArray();

        JSONObject records = PersistManager.getXpathJSONObject("IdentityManager/identities");

        if (records != null)
        {
            Iterator<String> keysIterator = records.keys();

            while (keysIterator.hasNext())
            {
                String identity = keysIterator.next();
                JSONObject record = Json.getObject(records, identity);
                if (record == null) continue;

                Json.put(record, "identity", identity);
                Json.put(jsonprefs, record);
            }
        }

        jsonAdapter = new JSONAdapter();
        listview.setAdapter(jsonAdapter);
        listview.setOnItemClickListener(jsonAdapter);
        listview.setOnItemLongClickListener(jsonAdapter);
    }

    private void loadPreferences()
    {
        if (listview == null)
        {
            listview = new ListView(getContext());
            listview.setBackgroundColor(0xffffc080);

            addView(listview, 0);
        }

        Map<String, Object> prefs = Simple.getAllPreferences(null);

        jsonprefs = new JSONArray();

        for (String prefkey : prefs.keySet())
        {
            if (prefkey.startsWith("firewall.")) continue;

            JSONObject jpref = new JSONObject();

            Json.put(jpref, "k", prefkey);
            Json.put(jpref, "v", prefs.get(prefkey));

            Json.put(jsonprefs, jpref);
        }

        jsonprefs = Json.sort(jsonprefs, "k", false);

        jsonAdapter = new JSONAdapter();
        listview.setAdapter(jsonAdapter);
        listview.setOnItemClickListener(jsonAdapter);
        listview.setOnItemLongClickListener(jsonAdapter);
    }

    private class JSONAdapter extends BaseAdapter implements
            AdapterView.OnItemClickListener,
            AdapterView.OnItemLongClickListener
    {
        public JSONAdapter()
        {
        }

        public int getCount()
        {
            return jsonprefs.length();
        }

        public JSONObject getItem(int position)
        {
            return Json.getObject(jsonprefs, position);
        }

        public long getItemId(int position)
        {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            LinearLayout layout = new LinearLayout(getContext());
            layout.setLayoutParams(Simple.layoutParamsMW());

            TextView itemView = new TextView(getContext());
            itemView.setLayoutParams(Simple.layoutParamsMW());
            itemView.setPadding(20, 0, 20, 0);
            itemView.setTextSize(Simple.getDeviceTextSize(18f));

            layout.addView(itemView);

            bindView(layout, position);

            return layout;
        }

        private void bindView(LinearLayout view, int position)
        {
            JSONObject item = getItem(position);

            ((TextView) view.getChildAt(0)).setText(Json.defuck(Json.toPretty(item)));
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {

        }

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.d(LOGTAG, "onItemLongClick: " + getItem(position).toString());

            if (Simple.equals(subtype, "preferences"))
            {
                JSONObject pref = getItem(position);
                String key = Json.getString(pref, "k");

                Simple.removeSharedPref(key);
            }

            if (Simple.equals(subtype, "cache"))
            {
                JSONObject pref = getItem(position);
                String key = Json.getString(pref, "k");

                File file = new File(Simple.getAnyContext().getCacheDir(), key.substring(2));
                Log.d(LOGTAG, "onItemClick: delete=" + file.toString());
                deleteRecursive(file);
            }

            if (Simple.equals(subtype, "sdcard"))
            {
                JSONObject pref = getItem(position);
                String key = Json.getString(pref, "k");

                File file = new File(Simple.getAnyContext().getFilesDir(), key.substring(2));
                Log.d(LOGTAG, "onItemClick: delete=" + file.toString());
                deleteRecursive(file);
            }

            if (Simple.equals(subtype, "rgroups"))
            {
                JSONObject pref = getItem(position);
                String groupidentity = Json.getString(pref, "groupidentity");

                RemoteGroups.removeGroupFinally(groupidentity);
            }

            if (Simple.equals(subtype, "rcontacts"))
            {
                JSONObject pref = getItem(position);
                String identity = Json.getString(pref, "identity");

                RemoteContacts.removeContactFinally(identity);
            }

            if (Simple.equals(subtype, "identities"))
            {
                JSONObject pref = getItem(position);
                String identity = Json.getString(pref, "identity");

                IdentityManager.removeIdentityFinally(identity);
            }

            return true;
        }
    }
}