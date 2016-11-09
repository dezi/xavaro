package de.xavaro.android.safehome;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
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

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout layout = new LinearLayout(Simple.getActContext());
        layout.setLayoutParams(Simple.layoutParamsMW());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        Simple.setPadding(layout, 10, 10, 10, 10);

        TextView itemView = new TextView(Simple.getActContext());
        itemView.setLayoutParams(Simple.layoutParamsMW());
        itemView.setPadding(20, 0, 20, 0);
        itemView.setTextSize(Simple.getDeviceTextSize(18f));

        layout.addView(itemView);

        bindViewxxx(layout, position);

        return layout;
    }

    private void bindViewxxx(LinearLayout view, int position)
    {
        JSONObject item = getItem(position);

        ((TextView) view.getChildAt(0)).setText(Json.defuck(Json.toPretty(item)));
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
