package de.xavaro.android.safehome;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import de.xavaro.android.common.StaticUtils;

public class ChatActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener
{
    private static final String LOGTAG = KioskService.class.getSimpleName();

    private static final int UI_HIDE = View.SYSTEM_UI_FLAG_FULLSCREEN;

    private final Handler handler = new Handler();

    private FrameLayout.LayoutParams lp;
    private FrameLayout topscreen;
    private Toolbar toolbar;
    private ImageView schwalbe;
    private EditText input;
    private FrameLayout.LayoutParams scrollviewlp;
    private ScrollView scrollview;
    private LinearLayout scrollcontent;
    private AppCompatActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        context = this;

        String identity = getIntent().getStringExtra("identity");
        String subtype = getIntent().getStringExtra("subtype");
        String label = getIntent().getStringExtra("label");

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.chat_bg_2);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),bmp);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        topscreen = new FrameLayout(this);
        topscreen.setSystemUiVisibility(topscreen.getSystemUiVisibility() + UI_HIDE);
        topscreen.setBackground(bitmapDrawable);

        setContentView(topscreen);

        lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                StaticUtils.getActionBarHeight(this));

        toolbar = (Toolbar) new Toolbar(this);
        toolbar.setBackgroundColor(0xffff3456);
        toolbar.setTitle(label);
        toolbar.setLayoutParams(lp);

        topscreen.addView(toolbar);

        scrollviewlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        scrollviewlp.setMargins(0, StaticUtils.getActionBarHeight(this) + 8, 0, 0);

        scrollview = new ScrollView(this);
        scrollview.setLayoutParams(scrollviewlp);

        topscreen.addView(scrollview);

        lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16, 0, 16, 0);

        scrollcontent = new LinearLayout(this);
        scrollcontent.setOrientation(LinearLayout.VERTICAL);
        scrollcontent.setLayoutParams(lp);

        scrollview.addView(scrollcontent);

        lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);

        MyFrameLayout inputframe = new MyFrameLayout(this);
        inputframe.setLayoutParams(lp);
        //inputframe.setBackgroundColor(0x30303030);

        topscreen.addView(inputframe);

        lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8, 8, 82, 8);

        input = new EditText(this);
        input.setBackgroundColor(0xffffffff);
        input.setLayoutParams(lp);
        input.setPadding(16, 12, 16, 12);
        input.setFocusable(false);
        input.setTextSize(30f);
        input.setText("Nachricht schreiben");
        input.setTextColor(0x33333333);
        input.setTag(false);

        input.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                Log.d(LOGTAG, "onTouch");

                if (! (boolean) input.getTag())
                {
                    input.setFocusable(true);
                    input.setFocusableInTouchMode(true);
                    input.setText("");
                    input.setTextColor(0xff000000);
                    input.setTag(true);
                }

                return false;
            }
        });

        inputframe.addView(input);

        lp = new FrameLayout.LayoutParams(
                StaticUtils.getActionBarHeight(this),
                StaticUtils.getActionBarHeight(this),
                Gravity.END + Gravity.BOTTOM);
        lp.setMargins(8, 8, 8, 8);

        schwalbe = new ImageView(this);
        schwalbe.setLayoutParams(lp);
        schwalbe.setImageResource(R.drawable.sendmessage_430x430);

        topscreen.addView(schwalbe);

        for (int inx = 0; inx < 100; inx++)
        {

            FrameLayout ldiv = new FrameLayout(this);
            ldiv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            lp.gravity = (inx % 2 == 0) ? Gravity.START : Gravity.END;

            FrameLayout ltxt = new FrameLayout(this);
            ltxt.setLayoutParams(lp);

            if (inx % 2 == 0)
            {
                ltxt.setBackgroundResource(R.drawable.balloon_incoming_normal);
            }
            else
            {
                ltxt.setBackgroundResource(R.drawable.balloon_outgoing_normal);
            }

            ldiv.addView(ltxt);

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView tv = new TextView(this);
            //tv.setLayoutParams(lp);
            //tv.setBackgroundColor(0x80ffff00);
            tv.setPadding(10, 10, 10, 10);
            tv.setTextSize(30f);
            tv.setText("" + inx);

            ltxt.addView(tv);

            scrollcontent.addView(ldiv);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        topscreen.setOnSystemUiVisibilityChangeListener(this);
    }

    private final Runnable makeFullscreen = new Runnable()
    {
        @Override
        public void run()
        {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.

            topscreen.setSystemUiVisibility(topscreen.getSystemUiVisibility() + UI_HIDE);
        }
    };

    @Override
    public void onSystemUiVisibilityChange(int visibility)
    {
        Log.d(LOGTAG, "onSystemUiVisibilityChange:" + visibility);

        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
        {
            handler.postDelayed(makeFullscreen, 2000);
        }
    }

    private class MyFrameLayout extends FrameLayout
    {
        public MyFrameLayout(Context context)
        {
            super(context);
        }

        public MyFrameLayout(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        public MyFrameLayout(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs, defStyle);
        }

        protected void onLayout(boolean changed, int left, int top, int right, int bottom)
        {
            Log.d("MyFrameLayout", "onLayout:" + changed + "=" + left + "=" + top + "=" + right + "=" + bottom);

            super.onLayout(changed, left, top, right, bottom);

            scrollviewlp.bottomMargin = bottom - top;
            scrollview.setLayoutParams(scrollviewlp);
        }
    }
}
