package de.xavaro.android.safehome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

public class LaunchGroupPreferences extends LaunchGroup implements
        DialogInterface.OnClickListener
{
    private static final String LOGTAG = LaunchGroupPreferences.class.getSimpleName();

    private static LaunchGroupPreferences instance;

    public static LaunchGroupPreferences getInstance(Context context)
    {
        if (instance == null) instance = new LaunchGroupPreferences(context);

        return instance;
    }

    private JSONObject settings;

    public LaunchGroupPreferences(Context context)
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

    private EditText pwedit;

    public void open()
    {
        if (Simple.getSharedPrefString("admin.password") == null)
        {
            ((HomeActivity) context).addViewToBackStack(this);

            ArchievementManager.show("configure.settings.*");

            return;
        }

        //
        // Build and open password dialog.
        //

        ArchievementManager.archieved("howto.open.settings");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Settings Password");

        pwedit = new EditText(context);

        pwedit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pwedit.setPadding(40, 40, 40, 40);
        pwedit.setTextSize(48f);

        builder.setView(pwedit);

        builder.setPositiveButton("Ok", this);
        builder.setNegativeButton("Cancel", this);

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24f);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(24f);
    }

    public void onClick(DialogInterface dialog, int which)
    {
        String pwreal = Simple.getSharedPrefString("admin.password");
        String pwuser = pwedit.getText().toString();

        if ((which == DialogInterface.BUTTON_POSITIVE) && Simple.equals(pwreal, pwuser))
        {
            ((HomeActivity) context).addViewToBackStack(this);

            ArchievementManager.show("configure.settings.*");

            return;
        }

        dialog.cancel();
    }
}
