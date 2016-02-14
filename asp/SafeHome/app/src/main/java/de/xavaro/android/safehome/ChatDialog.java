package de.xavaro.android.safehome;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
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
    private String lastDate;
    private String incomingLastDate;
    private String incomingLastIdent;

    private boolean isuser;
    private boolean isgroup;
    private boolean istoday;

    private final static int ID_TEXT = 1;
    private final static int ID_STATUS = 2;
    private final static int ID_TIME = 3;
    private final static int ID_USER = 4;
    private final static int ID_THUMBS = 5;

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

    private void checkDate(String date)
    {
        if ((lastDate == null) || ! Simple.equals(lastDate, Simple.getLocalDateLong(date)))
        {
            lastDate = Simple.getLocalDateLong(date);

            lastDiv = new LinearLayout(getContext());
            lastDiv.setOrientation(LinearLayout.VERTICAL);
            lastDiv.setLayoutParams(Simple.layoutParamsMW());
            scrollcontent.addView(lastDiv);

            FrameLayout textDiv = new FrameLayout(getContext());
            textDiv.setLayoutParams(Simple.layoutParamsMW());
            lastDiv.addView(textDiv);

            TextView textView = new TextView(getContext());
            textView.setLayoutParams(Simple.layoutParamsWW(Gravity.CENTER));
            textView.setBackgroundResource(R.drawable.balloon_outgoing_pressed_ext);
            textView.setPadding(20, 10, 36, 10);
            textView.setTextSize(24f);
            textView.setText(lastDate);

            textDiv.addView(textView);

            lastDiv = null;
        }
    }

    private boolean accumulateMessage(String message, String date)
    {
        if (lastTextLayout == null) return false;

        TextView textView = (TextView) lastTextLayout.findViewById(ID_TEXT);
        TextView timeView = (TextView) lastTextLayout.findViewById(ID_TIME);

        if ((textView == null) || (timeView == null) ||
            ! message.equals(textView.getText())) return false;

        String time = timeView.getText().toString();
        String n24h = Simple.getLocal24HTime(date);
        String sand = Simple.getTrans(R.string.simple_and);

        if (! time.contains(n24h)) time += ", " + n24h;
        time = time.replace(" " + sand + " ", ", ");

        if (time.lastIndexOf(',') > 0)
        {
            time = time.substring(0, time.lastIndexOf(',')) + " " + sand
                    + time.substring(time.lastIndexOf(',') + 1, time.length());
        }

        timeView.setText(time);

        return true;
    }

    public void checkMediaPath(JSONObject chatMessage)
    {
        if (lastTextLayout == null) return;

        String mediapath = Json.getString(chatMessage, "mediapath");
        if (mediapath == null) return;

        int size = 80;

        LinearLayout thumbnails = (LinearLayout) lastTextLayout.findViewById(ID_THUMBS);

        if (thumbnails == null)
        {
            TextView textView = (TextView) lastTextLayout.findViewById(ID_TEXT);

            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textView.getLayoutParams();
            lp.setMargins(0, size - 10, 0, 0);

            thumbnails = new LinearLayout(getContext());
            thumbnails.setOrientation(LinearLayout.HORIZONTAL);
            thumbnails.setLayoutParams(new LinearLayout.LayoutParams(Simple.MP, size));
            thumbnails.setPadding(8, 8, 8, 8);
            thumbnails.setId(ID_THUMBS);

            lastTextLayout.addView(thumbnails);
        }

        ImageView thumbnail = new ImageView(getContext());
        thumbnail.setLayoutParams(new FrameLayout.LayoutParams(size - 16, size - 16));
        thumbnail.setPadding(2, 2, 2, 2);
        thumbnail.setImageDrawable(Simple.getDrawableSquare(mediapath, size - 20));


        thumbnails.addView(thumbnail);
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

        checkDate(date);

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

        if (accumulateMessage(message, date))
        {
            checkMediaPath(chatMessage);

            return;
        }

        FrameLayout textDiv = new FrameLayout(getContext());
        textDiv.setLayoutParams(Simple.layoutParamsMW());
        lastDiv.addView(textDiv);

        if (! accumulateMessage(message, date))

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

        lastTextLayout = new FrameLayout(getContext());
        lastTextLayout.setLayoutParams(Simple.layoutParamsWW(Gravity.START));
        lastTextLayout.setBackgroundResource(bi);
        lastMessagePriority = priority;

        textDiv.addView(lastTextLayout);

        if (isgroup && !Simple.equals(identity, incomingLastIdent))
        {
            TextView remoteUser = new TextView(getContext());
            remoteUser.setId(ID_USER);
            remoteUser.setPadding(12, 0, 12, 0);
            remoteUser.setTypeface(null, Typeface.BOLD);
            remoteUser.setText(getRemoteName(chatMessage));
            remoteUser.setTextColor(0xff550390);
            remoteUser.setLayoutParams(Simple.layoutParamsWW(Gravity.START | Gravity.TOP));

            lastTextLayout.addView(remoteUser);
        }

        TextView textView = new TextView(getContext());
        textView.setPadding(10, 10, 10, 16);
        textView.setId(ID_TEXT);
        textView.setTextSize(30f);
        textView.setText(message);

        lastTextLayout.addView(textView);

        lp = new FrameLayout.LayoutParams(Simple.MP, 28, Gravity.BOTTOM);
        FrameLayout statusFrame = new FrameLayout(getContext());
        statusFrame.setLayoutParams(lp);

        lastTextLayout.addView(statusFrame);

        TextView statusTime = new TextView(getContext());
        statusTime.setId(ID_TIME);
        statusTime.setPadding(12, 0, 12, 0);
        statusTime.setText(Simple.getLocal24HTime(date));
        statusTime.setLayoutParams(Simple.layoutParamsMW(Gravity.END | Gravity.BOTTOM));

        lastTextLayout.addView(statusTime);

        checkMediaPath(chatMessage);

        incomingLastIdent = identity;

        protoIncoming.put(uuid, lastTextLayout);
    }

    public void createOutgoingMessage(JSONObject chatMessage)
    {
        String uuid = Json.getString(chatMessage, "uuid");
        String date = Json.getString(chatMessage, "date");
        String message = Json.getString(chatMessage, "message");

        checkDate(date);

        if ((lastDiv == null) || lastDivIsIncoming)
        {
            lastDiv = new LinearLayout(getContext());
            lastDiv.setOrientation(LinearLayout.VERTICAL);
            lastDiv.setLayoutParams(Simple.layoutParamsMW());

            lastDivIsIncoming = false;
            lastTextLayout = null;

            scrollcontent.addView(lastDiv);
        }

        if (accumulateMessage(message, date))
        {
            checkMediaPath(chatMessage);

            return;
        }

        FrameLayout textDiv = new FrameLayout(getContext());
        textDiv.setLayoutParams(Simple.layoutParamsMW());
        lastDiv.addView(textDiv);

        if (lastTextLayout != null)
        {
            lastTextLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal_ext);
        }

        lastTextLayout = new FrameLayout(getContext());
        lastTextLayout.setLayoutParams(Simple.layoutParamsWW(Gravity.END));
        lastTextLayout.setBackgroundResource(R.drawable.balloon_outgoing_normal);

        textDiv.addView(lastTextLayout);

        TextView textView = new TextView(getContext());
        textView.setId(ID_TEXT);
        textView.setPadding(10, 10, 24, 16);
        textView.setTextSize(30f);
        textView.setText(message);

        lastTextLayout.addView(textView);

        lp = new FrameLayout.LayoutParams(Simple.MP, 28, Gravity.BOTTOM);
        FrameLayout statusFrame = new FrameLayout(getContext());
        statusFrame.setLayoutParams(lp);

        lastTextLayout.addView(statusFrame);

        TextView statusTime = new TextView(getContext());
        statusTime.setLayoutParams(Simple.layoutParamsWW(Gravity.END | Gravity.BOTTOM));
        statusTime.setId(ID_TIME);
        statusTime.setPadding(0, 0, 24, 0);
        statusTime.setText(Simple.getLocal24HTime(date));
        statusFrame.addView(statusTime);

        lp = new FrameLayout.LayoutParams(38,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.END | Gravity.BOTTOM);

        ImageView statusImage = new ImageView(getContext());
        statusImage.setId(ID_STATUS);
        statusImage.setPadding(4, 4, 4, 4);

        if (chatMessage.has("acks"))
            statusImage.setImageResource(R.drawable.message_got_receipt_from_server);
        if (chatMessage.has("recv"))
            statusImage.setImageResource(R.drawable.message_got_receipt_from_target);
        if (chatMessage.has("read"))
            statusImage.setImageResource(R.drawable.message_got_read_receipt_from_target);

        statusFrame.addView(statusImage, lp);

        checkMediaPath(chatMessage);

        protoOutgoing.put(uuid, lastTextLayout);
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
