package de.xavaro.android.common;

import android.util.Log;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.view.View;

public abstract class PinnedListViewAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        PinnedListView.PinnedListViewAdapterInterface
{
    private static final String LOGTAG = PinnedListViewAdapter.class.getSimpleName();

    private static final int HEAD_VIEW_TYPE = 0;
    private static final int ITEM_VIEW_TYPE = 0;

    private int totalCount = -1;

    @Override
    public void notifyDataSetChanged()
    {
        totalCount = -1;

        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated()
    {
        totalCount = -1;

        super.notifyDataSetInvalidated();
    }

    @Override
    public final int getCount()
    {
        if (totalCount >= 0) return totalCount;

        totalCount = 0;

        for (int section = 0; section < getSectionCount(); section++)
        {
            totalCount += 1 + getCountForSection(section);
        }

        return totalCount;
    }

    @Override
    public final Object getItem(int position)
    {
        return getItem(getSectionForPosition(position), getPositionInSectionForPosition(position));
    }

    @Override
    public final long getItemId(int position)
    {
        return getItemId(getSectionForPosition(position), getPositionInSectionForPosition(position));
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent)
    {
        int section = getSectionForPosition(position);

        if (isHead(position))
        {
            return getHeadView(section, convertView, parent);
        }

        return getItemView(section, getPositionInSectionForPosition(position), convertView, parent);
    }

    @Override
    public final int getItemViewType(int position)
    {
        int section = getSectionForPosition(position);

        if (isHead(position))
        {
            return getItemViewTypeCount() + getHeadViewType(section);
        }

        return getItemViewType(section, getPositionInSectionForPosition(position));
    }

    @Override
    public final int getViewTypeCount()
    {
        return getItemViewTypeCount() + getHeadViewTypeCount();
    }

    public final int getSectionForPosition(int position)
    {
        int sectionMax = getSectionCount();

        for (int section = 0, sectionPos = 0; section < sectionMax; section++)
        {
            sectionPos += getCountForSection(section) + 1;
            if (position < sectionPos) return section;
        }

        return 0;
    }

    public final int getPositionInSectionForPosition(int position)
    {
        int sectionMax = getSectionCount();

        for (int section = 0, sectionPos = 0; section < sectionMax; section++)
        {
            int sectionEnd = sectionPos + getCountForSection(section) + 1;

            if (position < sectionEnd) return position - sectionPos - 1;

            sectionPos = sectionEnd;
        }

        return 0;
    }

    public boolean isHead(int position)
    {
        int sectionStart = 0;

        for (int section = 0; section < getSectionCount(); section++)
        {
            if (position == sectionStart)
            {
                return true;
            }

            if (position < sectionStart)
            {
                return false;
            }

            sectionStart += getCountForSection(section) + 1;
        }

        return false;
    }

    @SuppressWarnings({"UnusedParameters", "WeakerAccess"})
    public int getItemViewType(int section, int position)
    {
        return ITEM_VIEW_TYPE;
    }

    @SuppressWarnings({"WeakerAccess", "SameReturnValue"})
    public int getItemViewTypeCount()
    {
        return 1;
    }

    @SuppressWarnings({"WeakerAccess", "SameReturnValue"})
    public int getHeadViewType(int section)
    {
        return HEAD_VIEW_TYPE;
    }

    @SuppressWarnings({"WeakerAccess", "SameReturnValue"})
    public int getHeadViewTypeCount()
    {
        return 1;
    }

    public abstract Object getItem(int section, int position);

    public abstract long getItemId(int section, int position);

    public abstract int getSectionCount();

    public abstract int getCountForSection(int section);

    public abstract View getItemView(int section, int position, View convertView, ViewGroup parent);

    public abstract View getHeadView(int section, View convertView, ViewGroup parent);

    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(LOGTAG, "onItemClick: " + position);

        int section = getSectionForPosition(position);
        int posinsect = getPositionInSectionForPosition(position);

        if (posinsect == -1)
        {
            onHeadClick(parent, view, section, id);
        }
        else
        {
            onItemClick(parent, view, section, posinsect, id);
        }
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.d(LOGTAG, "onItemLongClick: " + position);

        int section = getSectionForPosition(position);
        int posinsect = getPositionInSectionForPosition(position);

        if (posinsect == -1)
        {
            return onHeadLongClick(parent, view, section, id);
        }
        else
        {
            return onItemLongClick(parent, view, section, posinsect, id);
        }
    }

    @SuppressWarnings({"UnusedParameters", "WeakerAccess"})
    public void onHeadClick(AdapterView<?> parent, View view, int section, long id)
    {
        Log.d(LOGTAG, "onHeadClick: section=" + section);
    }

    @SuppressWarnings({"UnusedParameters", "WeakerAccess"})
    public void onItemClick(AdapterView<?> parent, View view, int section, int position, long id)
    {
        Log.d(LOGTAG, "onItemClick: section=" + section + " position=" + position);
    }

    @SuppressWarnings({"UnusedParameters", "WeakerAccess", "SameReturnValue"})
    public boolean onHeadLongClick(AdapterView<?> parent, View view, int section, long id)
    {
        Log.d(LOGTAG, "onHeadLongClick: section=" + section);

        return true;
    }

    @SuppressWarnings({"UnusedParameters", "WeakerAccess", "SameReturnValue"})
    public boolean onItemLongClick(AdapterView<?> parent, View view, int section, int position, long id)
    {
        Log.d(LOGTAG, "onItemLongClick: section=" + section + " position=" + position);

        return true;
    }
}