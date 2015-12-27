package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LaunchSettings extends LaunchGroup implements
        DialogInterface.OnClickListener
{
    private static final String LOGTAG = LaunchSettings.class.getSimpleName();

    private static LaunchSettings instance;

    public static LaunchSettings getInstance(Context context)
    {
        if (instance == null) instance = new LaunchSettings(context);

        return instance;
    }

    private JSONObject settings;

    public LaunchSettings(Context context)
    {
        super(context);

        settings = StaticUtils.readRawTextResourceJSON(context, R.raw.default_settings);

        if ((settings == null) || !settings.has("launchgroup"))
        {
            Toast.makeText(context, "Keine <launchgroup> gefunden.", Toast.LENGTH_LONG).show();

            return;
        }

        try
        {
            setConfig(null, settings.getJSONObject("launchgroup"));
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
    }

    public void onClick(DialogInterface dialog, int which)
    {
        //m_Text = input.getText().toString();

        Log.d(LOGTAG,"onClick=" + which);

        if (which == DialogInterface.BUTTON_POSITIVE)
        {
            ((HomeActivity) context).addViewToBackStack(this);

            return;
        }

        dialog.cancel();
    }

    public void open()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Settings Password");

        final EditText input = new EditText(context);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(40, 40, 40, 40);
        input.setTextSize(48f);

        builder.setView(input);

        builder.setPositiveButton("OK", this);
        builder.setNegativeButton("Cancel", this);

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24f);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(24f);
    }
}
