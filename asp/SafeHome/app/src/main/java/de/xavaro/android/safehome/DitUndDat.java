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

    //region public static class ImageAntiAliasView extends ImageView

    public static class ImageAntiAliasView extends ImageView
    {
        public ImageAntiAliasView(Context context)
        {
            super(context);
        }

        @Override
        @SuppressLint("DrawAllocation")
        protected void onLayout(boolean changed, int left, int top, int right, int bottom)
        {
            super.onLayout(changed, left, top, right, bottom);

            if (getDrawable() instanceof  BitmapDrawable)
            {
                Bitmap orig = ((BitmapDrawable) getDrawable()).getBitmap();
                Bitmap anti = StaticUtils.downscaleAntiAliasBitmap(orig, right - left, bottom - top);

                setImageDrawable(new BitmapDrawable(getResources(), anti));
            }
        }
    }

    //endregion public static class ImageAntiAliasView extends ImageView

    //region public static class VideoQuality

    public static class VideoQuality
    {
        public static final int LQ = 0x01;
        public static final int SD = 0x02;
        public static final int HQ = 0x04;
        public static final int HD = 0x08;

        public static int deriveQuality(int scanlines)
        {
            if (scanlines <= 270) return LQ;
            if (scanlines <= 480) return SD;
            if (scanlines <= 576) return HQ;

            return HD;
        }
    }

    //endregion public static class VideoQuality

    //region public static class StreamOptions

    public static class StreamOptions
    {
        public String streamUrl;

        public int quality;
        public int bandWidth;

        public int width;
        public int height;
    }

    //endregion public static class StreamOptions

    //region public static class Animator extends Animation

    public static class Animator extends Animation
    {
        private final String LOGTAG = Animator.class.getSimpleName();

        private final ArrayList<Object> steps = new ArrayList<>();

        private Runnable finalCall;
        private boolean finalized;
        private boolean excessive;

        public void setLayout(FrameLayout view, LayoutParams from, LayoutParams toto)
        {
            StepLayout step = new StepLayout();

            step.view = view;
            step.from = from;
            step.toto = toto;

            steps.add(step);

            work = new LayoutParams(0,0);
        }

        public void setColor(FrameLayout view, int from, int toto)
        {
            StepColor step = new StepColor();

            step.view = view;
            step.from = from;
            step.toto = toto;

            steps.add(step);
        }

        public void setFinalCall(Runnable call)
        {
            finalCall = call;
        }

        @SuppressWarnings("unused")
        public void setExcessiveLog(boolean dodat)
        {
            excessive = dodat;
        }

        @Override
        protected void applyTransformation(float it, Transformation t)
        {
            if (finalized) return;

            if (steps.size() == 0) return;

            double div = 1.0 / steps.size();

            int mod = (int) (it / div);

            //
            // Check for overshoot due to rounding.
            //

            if (mod >= steps.size()) mod = steps.size() - 1;

            if (excessive) Log.d(LOGTAG,"applyTransformation:" + mod + "=" + it);

            float scaledit = (float) ((it - (mod * div)) / div);

            for (int inx = 0; inx <= mod; inx++)
            {
                Object step = steps.get(inx);

                if (step instanceof StepLayout) applyStepLayout((inx < mod) ? 1.0f : scaledit, (StepLayout) step);
                if (step instanceof StepColor) applyStepColor  ((inx < mod) ? 1.0f : scaledit, (StepColor)  step);
            }

            if (it >= 1.0f)
            {
                if (finalCall != null) finalCall.run();

                finalized = true;
            }
        }

        private LayoutParams work;

        private void applyStepLayout(float it,StepLayout step)
        {
            if (step.fini) return;

            if (excessive) Log.d(LOGTAG,"applyStepLayout:" + it);

            LayoutParams from = step.from;
            LayoutParams toto = step.toto;

            // @formatter:off
            int width  = from.width      + Math.round(it * (toto.width      - from.width));
            int height = from.height     + Math.round(it * (toto.height     - from.height));
            int left   = from.leftMargin + Math.round(it * (toto.leftMargin - from.leftMargin));
            int top    = from.topMargin  + Math.round(it * (toto.topMargin  - from.topMargin));
            // @formatter:on

            if ((work.width != width) || (work.height != height) || (work.leftMargin != left) || (work.topMargin != top))
            {
                work.width = width;
                work.height = height;
                work.leftMargin = left;
                work.topMargin = top;

                step.view.setLayoutParams(work);
            }

            step.fini = (it >= 1.0f);
        }

        @SuppressWarnings("PointlessBitwiseExpression")
        private void applyStepColor(float it,StepColor step)
        {
            if (step.fini) return;

            if (excessive) Log.d(LOGTAG,"applyStepColor:" + it);

            int from = step.from;
            int toto = step.toto;

            int af = (from >> 24) & 0xff;
            int rf = (from >> 16) & 0xff;
            int gf = (from >>  8) & 0xff;
            int bf = (from >>  0) & 0xff;

            int at = (toto >> 24) & 0xff;
            int rt = (toto >> 16) & 0xff;
            int gt = (toto >>  8) & 0xff;
            int bt = (toto >>  0) & 0xff;

            af += Math.round(it * (at - af));
            rf += Math.round(it * (rt - rf));
            gf += Math.round(it * (gt - gf));
            bf += Math.round(it * (bt - bf));

            if (af > 255) af = 255;
            if (rf > 255) rf = 255;
            if (gf > 255) gf = 255;
            if (bf > 255) bf = 255;

            step.view.setBackgroundColor(Color.argb(af,rf,gf,bf));

            step.fini = (it >= 1.0f);
        }

        @Override
        public boolean willChangeBounds()
        {
            return true;
        }

        private class StepLayout
        {
            public FrameLayout view;
            public LayoutParams from;
            public LayoutParams toto;

            public boolean fini;
        }

        private class StepColor
        {
            public FrameLayout view;
            public int from;
            public int toto;

            public boolean fini;
        }
    }

    //endregion public static class Animator extends Animation

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
