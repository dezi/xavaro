package de.xavaro.android.safehome;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.EditText;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.os.Handler;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import de.xavaro.android.common.SocialFacebook;
import de.xavaro.android.common.Json;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.ChatManager;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.OopsService;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.RemoteGroups;
import de.xavaro.android.common.Speak;
import de.xavaro.android.common.SystemIdentity;

@SuppressWarnings("ResourceType")
public class ChatActivity extends AppCompatActivity implements
        View.OnSystemUiVisibilityChangeListener,
        ChatManager.ChatMessageCallback
{
    private static final String LOGTAG = ChatActivity.class.getSimpleName();

    private final int UI_HIDE = View.SYSTEM_UI_FLAG_FULLSCREEN;

    private final Handler handler = new Handler();

    private ChatManager chatManager;
    private boolean isinitialized;
    private boolean isuser;
    private boolean isgroup;
    private boolean isalert;
    private String idremote;
    private String groupStatus;
    private String groupOwner;
    private String groupType;
    private String label;

    private FrameLayout.LayoutParams lp;
    private FrameLayout topscreen;
    private DitUndDat.Toolbar toolbar;
    private ImageView schwalbe;
    private EditText input;
    private FrameLayout.LayoutParams scrollviewlp;
    private ChatDialog scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate...");

        super.onCreate(savedInstanceState);

        Simple.setActContext(this);

        int toolbarIcon = R.drawable.communication_400x400;
        int toolbarColor = 0xff448844;

        chatManager = ChatManager.getInstance();

        idremote = getIntent().getStringExtra("idremote");

        if (RemoteContacts.isContact(idremote))
        {
            label = RemoteContacts.getDisplayName(idremote);
            isuser = true;
        }

        if (RemoteGroups.isGroup(idremote))
        {
            groupOwner = RemoteGroups.getGroupOwner(idremote);
            groupType = RemoteGroups.getGroupType(idremote);

            if (Simple.equals(groupOwner, SystemIdentity.getIdentity()) &&
                    Simple.equals(groupType, "alertcall"))
            {
                label = "Assistenz für Dich";

                toolbarIcon = R.drawable.alertgroup_300x300;
                toolbarColor = 0xff444444;
            }
            else
            {
                label = RemoteGroups.getDisplayName(idremote);

                toolbarIcon = R.drawable.commchatalert_300x300;
                toolbarColor = 0xff888888;
            }

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

        toolbar.setBackgroundColor(toolbarColor);
        toolbar.title.setText(label);
        toolbar.icon.setImageResource(toolbarIcon);
        toolbar.trash.setOnClickListener(onTrashClick);

        topscreen.addView(toolbar);

        scrollviewlp = new FrameLayout.LayoutParams(Simple.MP, Simple.MP);
        scrollviewlp.setMargins(0, Simple.getActionBarHeight() + 8, 0, 0);

        scrollview = new ChatDialog(this);
        scrollview.setIsUser(isuser);
        scrollview.setIsGroup(isgroup);
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
        input.setTextSize(Simple.getDeviceTextSize(30f));
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

        if (getIntent().hasExtra("alertcall"))
        {
            isalert = getIntent().getBooleanExtra("alertcall", false);

            if (! isalert)
            {
                //
                // Tell user how to fire an alert call.
                //

                ArchievementManager.show("alertcall.shortclick");
            }
            else
            {
                alertMessageUUID = getIntent().getStringExtra("alertMessageUUID");
            }
        }
    }

    @Override
    protected void onStart()
    {
        Log.d(LOGTAG, "onStart...");

        super.onStart();

        chatManager.subscribe(idremote, this);
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume...");

        super.onResume();

        Simple.setActContext(this);

        SocialFacebook.logEvent("ChatActivity");
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

    private View.OnClickListener onTrashClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            chatManager.clearProtocoll(idremote);
            scrollview.clearContent();
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

            scrollview.createOutgoingMessage(chatMessage);

            scrollDown();

            chatManager.sendOutgoingMessage(idremote, chatMessage);

            scrollview.setMessageUnsendPost(uuid);
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

                scrollview.createOutgoingMessage(temp);

                outgoingUuid = null;
            }

            if (nextIncoming && (incomingUuid != null))
            {
                JSONObject temp = Json.clone(Json.getObject(incoming,incomingUuid));
                Json.put(temp, "uuid", incomingUuid);

                scrollview.createIncomingMessage(temp);

                if (! temp.has("read"))
                {
                    JSONObject feedBack = Json.clone(temp);
                    Json.put(feedBack, "idremote", idremote);
                    chatManager.sendFeedbackMessage(feedBack, "read");
                }

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
        String lastIncoming = scrollview.getIncomingLastDate();

        if (lastIncoming != null)
        {
            if (lastOnline == null)
            {
                lastOnline = lastIncoming;
            }
            else
            {
                if (lastIncoming.compareTo(lastOnline) > 0)
                {
                    lastOnline = lastIncoming;
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
            int secsago = Simple.getSecondsAgo(lastOnline);

            if (secsago < 60)
            {
                onlinestatus = "ist online";
            }
            else
            {
                int daysago = Simple.getDaysAgo(lastOnline);

                if (daysago == 0)
                {
                    onlinestatus = "zul. online heute um "
                            + Simple.getLocal24HTime(lastOnline);
                }

                if (daysago == 1)
                {
                    onlinestatus = "zul. online gestern um "
                            + Simple.getLocal24HTime(lastOnline);
                }

                if (daysago > 1)
                {
                    onlinestatus = "zul. online am "
                            + Simple.getLocalDate(lastOnline)
                            + " um "
                            + Simple.getLocal24HTime(lastOnline);
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
            groupStatus = "Gruppe hat keine Mitglieder";
            toolbar.subtitle.setText(groupStatus);

            return;
        }

        try
        {
            SharedPreferences sp = Simple.getSharedPrefs();
            JSONArray members = group.getJSONArray("members");

            String grouptype = Json.getString(group, "type");
            String groupowner = Json.getString(group, "owner");

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

                if (Simple.equals(groupType, "alertcall") && Simple.equals(groupowner, ident))
                {
                    //
                    // The goup owner name is already in title of group.
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
        scrollview.createIncomingMessage(message);

        if (! message.has("read"))
        {
            JSONObject feedBack = Json.clone(message);
            Json.put(feedBack, "idremote", idremote);
            chatManager.sendFeedbackMessage(feedBack, "read");
        }

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

    public void onSetMessageStatus(String idremote, String uuid, String what)
    {
        scrollview.onSetMessageStatus(idremote, uuid, what);

        if (isalert) checkAlertMessageStatus(idremote, uuid, what);
    }

    //region Alert call handling

    private final ArrayList<String> idremotesAlertReceived = new ArrayList<>();
    private final ArrayList<String> idremotesAlertRead  = new ArrayList<>();
    private String alertMessageUUID;

    private final Runnable speekFeedbackMessage = new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (speekFeedbackMessage)
            {
                for (String idremote : idremotesAlertReceived)
                {
                    if (!idremotesAlertRead.contains(idremote))
                    {
                        String message = RemoteContacts.getDisplayName(idremote);
                        message += " hat ihren Assistenzruf empfangen";
                        Speak.speak(message);

                        JSONObject chatMessage = new JSONObject();

                        Json.put(chatMessage, "message", message);
                        Json.put(chatMessage, "date", Simple.nowAsISO());
                        Json.put(chatMessage, "uuid", Simple.getUUID());
                        Json.put(chatMessage, "identity", idremote);
                        Json.put(chatMessage, "idremote", ChatActivity.this.idremote);

                        ChatManager.getInstance().fakeIncomingMessage(chatMessage);
                    }
                }

                idremotesAlertReceived.clear();

                for (String idremote : idremotesAlertRead)
                {
                    String message = RemoteContacts.getDisplayName(idremote);
                    message += " hat ihren Assistenzruf gelesen";
                    Speak.speak(message);

                    JSONObject chatMessage = new JSONObject();

                    Json.put(chatMessage, "message", message);
                    Json.put(chatMessage, "date", Simple.nowAsISO());
                    Json.put(chatMessage, "uuid", Simple.getUUID());
                    Json.put(chatMessage, "identity", idremote);
                    Json.put(chatMessage, "idremote", ChatActivity.this.idremote);

                    ChatManager.getInstance().fakeIncomingMessage(chatMessage);
                }

                idremotesAlertRead.clear();
            }
        }
    };

    public void checkAlertMessageStatus(String idremote, String uuid, String what)
    {
        if (! alertMessageUUID.equals(uuid)) return;

        synchronized (speekFeedbackMessage)
        {
            if (what.equals("recv"))
            {
                if (!idremotesAlertReceived.contains(idremote))
                    idremotesAlertReceived.add(idremote);

                handler.removeCallbacks(speekFeedbackMessage);
                handler.postDelayed(speekFeedbackMessage, 3000);
            }

            if (what.equals("read"))
            {
                if (!idremotesAlertReceived.contains(idremote))
                    idremotesAlertReceived.remove(idremote);

                if (!idremotesAlertRead.contains(idremote))
                    idremotesAlertRead.add(idremote);

                handler.removeCallbacks(speekFeedbackMessage);
                handler.postDelayed(speekFeedbackMessage, 3000);
            }
        }
    }

    //endregion Alert call handling
}
