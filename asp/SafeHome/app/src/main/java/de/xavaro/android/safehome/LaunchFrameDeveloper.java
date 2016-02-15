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
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.PersistManager;
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
        if (Simple.equals(subtype,"contacts")) loadContacts();
        if (Simple.equals(subtype,"settings")) loadSettings();
        if (Simple.equals(subtype,"preferences")) loadPreferences();
        if (Simple.equals(subtype,"temporary")) loadStorageTemp();
        if (Simple.equals(subtype,"sdcard")) loadStorageSDCard();
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
            jsonListing.setTextSize(18f);

            scrollview.addView(jsonListing);
        }

        JSONObject root = PersistManager.getRoot();
        jsonListing.setText(Json.toPretty(root));
    }

    private void recurseDirectories(File dir, int plen)
    {
        File[] list = dir.listFiles();

        for (File item : list)
        {
            String path = item.toString();
            JSONObject jfile = new JSONObject();
            Json.put(jfile, "k", "..." + path.substring(plen));
            jsonprefs.put(jfile);

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

    private void loadStorageTemp()
    {
        File temp = Simple.getAnyContext().getCacheDir();

        deleteRecursive(new File(temp, "org.chromium.android_webview"));

        loadStorage(temp);
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
            jsonListing.setTextSize(18f);

            scrollview.addView(jsonListing);
        }

        JSONObject root = ContactsHandler.getJSONData(getContext());
        jsonListing.setText(Json.toPretty(root));
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
    }

    private class JSONAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
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
            itemView.setTextSize(18f);

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
            Log.d(LOGTAG, "onItemClick: " + getItem(position).toString());

            JSONObject pref = getItem(position);
            String key = Json.getString(pref, "k");

            Simple.removeSharedPref(key);
        }
    }
}