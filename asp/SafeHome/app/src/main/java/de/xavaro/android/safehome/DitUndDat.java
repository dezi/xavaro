package de.xavaro.android.safehome;


import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.graphics.Color;

import java.util.ArrayList;

//
// Utility namespace for included small classes.
//
public class DitUndDat
{
    private static final String LOGTAG = DitUndDat.class.getSimpleName();

    //region public class Animator extends Animation

    public static class Animator extends Animation
    {
        private final ArrayList<Object> steps = new ArrayList<>();

        private Runnable finalCall;
        private boolean finalized;

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

            Log.d(LOGTAG,"applyTransformation:" + mod + "=" + it);

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

            Log.d(LOGTAG,"applyStepLayout:" + it);

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

            Log.d(LOGTAG,"applyStepColor:" + it);

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

    //endregion
}
