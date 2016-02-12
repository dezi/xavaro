package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.xavaro.android.common.Json;
import de.xavaro.android.common.RemoteContacts;
import de.xavaro.android.common.Simple;

@SuppressWarnings("ResourceType")
public class ChatDialog extends ScrollView
{
    private static final String LOGTAG = ChatDialog.class.getSimpleName();

    public ChatDialog(Context context)
    {
        this(context, null, 0);
    }

    public ChatDialog(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ChatDialog(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        initialize();
    }

    private final Map<String, FrameLayout> protoIncoming = new HashMap<>();
    private final Map<String, FrameLayout> protoOutgoing = new HashMap<>();

    private final Handler handler = new Handler();

    private LinearLayout scrollcontent;
    private LinearLayout lastDiv;

    private FrameLayout.LayoutParams lp;
    private FrameLayout lastTextLayout;
    private boolean lastDivIsIncoming;
    private String lastMessagePriority;
    private String incomingLastDate;
    private String incomingLastIdent;

    private boolean isuser;
    private boolean isgroup;
    private boolean istoday;

    private final static int ID_TEXT = 1;
    private final static int ID_STATUS = 2;
    private final static int ID_TIME = 3;
    private final static int ID_USER = 4;

    private void initialize()
    {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Simple.MP, Simple.MP);
        lp.setMargins(0, 16, 0, 16);

        setLayoutParams(lp);

        scrollcontent = new LinearLayout(getContext());
        scrollcontent.setOrientation(LinearLayout.VERTICAL);
        scrollcontent.setPadding(16, 0, 16, 0);

        addView(scrollcontent);
    }

    public void setIsUser(boolean what)
    {
        isuser = what;
    }

    public void setIsGroup(boolean what)
    {
        isgroup = what;
    }

    public void setIsToday(boolean what)
    {
        istoday = what;
    }

    public void clearContent()
    {
        scrollcontent.removeAllViews();
        protoIncoming.clear();
        protoOutgoing.clear();
        lastDiv = null;
    }

    public void createIncomingMessage(JSONObject chatMessage)
    {
        String uuid = Json.getString(chatMessage, "uuid");
        String date = Json.getString(chatMessage, "date");
        String message = Json.getString(chatMessage, "message");
        String identity = Json.getString(chatMessage, "identity");
        String priority = Json.getString(chatMessage, "priority");

        if ((incomingLastDate == null) || (Simple.compareTo(incomingLastDate, date) < 0))
        {
            incomingLastDate = date;
        }

        if ((lastDiv == null) || !lastDivIsIncoming)
        {
            lastDiv = new LinearLayout(getContext());
            lastDiv.setOrientation(LinearLayout.VERTICAL);
            lastDiv.setLayoutParams(Simple.layoutParamsMW());

            lastDivIsIncoming = true;
            incomingLastIdent = null;
            lastTextLayout = null;

            scrollcontent.addView(lastDiv);
        }

        FrameLayout textDiv = new FrameLayout(getContext());
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

        FrameLayout textLayout = lastTextLayout = new FrameLayout(getContext());
        textLayout.setLayoutParams(Simple.layoutParamsWW(Gravity.START));
        textLayout.setBackgroundResource(bi);
        lastMessagePriority = priority;

        textDiv.addView(textLayout);

        if (isgroup && ! Simple.equals(identity, incomingLastIdent))
        {
            TextView remoteUser = new TextView(getContext());
            remoteUser.setId(ID_USER);
            remoteUser.setPadding(12, 0, 12, 0);
            remoteUser.setTypeface(null, Typeface.BOLD);
            remoteUser.setText(getRemoteName(chatMessage));
            remoteUser.setTextColor(0xff550390);
            remoteUser.setLayoutParams(Simple.layoutParamsWW(Gravity.START | Gravity.TOP));

            textLayout.addView(remoteUser);
        }

        TextView textView = new TextView(getContext());
        textView.setPadding(10, 10, 10, 16);
        textView.setTextSize(30f);
        textView.setText(message);

        textLayout.addView(textView);

        TextView statusTime = new TextView(getContext());
        statusTime.setId(ID_TIME);
        statusTime.setPadding(12, 0, 12, 0);
        statusTime.setText(Simple.getLocal24HTimeFromISO(date));
        statusTime.setLayoutParams(Simple.layoutParamsWW(Gravity.END | Gravity.BOTTOM));

        textLayout.addView(statusTime);

        incomingLastIdent = identity;

        protoIncoming.put(uuid, textLayout);
    }

