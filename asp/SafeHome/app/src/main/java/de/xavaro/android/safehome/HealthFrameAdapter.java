package de.xavaro.android.safehome;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

            LinearLayout iconLayout = new LinearLayout(Simple.getActContext());
            iconLayout.setLayoutParams(Simple.layoutParamsWM());
            iconLayout.setOrientation(LinearLayout.HORIZONTAL);
            iconLayout.setGravity(Gravity.CENTER_VERTICAL);
            view.addView(iconLayout);

            ImageView okView = new ImageView(Simple.getActContext());
            okView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
            okView.setId(android.R.id.button1);
            okView.setImageResource(R.drawable.health_ecg_ok_300x200);
            Simple.setPadding(okView, 20, 0, 0, 0);
            iconLayout.addView(okView);

            ImageView rhythmView = new ImageView(Simple.getActContext());
            rhythmView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
            rhythmView.setId(android.R.id.button2);
            rhythmView.setImageResource(R.drawable.health_ecg_rhythm_dim_300x200);
            Simple.setPadding(rhythmView, 20, 0, 0, 0);
            iconLayout.addView(rhythmView);

            ImageView waveView = new ImageView(Simple.getActContext());
            waveView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(90),Simple.WC));
            waveView.setId(android.R.id.button3);
            waveView.setImageResource(R.drawable.health_ecg_wave_dim_300x200);
            Simple.setPadding(waveView, 20, 0, 0, 0);
            iconLayout.addView(waveView);

            LinearLayout buttonLayout = new LinearLayout(Simple.getActContext());
            buttonLayout.setLayoutParams(Simple.layoutParamsMM());
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            buttonLayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            view.addView(buttonLayout);

            ImageView buttonView = new ImageView(Simple.getActContext());
            buttonView.setLayoutParams(Simple.layoutParamsXX(Simple.DP(70),Simple.WC));
            buttonView.setId(android.R.id.toggle);
            buttonView.setImageResource(R.drawable.health_ecg_display_256x256);
            Simple.setPadding(buttonView, 0, 0, 20, 0);
            buttonLayout.addView(buttonView);

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
