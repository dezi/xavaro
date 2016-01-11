package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
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
import android.util.AttributeSet;
import android.util.Log;
import android.os.Handler;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.StaticUtils;

@SuppressWarnings("ResourceType")
public class ChatActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener,
        ChatManager.MessageCallback
{
    private static final String LOGTAG = ChatActivity.class.getSimpleName();

    private final int UI_HIDE = View.SYSTEM_UI_FLAG_FULLSCREEN;
    private final Map<String, FrameLayout> protoIncoming = new HashMap<>();
    private final Map<String, FrameLayout> protoOutgoing = new HashMap<>();
    private final Handler handler = new Handler();

    private String idremote;
    private String label;

    private FrameLayout.LayoutParams lp;
    private FrameLayout topscreen;
    private DitUndDat.Toolbar toolbar;
    private ImageView schwalbe;
    private EditText input;
    private FrameLayout.LayoutParams scrollviewlp;
    private ScrollView scrollview;
    private LinearLayout scrollcontent;
    private AppCompatActivity context;
    private LinearLayout lastDiv;

    private boolean lastDivIsIncoming;
    private String incomingLastDate;

    private final static int ID_TEXT = 1;
    private final static int ID_STATUS = 2;
    private final static int ID_TIME = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate(savedInstanceState);

        context = this;

        idremote = getIntent().getStringExtra("idremote");
        label = getIntent().getStringExtra("label");

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.chat_bg_2);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        topscreen = new FrameLayout(this);
        topscreen.setSystemUiVisibility(topscreen.getSystemUiVisibility() + UI_HIDE);
        topscreen.setBackgroundColor(GlobalConfigs.ChatActivityBackgroundColor);
        topscreen.setBackground(bitmapDrawable);

        setContentView(topscreen);

        lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                StaticUtils.getActionBarHeight(this));

        toolbar = new DitUndDat.Toolbar(this);

        toolbar.title.setText(label);
        toolbar.icon.setImageResource(R.drawable.communication_400x400);
        toolbar.trash.setOnClickListener(onTrashClick);

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

        schwalbe.setOnClickListener(onSchwalbeClick);

        topscreen.addView(schwalbe);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onPostCreate...");

        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        topscreen.setOnSystemUiVisibilityChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume...");

        super.onResume();

        ChatManager.getInstance(context).subscribe(idremote, this);
    }

    @Override
    protected void onStop()
    {
        Log.d(LOGTAG, "onStop...");

        super.onStop();

        ChatManager.getInstance(context).unsubscribe(idremote, this);
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

    private void createIncomingMessage(JSONObject chatMessage)
    {
        try
        {
            String uuid = chatMessage.getString("uuid");
            String message = chatMessage.getString("message");
            String date = chatMessage.getString("date");

            if ((incomingLastDate == null) || (incomingLastDate.compareTo(date) < 0))
            {
                incomingLastDate = date;
            }

            if ((lastDiv == null) || ! lastDivIsIncoming)
            {
                lastDiv = new LinearLayout(context);
                lastDiv.setOrientation(LinearLayout.VERTICAL);
                lastDiv.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                lastDivIsIncoming = true;

                scrollcontent.addView(lastDiv);
            }

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            FrameLayout textDiv = new FrameLayout(context);
            textDiv.setLayoutParams(lp);
            lastDiv.addView(textDiv);

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.START);

            FrameLayout textLayout = new FrameLayout(context);
            textLayout.setLayoutParams(lp);
            textLayout.setBackgroundResource(R.drawable.balloon_incoming_normal);

            textDiv.addView(textLayout);

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView textView = new TextView(context);
            textView.setPadding(10, 10, 10, 16);
            textView.setTextSize(30f);
            textView.setText(message);

            textLayout.addView(textView);

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END | Gravity.BOTTOM);

            TextView statusTime = new TextView(context);
            statusTime.setId(ID_TIME);
            statusTime.setPadding(0, 0, 12, 0);
            statusTime.setText(Simple.getLocal24HTimeFromISO(date));

            textLayout.addView(statusTime,lp);

            protoIncoming.put(uuid, textLayout);
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private String createOutgoingMessage(JSONObject chatMessage)
    {
        try
        {
            String uuid = chatMessage.has("uuid") ? chatMessage.getString("uuid") : null;
            String message = chatMessage.has("message") ? chatMessage.getString("message") : null;
            String date = chatMessage.has("date") ? chatMessage.getString("date") : null;

            if (date == null) date = Simple.nowAsISO();

            if ((lastDiv == null) || lastDivIsIncoming)
            {
                lastDiv = new LinearLayout(context);
                lastDiv.setOrientation(LinearLayout.VERTICAL);
                lastDiv.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                lastDivIsIncoming = false;

                scrollcontent.addView(lastDiv);
            }

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            FrameLayout textDiv = new FrameLayout(context);
            textDiv.setLayoutParams(lp);
            lastDiv.addView(textDiv);

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END);

            FrameLayout textLayout = new FrameLayout(context);
            textLayout.setLayoutParams(lp);
            textLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal);

            textDiv.addView(textLayout);

            TextView textView = new TextView(context);
            textView.setId(ID_TEXT);
            textView.setPadding(10, 10, 24, 16);
            textView.setTextSize(30f);
            textView.setText(message);

            textLayout.addView(textView);

            lp = new FrameLayout.LayoutParams(76, 28, Gravity.END | Gravity.BOTTOM);
            FrameLayout statusFrame = new FrameLayout(context);
            statusFrame.setLayoutParams(lp);

            textLayout.addView(statusFrame);

            lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.START | Gravity.BOTTOM);

            TextView statusTime = new TextView(context);
            statusTime.setId(ID_TIME);
            statusTime.setText(Simple.getLocal24HTimeFromISO(date));

            statusFrame.addView(statusTime, lp);

            lp = new FrameLayout.LayoutParams(38,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.END | Gravity.BOTTOM);

            ImageView statusImage = new ImageView(context);
            statusImage.setId(ID_STATUS);
            statusImage.setPadding(4, 4, 4, 4);

            if (chatMessage.has("date")) statusImage.setImageResource(R.drawable.message_unsent);
            if (chatMessage.has("acks")) statusImage.setImageResource(R.drawable.message_got_receipt_from_server);
            if (chatMessage.has("recv")) statusImage.setImageResource(R.drawable.message_got_receipt_from_target);
            if (chatMessage.has("read")) statusImage.setImageResource(R.drawable.message_got_read_receipt_from_target);

            statusFrame.addView(statusImage, lp);

            if (uuid == null)
            {
                scrollview.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

                uuid = ChatManager.getInstance(context).sendOutgoingMessage(idremote, message);
            }

            protoOutgoing.put(uuid, textLayout);

            return uuid;
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    private View.OnClickListener onTrashClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            ChatManager.getInstance(context).clearProtocoll(idremote);
        }
    };

    private View.OnClickListener onSchwalbeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                if (! (boolean) input.getTag()) return;
                if (input.getText().length() == 0) return;

                String message = input.getText().toString();
                input.setText("");

                JSONObject chatMessage = new JSONObject();
                chatMessage.put("message", message);

                final String uuid = createOutgoingMessage(chatMessage);

                setMessageUnsendRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setMessageUnsend(uuid);
                    }
                };

                handler.postDelayed(setMessageUnsendRunnable, 3000);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    };

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
            super.onLayout(changed, left, top, right, bottom);

            scrollviewlp.bottomMargin = bottom - top;
            scrollview.setLayoutParams(scrollviewlp);
        }
    }

    private Runnable setMessageUnsendRunnable;

    private void setMessageUnsend(String uuid)
    {
        FrameLayout textLayout = protoOutgoing.get(uuid);
        if (textLayout == null) return;

        ImageView statusImage = (ImageView) textLayout.findViewById(ID_STATUS);
        if (statusImage == null) return;

        statusImage.setImageResource(R.drawable.message_unsent);
    }

    public void onProtocollMessages(JSONObject protocoll)
    {
        Log.d(LOGTAG, "onProtocollMessages: " + protocoll.toString());

        try
        {
            JSONObject outgoing = protocoll.has("outgoing") ? protocoll.getJSONObject("outgoing") : null;
            JSONObject incoming = protocoll.has("incoming") ? protocoll.getJSONObject("incoming") : null;

            Iterator<String> outgoingIter = (outgoing != null) ? outgoing.keys() : null;
            Iterator<String> incomingIter = (incoming != null) ? incoming.keys() : null;

            String outgoingUuid = null;
            String incomingUuid = null;

            while (true)
            {
                if ((outgoingUuid == null) && (outgoingIter != null) && outgoingIter.hasNext())
                {
                    outgoingUuid = outgoingIter.next();
                }

                if ((incomingUuid == null) && (incomingIter != null) && incomingIter.hasNext())
                {
                    incomingUuid = incomingIter.next();
                }

                if ((outgoingUuid == null) && (incomingUuid == null)) break;

                boolean nextOutgoing = true;
                boolean nextIncoming = true;

                if ((outgoingUuid != null) && (incomingUuid != null))
                {
                    String outgoingDate = outgoing.getJSONObject(outgoingUuid).getString("date");
                    String incomingDate = incoming.getJSONObject(incomingUuid).getString("date");

                    if (outgoingDate.compareTo(incomingDate) > 0)
                    {
                        nextOutgoing = false;
                    }
                    else
                    {
                        nextIncoming = false;
                    }
                }

                if (nextOutgoing && (outgoingUuid != null))
                {
                    JSONObject temp = Simple.JSONClone(outgoing.getJSONObject(outgoingUuid));
                    Simple.JSONput(temp, "uuid", outgoingUuid);

                    createOutgoingMessage(temp);

                    outgoingUuid = null;
                }

                if (nextIncoming && (incomingUuid != null))
                {
                    JSONObject temp = Simple.JSONClone(incoming.getJSONObject(incomingUuid));
                    Simple.JSONput(temp, "uuid", incomingUuid);

                    createIncomingMessage(temp);

                    incomingUuid = null;
                }
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        scrollview.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        displayLastOnline();
    }

    private void displayLastOnline()
    {
        if (incomingLastDate == null) return;

        int secsago = Simple.getSecondsAgoFromISO(incomingLastDate);

        if (secsago < 60)
        {
            toolbar.subtitle.setText("Online");

            return;
        }

        int daysago = Simple.getDaysAgoFromISO(incomingLastDate);

        if (daysago == 0)
        {
            toolbar.subtitle.setText("zul. online heute um " + Simple.getLocal24HTimeFromISO(incomingLastDate));

            return;
        }

        if (daysago == 1)
        {
            toolbar.subtitle.setText("zul. online gestern um " + Simple.getLocal24HTimeFromISO(incomingLastDate));

            return;
        }

        toolbar.subtitle.setText("zul. online am "
                + Simple.getLocalDateFromISO(incomingLastDate)
                + " um "
                + Simple.getLocal24HTimeFromISO(incomingLastDate));
    }

    public void onIncomingMessage(JSONObject sendChatMessage)
    {
        createIncomingMessage(sendChatMessage);

        scrollview.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        displayLastOnline();
    }

    public void onSetMessageStatus(String uuid, String what)
    {
        Log.d(LOGTAG,"onSetMessageStatus: " + uuid + "=" + what);

        FrameLayout textLayout = protoOutgoing.get(uuid);
        if (textLayout == null) return;

        ImageView statusImage = (ImageView) textLayout.findViewById(2);
        if (statusImage == null) return;

        handler.removeCallbacks(setMessageUnsendRunnable);

        if (what.equals("acks")) statusImage.setImageResource(R.drawable.message_got_receipt_from_server);
        if (what.equals("recv")) statusImage.setImageResource(R.drawable.message_got_receipt_from_target);
        if (what.equals("read")) statusImage.setImageResource(R.drawable.message_got_read_receipt_from_target);
    }
}
