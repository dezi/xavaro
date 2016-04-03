package de.xavaro.android.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

public class Chooser implements DialogInterface.OnClickListener
{
    private final static String LOGTAG = Chooser.class.getSimpleName();

    public Chooser(String title, String[] keys, String[] vals)
    {
        this.title = title;

        this.keys = keys;
        this.vals = vals;
    }

    public Chooser(String[] keys, String[] vals)
    {
        this(null, keys, vals);
    }

    public Chooser(String title, ArrayList<String> keys, ArrayList<String> vals)
    {
        this.title = title;

        this.keys = keys.toArray(new String[ keys.size() ]);
        this.vals = vals.toArray(new String[ vals.size() ]);
    }

    public Chooser(ArrayList<String> keys, ArrayList<String> vals)
    {
        this(null, keys, vals);
    }

    public Chooser(String title, Map<String, String> map)
    {
        this.title = title;

        keys = new String[ map.size() ];
        vals = new String[ map.size() ];

        int inx = 0;
        for (String key : map.keySet())
        {
            keys[ inx ] = key;
            vals[ inx ] = map.get(key);
            inx++;
        }
    }

    public Chooser(Map<String, String> map)
    {
        this(null, map);
    }

    private ChooserResultCallback callback;

    private String title;
    private String[] keys;
    private String[] vals;
    private String defkey;
    private String reskey;

    private Context getContext()
    {
        return Simple.getAppContext();
    }

    public void setDefault(String defval)
    {
        this.defkey = defval;
        this.reskey = defval;
    }

    public void setOnChooserResult(ChooserResultCallback callback)
    {
        this.callback = callback;
    }

    public void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        builder.setPositiveButton("Ok", this);
        builder.setNegativeButton("Abbrechen", this);

        AlertDialog dialog = builder.create();

        RadioGroup rg = new RadioGroup(getContext());
        rg.setOrientation(RadioGroup.VERTICAL);
        rg.setPadding(40, 10, 0, 0);

        for (int inx = 0; inx < keys.length; inx++)
        {
            RadioButton rb = new RadioButton(getContext());

            rb.setId(4711 + inx);
            rb.setTextSize(24f);
            rb.setPadding(0, 20, 0, 18);
            rb.setTag(keys[ inx ]);
            rb.setText(vals[ inx ]);
            rb.setChecked(Simple.equals(keys[ inx ], defkey));

            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked)
                {
                    if (checked) reskey = (String) compoundButton.getTag();
                }
            });

            rg.addView(rb);
        }

        dialog.setView(rg);
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24f);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(24f);
    }

    public void onClick(DialogInterface dialog, int which)
    {
        if ((which == DialogInterface.BUTTON_POSITIVE) && (callback != null))
        {
            callback.onChooserResult(reskey);
        }

        dialog.cancel();
    }

    public interface ChooserResultCallback
    {
        void onChooserResult(String key);
    }
}
