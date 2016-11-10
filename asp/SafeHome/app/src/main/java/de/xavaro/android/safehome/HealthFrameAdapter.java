package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.View;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.Json;

public class HealthFrameAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener
{
    private static final String LOGTAG = HealthFrameAdapter.class.getSimpleName();

    private String subtype;
    private JSONArray jsonArray;

    public void setContent(String subtype, JSONArray jsonArray)
    {
        this.subtype = subtype;
        this.jsonArray = jsonArray;
    }

    public int getCount()
    {
        return (jsonArray == null) ? 0: jsonArray.length();
    }

    public JSONObject getItem(int position)
    {
        return Json.getObject(jsonArray, position);
    }

    public long getItemId(int position)
    {
        return (long) position;
    }

    @SuppressLint("RtlHardcoded")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;

        if (view == null)
        {
            if (Simple.equals(subtype, "ecg")) view = HealthECG.getInstance().createListView();
        }

        JSONObject item = getItem(position);

        if (Simple.equals(subtype, "ecg")) HealthECG.getInstance().populateListView(view, position, item);

        return view;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(LOGTAG, "onItemClick: " + getItem(position).toString());
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(LOGTAG, "onItemLongClick: " + getItem(position).toString());

        return true;
    }
}
