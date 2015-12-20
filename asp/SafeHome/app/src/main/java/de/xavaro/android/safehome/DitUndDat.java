package de.xavaro.android.safehome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telecom.Call;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

//
// Utility namespace for included small classes.
//
public class DitUndDat
{
    //region public static class VideoQuality

    public static class VideoQuality
    {
        public static final int LQ = 0;
        public static final int SD = 1;
        public static final int HQ = 2;
        public static final int HD = 3;
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

        public static void initialize(Context context)
        {
            if (instance == null) instance = new InternetState(context);
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
                isConnected = (actNetwork != null) && actNetwork.isConnectedOrConnecting();
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
