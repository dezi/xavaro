package de.xavaro.android.safehome;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

public class FirewallActivity extends PreferenceActivity
{
    private static final String LOGTAG = FirewallActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PrefFragments.initialize(this);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        Header category;

        category = new Header();
        category.title = "Firewall";
        target.add(category);

        target.add(PrefFragments.SafetyFragment.getHeader());
        target.add(PrefFragments.DomainsFragment.getHeader());

        category = new Header();
        category.title = "Kommunikation";
        target.add(category);

        target.add(PrefFragments.PhoneFragment.getHeader());
        target.add(PrefFragments.SkypeFragment.getHeader());
        target.add(PrefFragments.WhatsAppFragment.getHeader());

        category = new Header();
        category.title = "Internet Media Streaming";
        target.add(category);

        target.add(PrefFragments.IPTelevisionFragment.getHeader());
        target.add(PrefFragments.IPRadioFragment.getHeader());

        category = new Header();
        category.title = "Internet Online Content";
        target.add(category);

        target.add(PrefFragments.WebConfigNewspaperFragment.getHeader());
        target.add(PrefFragments.WebConfigMagazineFragment.getHeader());
        target.add(PrefFragments.WebConfigShoppingFragment.getHeader());
        target.add(PrefFragments.WebConfigEroticsFragment.getHeader());

        setHeaders(target);
    }

    private List<Header> headers;

    private void setHeaders(List<Header> headers)
    {
        this.headers = headers;
    }

    private List<Header> getHeaders()
    {
        return headers;
    }

    @Override
    public void setListAdapter(ListAdapter adapter)
    {
        if (adapter == null)
        {
            super.setListAdapter(null);
        }
        else
        {
            super.setListAdapter(new HeaderAdapter(this, getHeaders()));
        }
    }

    private static class HeaderAdapter extends ArrayAdapter<Header>
    {
        static final int HEADER_TYPE_CATEGORY = 0;
        static final int HEADER_TYPE_NORMAL = 1;

        private static final int HEADER_TYPE_COUNT = HEADER_TYPE_NORMAL + 1;

        private static class HeaderViewHolder
        {
            ImageView icon;
            TextView title;
        }

        static int getHeaderType(Header header)
        {
            if (header.fragment == null && header.intent == null)
            {
                return HEADER_TYPE_CATEGORY;
            }
            else
            {
                return HEADER_TYPE_NORMAL;
            }
        }

        @Override
        public int getItemViewType(int position)
        {
            Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public boolean areAllItemsEnabled()
        {
            return false;
        }

        @Override
        public boolean isEnabled(int position)
        {
            return getItemViewType(position) != HEADER_TYPE_CATEGORY;
        }

        @Override
        public int getViewTypeCount()
        {
            return HEADER_TYPE_COUNT;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        }

        Context context;

        public HeaderAdapter(Context context, List<Header> objects)
        {
            super(context, 0, objects);

            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            HeaderViewHolder holder;
            Header header = getItem(position);
            int headerType = getHeaderType(header);

            View view = null;

            if (convertView == null)
            {
                holder = new HeaderViewHolder();

                LinearLayout flview;

                flview = new LinearLayout(context);
                flview.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 56));

                if (headerType == HEADER_TYPE_NORMAL)
                {
                    holder.icon = new DitUndDat.ImageAntiAliasView(context);
                    holder.icon.setPadding(24, 12, 0, 12);
                    flview.addView(holder.icon, new LinearLayout.LayoutParams(
                            56, ViewGroup.LayoutParams.MATCH_PARENT));
                }

                holder.title = new TextView(context);
                holder.title.setGravity(Gravity.CENTER_VERTICAL);
                holder.title.setPadding(16, 0, 0, 0);

                flview.addView(holder.title, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.START));

                if (headerType == HEADER_TYPE_NORMAL)
                {
                    holder.title.setTextSize(17f);
                    holder.title.setTextColor(0xff000000);
                }

                if (headerType == HEADER_TYPE_CATEGORY)
                {
                    holder.title.setTextSize(16f);
                    holder.title.setPadding(32, 0, 0, 0);
                    flview.setBackgroundColor(0xcccccccc);
                }

                flview.setTag(holder);

                view = flview;
            }
            else
            {
                view = convertView;

                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may
            // be recycled

            if ((holder.icon != null) && (header.iconRes != 0))
            {
                holder.icon.setImageResource(header.iconRes);
            }

            holder.title.setText(header.getTitle(getContext().getResources()));

            return view;
        }
    }
}
