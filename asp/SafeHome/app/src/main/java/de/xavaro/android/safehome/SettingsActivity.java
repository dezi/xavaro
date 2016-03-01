package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.Simple;
import de.xavaro.android.common.WebApp;

public class SettingsActivity extends PreferenceActivity
{
    private static final String LOGTAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Simple.setAppContext(this);

        SettingsFragments.initialize(this);
        DitUndDat.SharedPrefs.initialize(this);
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume...");

        super.onResume();

        Simple.setAppContext(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        Log.d(LOGTAG, "onWindowFocusChanged=" + hasFocus);

        super.onWindowFocusChanged(hasFocus);

        CommonStatic.setFocused(SettingsActivity.class.getSimpleName(), hasFocus);
        CommonStatic.settingschanged = true;
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return true;
    }

    @Override
    public void onHeaderClick(Header header, int position)
    {
        Log.d(LOGTAG, "onHeaderClick: " + header.title + "=" + header.fragmentArguments);

        super.onHeaderClick(header, position);
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        Header category;

        category = new Header();
        category.title = "Wichtige Einstellungen";
        target.add(category);

        target.add(PreferencesHealth.HealthMedicatorFragment.getHeader());

        target.add(PreferencesBasics.OwnerFragment.getHeader());
        target.add(PreferencesBasics.AdminFragment.getHeader());
        target.add(PreferencesBasics.CommunityFragment.getHeader());
        target.add(PreferencesBasics.AlertgroupFragment.getHeader());

        category = new Header();
        category.title = "Kommunikation";
        target.add(category);

        target.add(PreferencesComm.PhoneFragment.getHeader());
        target.add(PreferencesComm.SkypeFragment.getHeader());
        target.add(PreferencesComm.WhatsAppFragment.getHeader());
        target.add(PreferencesComm.XavaroFragment.getHeader());

        category = new Header();
        category.title = "Webapps";
        target.add(category);

        PreferencesWebApps.WebappFragment.getHeaders(target);

        category = new Header();
        category.title = "Medien";
        target.add(category);

        target.add(PreferencesMedia.MediaImageFragment.getHeader());
        target.add(PreferencesMedia.MediaAudioFragment.getHeader());
        target.add(PreferencesMedia.MediaVideoFragment.getHeader());
        target.add(PreferencesMedia.MediaEbookFragment.getHeader());

        category = new Header();
        category.title = "Apps";
        target.add(category);

        target.add(SettingsFragments.AppsDiscounterFragment.getHeader());

        category = new Header();
        category.title = "Vitaldaten";
        target.add(category);

        target.add(PreferencesHealth.HealthPersonalFragment.getHeader());
        target.add(PreferencesHealth.HealthBPMFragment.getHeader());
        target.add(PreferencesHealth.HealthScaleFragment.getHeader());
        target.add(PreferencesHealth.HealthSensorFragment.getHeader());
        target.add(PreferencesHealth.HealthGlucoseFragment.getHeader());
        target.add(PreferencesHealth.HealthUnitsFragment.getHeader());

        category = new Header();
        category.title = "Internet Media Streaming";
        target.add(category);

        target.add(PreferencesWebStream.IPTelevisionFragment.getHeader());
        target.add(PreferencesWebStream.IPRadioFragment.getHeader());

        category = new Header();
        category.title = "Internet Online Content";
        target.add(category);

        target.add(PreferencesWebFrame.WebConfigNewspaperFragment.getHeader());
        target.add(PreferencesWebFrame.WebConfigMagazineFragment.getHeader());
        target.add(PreferencesWebFrame.WebConfigPictorialFragment.getHeader());
        target.add(PreferencesWebFrame.WebConfigShoppingFragment.getHeader());
        target.add(PreferencesWebFrame.WebConfigEroticsFragment.getHeader());

        category = new Header();
        category.title = "Firewall";
        target.add(category);

        target.add(PreferencesFirewall.SafetyFragment.getHeader());
        target.add(PreferencesFirewall.DomainsFragment.getHeader());

        category = new Header();
        category.title = "Betaversion";
        target.add(category);

        target.add(PreferencesBeta.DeveloperFragment.getHeader());

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

            View view;

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

            //
            // All view fields must be updated every time,
            // because the view may be recycled.
            //

            if (holder.icon != null)
            {
                if (header.iconRes != 0)
                {
                    holder.icon.setImageResource(header.iconRes);
                }
                else
                {
                    //
                    // Check if we have a webapp here.
                    //

                    if (header.fragmentArguments != null)
                    {
                        String webappname = header.fragmentArguments.getString("webappname");

                        if (webappname != null)
                        {
                            holder.icon.setImageDrawable(WebApp.getAppIcon(webappname));
                        }
                    }
                }
            }

            holder.title.setText(header.getTitle(getContext().getResources()));

            return view;
        }
    }
}
