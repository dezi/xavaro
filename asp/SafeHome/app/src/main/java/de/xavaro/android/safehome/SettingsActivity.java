package de.xavaro.android.safehome;

import android.preference.PreferenceActivity;
import android.content.Intent;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.View;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import de.xavaro.android.common.CaptureSettings;
import de.xavaro.android.common.CommonStatic;
import de.xavaro.android.common.ImageSmartView;
import de.xavaro.android.common.Simple;

public class SettingsActivity extends CaptureSettings
{
    private static final String LOGTAG = SettingsActivity.class.getSimpleName();

    private static List<PreferenceActivity.Header> headers;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOGTAG, "onActivityResult");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Simple.setActContext(this);
        Simple.setThreadPolicy();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        getListView().setKeepScreenOn(true);
    }

    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume...");

        super.onResume();

        Simple.setActContext(this);
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

        target.add(PreferencesBasicsPermissions.getHeader());
        target.add(PreferencesBasicsOwner.getHeader());
        target.add(PreferencesBasicsCommunity.getHeader());
        target.add(PreferencesBasicsAssistance.getHeader());
        target.add(PreferencesBasicsMonitors.getHeader());
        target.add(PreferencesBasicsSafety.getHeader());

        category = new Header();
        category.title = "Vitaldaten";
        target.add(category);

        target.add(PreferencesHealth.HealthPersonalFragment.getHeader());
        target.add(PreferencesHealth.HealthBPMFragment.getHeader());
        target.add(PreferencesHealth.HealthOxyFragment.getHeader());
        target.add(PreferencesHealth.HealthGlucoseFragment.getHeader());
        target.add(PreferencesHealth.HealthScaleFragment.getHeader());
        target.add(PreferencesHealth.HealthSensorFragment.getHeader());
        target.add(PreferencesHealth.HealthUnitsFragment.getHeader());
        target.add(PreferencesHealth.HealthMedicatorFragment.getHeader());

        category = new Header();
        category.title = "Kommunikation";
        target.add(category);

        target.add(PreferencesBasicsCalls.getHeader());

        target.add(PreferencesComm.PhoneFragment.getHeader());
        target.add(PreferencesComm.SkypeFragment.getHeader());
        target.add(PreferencesComm.WhatsAppFragment.getHeader());

        target.add(PreferencesCommXavaro.getHeader());

        category = new Header();
        category.title = "Social Networks";
        target.add(category);

        target.add(PreferencesSocialFacebook.getHeader());
        target.add(PreferencesSocialTwitter.getHeader());
        target.add(PreferencesSocialGoogleplus.getHeader());
        target.add(PreferencesSocialInstagram.getHeader());
        target.add(PreferencesSocialTinder.getHeader());

        category = new Header();
        category.title = "Web-Apps";
        target.add(category);

        PreferencesWebApps.WebappFragment.getHeaders(target);

        category = new Header();
        category.title = "Medien";
        target.add(category);

        target.add(PreferencesMedia.MediaImageFragment.getHeader());
        target.add(PreferencesMedia.MediaVideoFragment.getHeader());
        target.add(PreferencesMedia.MediaAudioFragment.getHeader());
        target.add(PreferencesMedia.MediaEbookFragment.getHeader());

        category = new Header();
        category.title = "Apps";
        target.add(category);

        target.add(PreferencesApps.AppsDiscounterFragment.getHeader());

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

        target.add(PreferencesDeveloper.DeveloperFragment.getHeader());

        setHeaders(target);
    }

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
            ImageSmartView icon;
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
                        ViewGroup.LayoutParams.MATCH_PARENT, Simple.DP(56)));

                if (headerType == HEADER_TYPE_NORMAL)
                {
                    holder.icon = new ImageSmartView(context);
                    Simple.setPadding(holder.icon, 24, 12, 0, 12);
                    flview.addView(holder.icon, new LinearLayout.LayoutParams(
                            Simple.DP(56), ViewGroup.LayoutParams.MATCH_PARENT));
                }

                holder.title = new TextView(context);
                holder.title.setGravity(Gravity.CENTER_VERTICAL);
                Simple.setPadding(holder.title, 16, 0, 0, 0);

                flview.addView(holder.title, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.START));

                if (headerType == HEADER_TYPE_NORMAL)
                {
                    Simple.setTextSize(holder.title, 17f);
                    holder.title.setTextColor(0xff000000);
                }

                if (headerType == HEADER_TYPE_CATEGORY)
                {
                    Simple.setTextSize(holder.title, 16f);
                    Simple.setPadding(holder.title, 32, 0, 0, 0);
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
                            if ((holder.icon.getTag() == null) ||
                                    ! Simple.equals((String) holder.icon.getTag(), webappname))
                            {
                                holder.icon.setTag(webappname);
                                holder.icon.setImageResource(webappname);
                            }
                        }
                    }
                }
            }

            holder.title.setText(header.getTitle(getContext().getResources()));

            return view;
        }
    }
}
