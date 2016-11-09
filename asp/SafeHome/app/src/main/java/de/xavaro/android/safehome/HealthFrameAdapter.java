package de.xavaro.android.safehome;

import android.graphics.Typeface;
import android.view.Gravity;
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
        LinearLayout view;

        if (convertView instanceof LinearLayout)
        {
            view = (LinearLayout) convertView;
        }
        else
        {
            view = new LinearLayout(Simple.getActContext());
            view.setLayoutParams(Simple.layoutParamsMW());
            view.setOrientation(LinearLayout.HORIZONTAL);
            Simple.setPadding(view, 10, 10, 10, 10);

            LinearLayout dateLayout = new LinearLayout(Simple.getActContext());
            dateLayout.setLayoutParams(Simple.layoutParamsWW());
            dateLayout.setOrientation(LinearLayout.VERTICAL);
            dateLayout.setId(android.R.id.primary);
            view.addView(dateLayout);

            TextView dateView = new TextView(Simple.getActContext());
            dateView.setLayoutParams(Simple.layoutParamsWW());
            dateView.setTextSize(Simple.getDeviceTextSize(24f));
            dateView.setTypeface(null, Typeface.BOLD);
            dateView.setId(android.R.id.text1);
            dateLayout.addView(dateView);

            TextView timeView = new TextView(Simple.getActContext());
            timeView.setLayoutParams(Simple.layoutParamsWW());
            timeView.setTextSize(Simple.getDeviceTextSize(24f));
            timeView.setTypeface(null, Typeface.BOLD);
            timeView.setId(android.R.id.text2);
            dateLayout.addView(timeView);

            TextView pulseView = new TextView(Simple.getActContext());
            pulseView.setLayoutParams(Simple.layoutParamsWM());
            pulseView.setGravity(Gravity.CENTER_VERTICAL);
            Simple.setPadding(pulseView, 40, 0, 0, 0);
            pulseView.setTextSize(Simple.getDeviceTextSize(24f));
            pulseView.setTypeface(null, Typeface.BOLD);
            pulseView.setId(android.R.id.content);
            view.addView(pulseView);

            TextView jsonView = new TextView(Simple.getActContext());
            jsonView.setLayoutParams(Simple.layoutParamsWW());
            jsonView.setTextSize(Simple.getDeviceTextSize(18f));
            jsonView.setId(android.R.id.summary);
            //view.addView(jsonView);
        }

        JSONObject item = getItem(position);

        long dts = Simple.getTimeStamp(Json.getString(item, "dts"));

        TextView dateView = (TextView) view.findViewById(android.R.id.text1);
        dateView.setText(Simple.getLocaleDateMedium(dts));

        TextView timeView = (TextView) view.findViewById(android.R.id.text2);
        timeView.setText(Simple.getLocaleTime(dts));

        TextView pulseView = (TextView) view.findViewById(android.R.id.content);
        pulseView.setText("Puls" + ": " + Json.getInt(item, "pls"));

        //TextView jsonView = (TextView) view.findViewById(android.R.id.summary);
        //jsonView.setText(Json.toPretty(item));

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
