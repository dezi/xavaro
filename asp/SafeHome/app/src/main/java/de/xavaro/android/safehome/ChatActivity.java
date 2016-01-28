package de.xavaro.android.safehome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.text.InputType;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.SystemIdentity;

@SuppressWarnings("ResourceType")
public class ChatActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener,
        ChatManager.ChatMessageCallback
{
    private static final String LOGTAG = ChatActivity.class.getSimpleName();

    private final int UI_HIDE = View.SYSTEM_UI_FLAG_FULLSCREEN;
    private final Map<String, FrameLayout> protoIncoming = new HashMap<>();
    private final Map<String, FrameLayout> protoOutgoing = new HashMap<>();

    private final Handler handler = new Handler();

    private ChatManager chatManager;
    private boolean isinitialized;
    private boolean isuser;
    private boolean isgroup;
    private String idremote;
    private String groupStatus;
    private String label;

    private FrameLayout.LayoutParams lp;
    private FrameLayout topscreen;
    private DitUndDat.Toolbar toolbar;
    private ImageView schwalbe;
    private EditText input;
    private FrameLayout.LayoutParams scrollviewlp;
    private ScrollView scrollview;
    private LinearLayout scrollcontent;
    private LinearLayout lastDiv;

    private FrameLayout lastTextLayout;
    private boolean lastDivIsIncoming;
    private String lastMessagePriority;
    private String incomingLastDate;
    private String incomingLastIdent;

    private final static int ID_TEXT = 1;
    private final static int ID_STATUS = 2;
    private final static int ID_TIME = 3;
    private final static int ID_USER = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate(savedInstanceState);

        Simple.setAppContext(this);

        chatManager = ChatManager.getInstance();

        idremote = getIntent().getStringExtra("idremote");

        if (RemoteContacts.isContact(idremote))
        {
            label = RemoteContacts.getDisplayName(idremote);
            isuser = true;
        }

        if (RemoteGroups.isGroup(idremote))
        {
            label = RemoteGroups.getDisplayName(idremote);
            isgroup = true;
        }

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.chat_bg_2);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        topscreen = new FrameLayout(this);
        topscreen.setSystemUiVisibility(topscreen.getSystemUiVisibility() + UI_HIDE);
        topscreen.setBackgroundColor(GlobalConfigs.ChatActivityBackgroundColor);
        topscreen.setBackground(bitmapDrawable);

        setContentView(topscreen);

        toolbar = new DitUndDat.Toolbar(this);

        toolbar.title.setText(label);
        toolbar.icon.setImageResource(R.drawable.communication_400x400);
        toolbar.trash.setOnClickListener(onTrashClick);

        topscreen.addView(toolbar);

        scrollviewlp = new FrameLayout.LayoutParams(Simple.MP, Simple.MP);
        scrollviewlp.setMargins(0, Simple.getActionBarHeight() + 8, 0, 0);

        scrollview = new ScrollView(this);
        scrollview.setLayoutParams(scrollviewlp);

        scrollview.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (input.isFocusable())
                {
                    input.setFocusable(false);
                    Simple.dismissKeyboard(input);
                    input.setTag(1);
                }

                return false;
            }
        });

        topscreen.addView(scrollview);

        scrollcontent = new LinearLayout(this);
        scrollcontent.setOrientation(LinearLayout.VERTICAL);
        scrollcontent.setPadding(16, 0, 16, 0);

        scrollview.addView(scrollcontent);

        MyFrameLayout inputframe = new MyFrameLayout(this);
        inputframe.setLayoutParams(Simple.layoutParamsMW(Gravity.BOTTOM));

        topscreen.addView(inputframe);

        lp = new FrameLayout.LayoutParams(Simple.MP,Simple.WC);
        lp.setMargins(8, 8, Simple.getActionBarHeight() + 16, 8);

        input = new EditText(this);
        input.setBackgroundColor(0xffffffff);
        input.setLayoutParams(lp);
        input.setPadding(16, 12, 16, 12);
        input.setFocusable(false);
        input.setTextSize(30f);
        input.setText("Nachricht");
        input.setTextColor(0x33333333);
        input.setTag(0);

        input.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (((int) input.getTag()) < 2)
                {
                    if (((int) input.getTag()) == 0) input.setText("");

                    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    input.setFocusable(true);
                    input.setFocusableInTouchMode(true);
                    input.setTextColor(0xff000000);
                    input.setTag(2);
                }

                return false;
            }
        });

        inputframe.addView(input);

        lp = new FrameLayout.LayoutParams(
                Simple.getActionBarHeight(),
                Simple.getActionBarHeight(),
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
    protected void onStart()
    {
        Log.d(LOGTAG, "onStart...");

        super.onStart();

        chatManager.subscribe(idremote, this);
    }

    @Override
    protected void onStop()
    {
        Log.d(LOGTAG, "onStop...");

        super.onStop();

        chatManager.unsubscribe(idremote, this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.d(LOGTAG, "onWindowFocusChanged=" + hasFocus);

        super.onWindowFocusChanged(hasFocus);

        CommonStatic.setFocused(ChatActivity.class.getSimpleName(), hasFocus);

        if (hasFocus) handler.postDelayed(makeFullscreen, 500);
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

            if (input.hasFocus())
            {
                //
                // Focus with keyboard requires status bar for
                // keyboard selection. Hiding the status bar now
                // yields in a transparent, unreadable status bar.
                //
            }
            else
            {
                topscreen.setSystemUiVisibility(topscreen.getSystemUiVisibility() + UI_HIDE);
            }
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

    private String getRemoteName(JSONObject chatMessage)
    {
        String identity = Json.getString(chatMessage, "identity");
        return RemoteContacts.getDisplayName(identity);
    }

    private void createIncomingMessage(JSONObject chatMessage)
    {
        String uuid = Json.getString(chatMessage, "uuid");
        String date = Json.getString(chatMessage, "date");
        String message = Json.getString(chatMessage, "message");
        String identity = Json.getString(chatMessage, "identity");
        String priority = Json.getString(chatMessage, "priority");

        if ((incomingLastDate == null) || (Simple.compareTo(incomingLastDate,date) < 0))
        {
            incomingLastDate = date;
        }

        if ((lastDiv == null) || !lastDivIsIncoming)
        {
            lastDiv = new LinearLayout(this);
            lastDiv.setOrientation(LinearLayout.VERTICAL);
            lastDiv.setLayoutParams(Simple.layoutParamsMW());

            lastDivIsIncoming = true;
            incomingLastIdent = null;
            lastTextLayout = null;

            scrollcontent.addView(lastDiv);
        }

        FrameLayout textDiv = new FrameLayout(this);
        textDiv.setLayoutParams(Simple.layoutParamsMW());
        lastDiv.addView(textDiv);

        if (lastTextLayout != null)
        {
            int bi = Simple.equals(lastMessagePriority, "alertcall")
                    ? R.drawable.balloon_incoming_alert_ext
                    : R.drawable.balloon_incoming_normal_ext;

            lastTextLayout.setBackgroundResource(bi);
        }

        int bi = Simple.equals(priority, "alertcall")
                ? R.drawable.balloon_incoming_alert
                : R.drawable.balloon_incoming_normal;

        FrameLayout textLayout = lastTextLayout = new FrameLayout(this);
        textLayout.setLayoutParams(Simple.layoutParamsWW(Gravity.START));
        textLayout.setBackgroundResource(bi);
        lastMessagePriority = priority;

        textDiv.addView(textLayout);

        if (isgroup && ! Simple.equals(identity, incomingLastIdent))
        {
            TextView remoteUser = new TextView(this);
            remoteUser.setId(ID_USER);
            remoteUser.setPadding(12, 0, 12, 0);
            remoteUser.setTypeface(null, Typeface.BOLD);
            remoteUser.setText(getRemoteName(chatMessage));
            remoteUser.setTextColor(0xff550390);
            remoteUser.setLayoutParams(Simple.layoutParamsWW(Gravity.START | Gravity.TOP));

            textLayout.addView(remoteUser);
        }

        TextView textView = new TextView(this);
        textView.setPadding(10, 10, 10, 16);
        textView.setTextSize(30f);
        textView.setText(message);

        textLayout.addView(textView);

        TextView statusTime = new TextView(this);
        statusTime.setId(ID_TIME);
        statusTime.setPadding(12, 0, 12, 0);
        statusTime.setText(Simple.getLocal24HTimeFromISO(date));
        statusTime.setLayoutParams(Simple.layoutParamsWW(Gravity.END | Gravity.BOTTOM));

        textLayout.addView(statusTime);

        incomingLastIdent = identity;

        protoIncoming.put(uuid, textLayout);

        if (! chatMessage.has("read"))
        {
            JSONObject feedBack = Json.clone(chatMessage);
            Json.put(feedBack, "idremote", idremote);
            chatManager.sendFeedbackMessage(feedBack, "read");
        }
    }

    private void createOutgoingMessage(JSONObject chatMessage)
    {
        String uuid = Json.getString(chatMessage, "uuid");
        String date = Json.getString(chatMessage, "date");
        String message = Json.getString(chatMessage, "message");

        if ((lastDiv == null) || lastDivIsIncoming)
        {
            lastDiv = new LinearLayout(this);
            lastDiv.setOrientation(LinearLayout.VERTICAL);
            lastDiv.setLayoutParams(Simple.layoutParamsMW());

            lastDivIsIncoming = false;
            lastTextLayout = null;

            scrollcontent.addView(lastDiv);
        }

        FrameLayout textDiv = new FrameLayout(this);
        textDiv.setLayoutParams(Simple.layoutParamsMW());
        lastDiv.addView(textDiv);

        if (lastTextLayout != null)
        {
            lastTextLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal_ext);
        }

        FrameLayout textLayout = lastTextLayout = new FrameLayout(this);
        textLayout.setLayoutParams(Simple.layoutParamsWW(Gravity.END));
        textLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal);

        textDiv.addView(textLayout);

        TextView textView = new TextView(this);
        textView.setId(ID_TEXT);
        textView.setPadding(10, 10, 24, 16);
        textView.setTextSize(30f);
        textView.setText(message);

        textLayout.addView(textView);

        lp = new FrameLayout.LayoutParams(76, 28, Gravity.END | Gravity.BOTTOM);
        FrameLayout statusFrame = new FrameLayout(this);
        statusFrame.setLayoutParams(lp);

        textLayout.addView(statusFrame);

        TextView statusTime = new TextView(this);
        statusTime.setLayoutParams(Simple.layoutParamsWW(Gravity.START | Gravity.BOTTOM));
        statusTime.setId(ID_TIME);
        statusTime.setText(Simple.getLocal24HTimeFromISO(date));

        statusFrame.addView(statusTime);

        lp = new FrameLayout.LayoutParams(38,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END | Gravity.BOTTOM);

        ImageView statusImage = new ImageView(this);
        statusImage.setId(ID_STATUS);
        statusImage.setPadding(4, 4, 4, 4);

        if (chatMessage.has("acks")) statusImage.setImageResource(R.drawable.message_got_receipt_from_server);
        if (chatMessage.has("recv")) statusImage.setImageResource(R.drawable.message_got_receipt_from_target);
        if (chatMessage.has("read")) statusImage.setImageResource(R.drawable.message_got_read_receipt_from_target);

        statusFrame.addView(statusImage, lp);

        protoOutgoing.put(uuid, textLayout);
    }

    private View.OnClickListener onTrashClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            chatManager.clearProtocoll(idremote);

            scrollcontent.removeAllViews();
            protoIncoming.clear();
            protoOutgoing.clear();
            lastDiv = null;
        }
    };

    private View.OnClickListener onSchwalbeClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (((int) input.getTag()) == 0) return;
            if (input.getText().length() == 0) return;

            final String uuid = UUID.randomUUID().toString();
            final String message = input.getText().toString();

            input.setText("");

            JSONObject chatMessage = new JSONObject();
            Json.put(chatMessage, "message", message);
            Json.put(chatMessage, "date", Simple.nowAsISO());
            Json.put(chatMessage, "uuid", uuid);

            createOutgoingMessage(chatMessage);

            scrollDown();

            chatManager.sendOutgoingMessage(idremote, chatMessage);

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

        private int lastTop;
        private int lastBottom;

        private Runnable resizeOnLayout = new Runnable()
        {
            @Override
            public void run()
            {
                scrollviewlp.bottomMargin = lastBottom - lastTop;
                scrollview.setLayoutParams(scrollviewlp);
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        };

        protected void onLayout(boolean changed, int left, int top, int right, int bottom)
        {
            super.onLayout(changed, left, top, right, bottom);

            lastTop = top;
            lastBottom = bottom;

            handler.post(resizeOnLayout);
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

    public void onRemoteStatus()
    {
        handler.post(displayRemoteStatus);
    }

    public void onProtocollMessages(JSONObject protocoll)
    {
        if (isinitialized) return;
        isinitialized = true;

        Log.d(LOGTAG, "onProtocollMessages: " + protocoll.toString());

        JSONObject outgoing = Json.getObject(protocoll, "outgoing");
        JSONObject incoming = Json.getObject(protocoll, "incoming");

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
                String outgoingDate = Json.getString(Json.getObject(outgoing, outgoingUuid), "date");
                String incomingDate = Json.getString(Json.getObject(incoming, incomingUuid), "date");

                if (Simple.compareTo(outgoingDate, incomingDate) > 0)
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
                JSONObject temp = Json.clone(Json.getObject(outgoing,outgoingUuid));
                Json.put(temp, "uuid", outgoingUuid);

                createOutgoingMessage(temp);

                outgoingUuid = null;
            }

            if (nextIncoming && (incomingUuid != null))
            {
                JSONObject temp = Json.clone(Json.getObject(incoming,incomingUuid));
                Json.put(temp, "uuid", incomingUuid);

                createIncomingMessage(temp);

                incomingUuid = null;
            }
        }

        scrollview.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        handler.postDelayed(displayRemoteStatus, 1000);
    }

    private boolean wasInChat;

    private void displayRemoteUserStatus()
    {
        //
        // Check if partner is within chat or not.
        //

        String incomingChat = chatManager.getLastChatStatus(idremote);

        String incomingChatType = null;
        String incomingChatDate = null;

        if (incomingChat != null)
        {
            String[] parts = incomingChat.split("=");

            if (parts.length == 2)
            {
                incomingChatType = parts[ 0 ];
                incomingChatDate = parts[ 1 ];
            }
        }

        String chatstatus = null;

        if (incomingChatDate != null)
        {
            if (incomingChatType.equals("joinchat"))
            {
                chatstatus = "jetzt im Chat";
                wasInChat = true;
            }
            else
            {
                if (wasInChat) chatstatus = "hat den Chat verlassen";
            }
        }

        //
        // Figure out latest last online date.
        //

        String lastOnline = chatManager.getLastOnlineDate(idremote);

        if (incomingLastDate != null)
        {
            if (lastOnline == null)
            {
                lastOnline = incomingLastDate;
            }
            else
            {
                if (incomingLastDate.compareTo(lastOnline) > 0)
                {
                    lastOnline = incomingLastDate;
                }
            }
        }

        //
        // Build online message part.
        //

        String onlinestatus = null;

        if ((lastOnline != null) &&
                ((incomingChatDate == null) || ! incomingChatType.equals("joinchat")))
        {
            int secsago = Simple.getSecondsAgoFromISO(lastOnline);

            if (secsago < 60)
            {
                onlinestatus = "ist online";
            }
            else
            {
                int daysago = Simple.getDaysAgoFromISO(lastOnline);

                if (daysago == 0)
                {
                    onlinestatus = "zul. online heute um "
                            + Simple.getLocal24HTimeFromISO(lastOnline);
                }

                if (daysago == 1)
                {
                    onlinestatus = "zul. online gestern um "
                            + Simple.getLocal24HTimeFromISO(lastOnline);
                }

                if (daysago > 1)
                {
                    onlinestatus = "zul. online am "
                            + Simple.getLocalDateFromISO(lastOnline)
                            + " um "
                            + Simple.getLocal24HTimeFromISO(lastOnline);
                }
            }
        }

        //
        // Assemble display status.
        //

        String status = chatstatus;

        if (onlinestatus != null)
        {
            if (status == null)
            {
                status = onlinestatus;
            }
            else
            {
                status += ", " + onlinestatus;
            }
        }

        if (status == null) status = "Offline";

        toolbar.subtitle.setText(status);
    }

    private void displayRemoteGroupStatus()
    {
        if (groupStatus != null) return;

        JSONObject group = RemoteGroups.getGroup(idremote);

        if ((group == null) || ! group.has("members"))
        {
            groupStatus = "unbekannt";
            toolbar.subtitle.setText(groupStatus);

            return;
        }

        try
        {
            SharedPreferences sp = Simple.getSharedPrefs();
            JSONArray members = group.getJSONArray("members");

            groupStatus = "";

            for (int inx = 0; inx < members.length(); inx++)
            {
                JSONObject member = members.getJSONObject(inx);
                String ident = member.getString("identity");

                if (ident.equals(SystemIdentity.getIdentity()))
                {
                    //
                    // Surprise, we are in the group.
                    //

                    continue;
                }

                String name = "";

                String nickpref = "community.remote." + ident + ".nickname";

                if (sp.contains(nickpref) && ! sp.getString(nickpref, "").equals(""))
                {
                    name = sp.getString(nickpref, "");
                }
                else
                {
                    if (member.has("ownerFirstName")) name += " " + member.get("ownerFirstName");
                    if (member.has("ownerGivenName")) name += " " + member.get("ownerGivenName");
                }

                if (groupStatus.length() > 0) groupStatus+= ", ";
                groupStatus += name.trim();
            }

            if (groupStatus.length() > 0) groupStatus+= ", ";
            groupStatus += "Du";
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        toolbar.subtitle.setText(groupStatus);
    }

    private final Runnable displayRemoteStatus = new Runnable()
    {
        @Override
        public void run()
        {
            if (isuser) displayRemoteUserStatus();
            if (isgroup) displayRemoteGroupStatus();

            handler.postDelayed(displayRemoteStatus, 10000);
        }
    };

    public void onIncomingMessage(JSONObject message)
    {
        createIncomingMessage(message);

        scrollDown();

        handler.post(displayRemoteStatus);
    }

    private void scrollDown()
    {
        scrollview.post(new Runnable()
        {
            @Override
            public void run()
            {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void onSetMessageStatus(String uuid, String what)
    {
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
