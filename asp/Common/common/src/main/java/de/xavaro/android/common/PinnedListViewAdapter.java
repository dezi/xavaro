package de.xavaro.android.common;

import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.util.SparseIntArray;

public abstract class PinnedListViewAdapter extends BaseAdapter
        implements PinnedListView.PinnedListViewAdapterInterface
{
    private static final String LOGTAG = PinnedListViewAdapter.class.getSimpleName();

    private static final int HEAD_VIEW_TYPE = 0;
    private static final int ITEM_VIEW_TYPE = 0;

    private SparseIntArray mSectionCache = new SparseIntArray();
    private SparseIntArray mSectionCountCache = new SparseIntArray();
    private SparseIntArray mSectionPositionCache = new SparseIntArray();

    private int mCount = -1;
    private int mSectionCount = -1;

    @Override
    public void notifyDataSetChanged()
    {
        mSectionCache.clear();
        mSectionCountCache.clear();
        mSectionPositionCache.clear();

        mCount = -1;
        mSectionCount = -1;

        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated()
    {
        mSectionCache.clear();
        mSectionCountCache.clear();
        mSectionPositionCache.clear();

        mCount = -1;
        mSectionCount = -1;

        super.notifyDataSetInvalidated();
    }

    @Override
    public final int getCount()
    {
        if (mCount >= 0) return mCount;

        mCount = 0;

        for (int section = 0; section < internalGetSectionCount(); section++)
        {
            mCount += 1 + internalGetCountForSection(section);
        }

        return mCount;
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
        int section = mSectionCache.get(position, -1);
        if (section >= 0) return section;

        int sectionStart = 0;

        for (section = 0; section < internalGetSectionCount(); section++)
        {
            int sectionCount = internalGetCountForSection(section);
            int sectionEnd = sectionStart + sectionCount + 1;

            if (position >= sectionStart && position < sectionEnd)
            {
                mSectionCache.put(position, section);
                return section;
            }

            sectionStart = sectionEnd;
        }

        return 0;
    }

    public int getPositionInSectionForPosition(int position)
    {
        int sectionStart = mSectionPositionCache.get(position, -1);
        if (sectionStart >= 0) return sectionStart;

        sectionStart = 0;

        for (int section = 0; section < internalGetSectionCount(); section++)
        {
            int sectionCount = internalGetCountForSection(section);
            int sectionEnd = sectionStart + sectionCount + 1;

            if (position >= sectionStart && position < sectionEnd)
            {
                int positionInSection = position - sectionStart - 1;
                mSectionPositionCache.put(position, positionInSection);
                return positionInSection;
            }

            sectionStart = sectionEnd;
        }

        return 0;
    }

    public boolean isHead(int position)
    {
        int sectionStart = 0;

        for (int section = 0; section < internalGetSectionCount(); section++)
        {
            if (position == sectionStart)
            {
                return true;
            }

            if (position < sectionStart)
            {
                return false;
            }

            sectionStart += internalGetCountForSection(section) + 1;
        }

        return false;
    }

    public int getItemViewType(int section, int position)
    {
        return ITEM_VIEW_TYPE;
    }

    public int getItemViewTypeCount()
    {
        return 1;
    }

    public int getHeadViewType(int section)
    {
        return HEAD_VIEW_TYPE;
    }

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

    private int internalGetCountForSection(int section)
    {
        int sectionCount = mSectionCountCache.get(section, -1);
        if (sectionCount >= 0) return sectionCount;

        sectionCount = getCountForSection(section);
        mSectionCountCache.put(section, sectionCount);
        return sectionCount;
    }

    private int internalGetSectionCount()
    {
        if (mSectionCount >= 0)
        {
            return mSectionCount;
        }
        mSectionCount = getSectionCount();
        return mSectionCount;
    }

}