    public void createOutgoingMessage(JSONObject chatMessage)
    {
        String uuid = Json.getString(chatMessage, "uuid");
        String date = Json.getString(chatMessage, "date");
        String message = Json.getString(chatMessage, "message");

        if (date == null) date = "??.??";

        if ((lastDiv == null) || lastDivIsIncoming)
        {
            lastDiv = new LinearLayout(getContext());
            lastDiv.setOrientation(LinearLayout.VERTICAL);
            lastDiv.setLayoutParams(Simple.layoutParamsMW());

            lastDivIsIncoming = false;
            lastTextLayout = null;

            scrollcontent.addView(lastDiv);
        }

        FrameLayout textDiv = new FrameLayout(getContext());
        textDiv.setLayoutParams(Simple.layoutParamsMW());
        lastDiv.addView(textDiv);

        if (lastTextLayout != null)
        {
            lastTextLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal_ext);
        }

        FrameLayout textLayout = lastTextLayout = new FrameLayout(getContext());
        textLayout.setLayoutParams(Simple.layoutParamsWW(Gravity.END));
        textLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal);

        textDiv.addView(textLayout);

        TextView textView = new TextView(getContext());
        textView.setId(ID_TEXT);
        textView.setPadding(10, 10, 24, 16);
        textView.setTextSize(30f);
        textView.setText(message);

        textLayout.addView(textView);

        lp = new FrameLayout.LayoutParams(76, 28, Gravity.END | Gravity.BOTTOM);
        FrameLayout statusFrame = new FrameLayout(getContext());
        statusFrame.setLayoutParams(lp);

        textLayout.addView(statusFrame);

        TextView statusTime = new TextView(getContext());
        statusTime.setLayoutParams(Simple.layoutParamsWW(Gravity.START | Gravity.BOTTOM));
        statusTime.setId(ID_TIME);
        statusTime.setText(Simple.getLocal24HTimeFromISO(date));

        statusFrame.addView(statusTime);

        lp = new FrameLayout.LayoutParams(38,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END | Gravity.BOTTOM);

        ImageView statusImage = new ImageView(getContext());
        statusImage.setId(ID_STATUS);
        statusImage.setPadding(4, 4, 4, 4);

        if (chatMessage.has("acks")) statusImage.setImageResource(R.drawable.message_got_receipt_from_server);
        if (chatMessage.has("recv")) statusImage.setImageResource(R.drawable.message_got_receipt_from_target);
        if (chatMessage.has("read")) statusImage.setImageResource(R.drawable.message_got_read_receipt_from_target);

        statusFrame.addView(statusImage, lp);

        protoOutgoing.put(uuid, textLayout);
    }

    private String getRemoteName(JSONObject chatMessage)
    {
        String identity = Json.getString(chatMessage, "identity");
        return RemoteContacts.getDisplayName(identity);
    }

    public void setMessageUnsend(String uuid)
    {
        FrameLayout textLayout = protoOutgoing.get(uuid);
        if (textLayout == null) return;

        ImageView statusImage = (ImageView) textLayout.findViewById(ID_STATUS);
        if (statusImage == null) return;

        statusImage.setImageResource(R.drawable.message_unsent);
    }

    private Runnable setMessageUnsendRunnable;

    public void setMessageUnsendPost(String uuid)
    {
        final String runuuid = uuid;

        setMessageUnsendRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                setMessageUnsend(runuuid);
            }
        };

        handler.postDelayed(setMessageUnsendRunnable, 3000);
    }

    public void onSetMessageStatus(String idremote, String uuid, String what)
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

    public String getIncomingLastDate()
    {
        return incomingLastDate;
    }
}
