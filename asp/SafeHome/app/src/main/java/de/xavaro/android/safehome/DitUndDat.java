package de.xavaro.android.safehome;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

//
// Utility namespace for included small classes.
//
public class DitUndDat
{
    //region public static class Ranking

    public static class Ranking extends LinearLayout
    {
        public Ranking(Context context)
        {
            super(context);

            init();
        }

        public Ranking(Context context, AttributeSet attrs)
        {
            super(context, attrs);

            init();
        }

        public Ranking(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);

            init();
        }

        private final ImageView stars[] = new ImageView[ 5 ];

        public void init()
        {
            for (int inx = 0; inx < 5; inx++)
            {
                stars[ inx ] = new ImageView(getContext());
                stars[ inx ].setImageResource(R.drawable.score_0_40x40);

                addView(stars[ inx ]);
            }
        }
    }

    //endregion public static class Ranking

    //region public static class Toolbar

    public static class Toolbar extends FrameLayout
    {
        private final static String LOGTAG = Toolbar.class.getSimpleName();

        public Toolbar(Context context)
        {
            super(context);

            init();
        }

        public Toolbar(Context context, AttributeSet attrs)
        {
            super(context, attrs);

            init();
        }

        public Toolbar(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);

            init();
        }

        public ImageView icon;
        public TextView title;
        public TextView subtitle;
        public ImageView trash;

        public void init()
        {
            int abheight = Simple.getActionBarHeight();

            FrameLayout.LayoutParams lp;

            setLayoutParams(new FrameLayout.LayoutParams(Simple.MP, abheight));
            setBackgroundColor(0xffff3456);

            icon = new ImageView(getContext());

            icon.setLayoutParams(new FrameLayout.LayoutParams(abheight, abheight));
            icon.setPadding(10, 10, 10, 10);

            addView(icon);

            title = new TextView(getContext());

            title.setGravity(Gravity.BOTTOM);
            title.setTextColor(Color.WHITE);
            title.setTextSize(24f);
            title.setSingleLine();

            int magic = abheight / 16;

            lp = new FrameLayout.LayoutParams(Simple.WC, abheight / 2 + magic, Gravity.TOP);
            lp.leftMargin = abheight;

            addView(title, lp);

            subtitle = new TextView(getContext());

            subtitle.setGravity(Gravity.TOP);
            subtitle.setTextColor(Color.WHITE);
            subtitle.setSingleLine();

            lp = new FrameLayout.LayoutParams(Simple.WC, abheight / 2 - magic, Gravity.BOTTOM);
            lp.leftMargin = abheight;

            addView(subtitle, lp);

            trash = new ImageView(getContext());

            trash.setPadding(10, 10, 10, 10);
            trash.setImageResource(android.R.drawable.ic_menu_delete);

            addView(trash, new FrameLayout.LayoutParams(abheight, abheight, Gravity.END));
        }
    }

    //endregion public static class Toolbar

    //region public static class InternetState extends BroadcastReceiver

    public static class InternetState extends BroadcastReceiver
    {
        private final static String LOGTAG = InternetState.class.getSimpleName();

        private final static ArrayList<Callback> callbacks = new ArrayList<>();
        private static InternetState instance;

        public static boolean isConnected;
        public static boolean isMobile;
        public static boolean isWifi;

        public static void subscribe(Context context)
        {
            if (instance == null) instance = new InternetState(context);
        }

        public static void unsubscribe(Context context)
        {
            if (instance != null)
            {
                context.unregisterReceiver(instance);

                instance = null;
            }
        }

        public static void subscribe(Callback callback)
        {
            if (! callbacks.contains(callback)) callbacks.add(callback);
        }

        private final ConnectivityManager cm;

        public InternetState(Context context)
        {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver(this, filter);

            onReceive(context, null);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            NetworkInfo actNetwork = cm.getActiveNetworkInfo();

            if (actNetwork == null)
            {
                isConnected = isMobile = isWifi = false;
            }
            else
            {
                isConnected = actNetwork.isConnectedOrConnecting();
                isMobile = (actNetwork.getType() == ConnectivityManager.TYPE_MOBILE);
                isWifi = (actNetwork.getType() == ConnectivityManager.TYPE_WIFI);
            }

            Log.d(LOGTAG, "onReceive:"
                    + " isConnected=" + isConnected
                    + " isMobile=" + isMobile
                    + " isWifi=" + isWifi);

            for (Callback callback : callbacks)
            {
                callback.onInternetChanged();
            }
        }

        public interface Callback
        {
            void onInternetChanged();
        }
    }

    //endregion public static class InternetState extends BroadcastReceiver
}
