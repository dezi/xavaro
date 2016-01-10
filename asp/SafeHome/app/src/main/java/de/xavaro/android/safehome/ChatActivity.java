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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.xavaro.android.common.CommService;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.StaticUtils;

@SuppressWarnings("ResourceType")
public class ChatActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener,
        CommService.CommServiceCallback
{
    private static final String LOGTAG = KioskService.class.getSimpleName();

    private static final int UI_HIDE = View.SYSTEM_UI_FLAG_FULLSCREEN;

    private final Handler handler = new Handler();

    private String idremote;
    private String label;

    private FrameLayout.LayoutParams lp;
    private FrameLayout topscreen;
    private Toolbar toolbar;
    private ImageView schwalbe;
    private EditText input;
    private FrameLayout.LayoutParams scrollviewlp;
    private ScrollView scrollview;
    private LinearLayout scrollcontent;
    private AppCompatActivity context;
    private LinearLayout lastDiv;

    private boolean lastDivIsIncoming;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        toolbar = new Toolbar(this);
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

                if (!(boolean) input.getTag())
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
        super.onPostCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        topscreen.setOnSystemUiVisibilityChangeListener(this);

        CommService.subscribeMessage(this, "sendChatMessage");
        CommService.subscribeMessage(this, "recvChatMessage");
        CommService.subscribeMessage(this, "readChatMessage");

        CommService.subscribeMessage(this, "serverAckMessage");
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

    private View.OnClickListener onSchwalbeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (input.getText().length() == 0) return;

            String message = input.getText().toString();
            input.setText("");

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
            textView.setId(1);
            textView.setPadding(10, 10, 24, 10);
            textView.setTextSize(30f);
            textView.setText(message);

            textLayout.addView(textView);

            lp = new FrameLayout.LayoutParams(38, 28, Gravity.END | Gravity.BOTTOM);

            ImageView statusImage = new ImageView(context);
            statusImage.setId(2);
            statusImage.setLayoutParams(lp);
            statusImage.setPadding(4, 4, 4, 4);

            textLayout.addView(statusImage);

            scrollview.post(new Runnable()
            {
                @Override
                public void run()
                {
                    scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

            try
            {
                String uuid = UUID.randomUUID().toString();

                JSONObject sendChatMessage = new JSONObject();

                sendChatMessage.put("type", "sendChatMessage");
                sendChatMessage.put("idremote", idremote);
                sendChatMessage.put("message", message);
                sendChatMessage.put("uuid", uuid);

                protoOutgoing.put(uuid, new Message(Message.MSG_NEW, sendChatMessage, textLayout));

                CommService.sendEncryptedWithAck(sendChatMessage);

                final JSONObject pm = sendChatMessage;

                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setMessageUnsend(pm);
                    }
                }, 3000);
            }
            catch (JSONException ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }
    };

    private final Map<String, Message> protoIncoming = new HashMap<>();
    private final Map<String, Message> protoOutgoing = new HashMap<>();

    private class Message
    {
        public static final int MSG_NEW = 0;
        public static final int MSG_UNSEND = 1;
        public static final int MSG_RECVSERVER = 2;
        public static final int MSG_RECVCLIENT = 3;
        public static final int MSG_READCLIENT = 4;

        public int status;
        public JSONObject message;
        public FrameLayout textLayout;

        public Message (int status, JSONObject message, FrameLayout textLayout)
        {
            this.status = status;
            this.message = message;
            this.textLayout = textLayout;
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
            super.onLayout(changed, left, top, right, bottom);

            scrollviewlp.bottomMargin = bottom - top;
            scrollview.setLayoutParams(scrollviewlp);
        }
    }

    private void setMessageUnsend(JSONObject sendChatMessage)
    {
        String uuid;

        try
        {
            uuid = sendChatMessage.getString("uuid");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);

            return;
        }

        Message outgoing = protoOutgoing.get(uuid);
        if (outgoing == null) return;

        if (outgoing.status == Message.MSG_NEW)
        {
            outgoing.status = Message.MSG_UNSEND;
            ImageView statusImage = (ImageView) outgoing.textLayout.findViewById(2);
            statusImage.setImageResource(R.drawable.message_unsent);
        }
    }

    private void setMessageAcks(JSONObject serverAckMessage)
    {
        String uuid;

        try
        {
            uuid = serverAckMessage.getString("uuid");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);

            return;
        }

        Message outgoing = protoOutgoing.get(uuid);
        if (outgoing == null) return;

        outgoing.status = Message.MSG_RECVSERVER;
        ImageView statusImage = (ImageView) outgoing.textLayout.findViewById(2);
        statusImage.setImageResource(R.drawable.message_got_receipt_from_server);
    }

    private void setMessageRecv(JSONObject recvChatMessage)
    {
        String uuid;

        try
        {
            uuid = recvChatMessage.getString("uuid");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);

            return;
        }

        Message outgoing = protoOutgoing.get(uuid);
        if (outgoing == null) return;

        outgoing.status = Message.MSG_RECVCLIENT;
        ImageView statusImage = (ImageView) outgoing.textLayout.findViewById(2);
        statusImage.setImageResource(R.drawable.message_got_receipt_from_target);
    }

    private void setMessageRead(JSONObject readChatMessage)
    {
        String uuid;

        try
        {
            uuid = readChatMessage.getString("uuid");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);

            return;
        }

        Message outgoing = protoOutgoing.get(uuid);
        if (outgoing == null) return;

        outgoing.status = Message.MSG_READCLIENT;
        ImageView statusImage = (ImageView) outgoing.textLayout.findViewById(2);
        statusImage.setImageResource(R.drawable.message_got_read_receipt_from_target);
    }

    private void displayMessage(JSONObject sendChatMessage)
    {
        String uuid;
        String message;

        try
        {
            uuid = sendChatMessage.getString("uuid");
            message = sendChatMessage.getString("message");
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);

            return;
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
        textView.setPadding(10, 10, 10, 10);
        textView.setTextSize(30f);
        textView.setText(message);

        textLayout.addView(textView);

        scrollview.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        protoIncoming.put(uuid, new Message(Message.MSG_NEW, sendChatMessage, textLayout));
    }

    public void onMessageReceived(JSONObject message)
    {
        try
        {
            if (message.has("type"))
            {
                String type = message.getString("type");

                if (type.equals("sendChatMessage"))
                {
                    String uuid = message.getString("uuid");
                    String idremote = message.getString("identity");

                    final JSONObject pm = message;

                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            displayMessage(pm);
                        }
                    });

                    JSONObject readChatMessage = new JSONObject();

                    readChatMessage.put("type", "readChatMessage");
                    readChatMessage.put("idremote", idremote);
                    readChatMessage.put("uuid", uuid);

                    CommService.sendEncrypted(readChatMessage);

                    return;
                }

                if (type.equals("serverAckMessage"))
                {
                    final JSONObject pm = message;

                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMessageAcks(pm);
                        }
                    });

                    return;
                }

                if (type.equals("recvChatMessage"))
                {
                    final JSONObject pm = message;

                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMessageRecv(pm);
                        }
                    });

                    return;
                }

                if (type.equals("readChatMessage"))
                {
                    final JSONObject pm = message;

                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMessageRead(pm);
                        }
                    });

                    return;
                }

                if (type.equals("serverAckMessage"))
                {
                    final JSONObject pm = message;

                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setMessageAcks(pm);
                        }
                    });

                    return;
                }
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        Log.d(LOGTAG, "onMessageReceived: " + message.toString());
    }
}
