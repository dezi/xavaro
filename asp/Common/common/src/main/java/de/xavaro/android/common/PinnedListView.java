package de.xavaro.android.common;

import android.widget.HeaderViewListAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.content.Context;
import android.graphics.Canvas;
import android.view.ViewGroup;
import android.view.View;
import android.util.AttributeSet;

public class PinnedListView extends ListView implements OnScrollListener
{
    private OnScrollListener mOnScrollListener;

    public interface PinnedListViewAdapterInterface
    {
        boolean isHead(int position);

        int getSectionForPosition(int position);

        View getHeadView(int section, View convertView, ViewGroup parent);

        int getHeadViewType(int section);

        int getCount();
    }

    private PinnedListViewAdapterInterface mAdapter;
    private View mCurrentHeader;
    private int mCurrentHeaderViewType = 0;
    private float mHeaderOffset;
    private boolean mShouldPin = true;
    private int mCurrentSection = 0;
    private int mWidthMode;
    private int mHeightMode;

    public PinnedListView(Context context)
    {
        super(context);
        super.setOnScrollListener(this);
    }

    public PinnedListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        super.setOnScrollListener(this);
    }

    public PinnedListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
    }

    public void setPinHeaders(boolean shouldPin)
    {
        mShouldPin = shouldPin;
    }

    @Override
    public void setAdapter(ListAdapter adapter)
    {
        mCurrentHeader = null;
        mAdapter = (PinnedListViewAdapterInterface) adapter;
        super.setAdapter(adapter);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (mOnScrollListener != null)
        {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        if (mAdapter == null || mAdapter.getCount() == 0 || !mShouldPin || (firstVisibleItem < getHeaderViewsCount()))
        {
            mCurrentHeader = null;
            mHeaderOffset = 0.0f;
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++)
            {
                View header = getChildAt(i);
                if (header != null)
                {
                    header.setVisibility(VISIBLE);
                }
            }
            return;
        }

        firstVisibleItem -= getHeaderViewsCount();

        int section = mAdapter.getSectionForPosition(firstVisibleItem);
        int viewType = mAdapter.getHeadViewType(section);
        mCurrentHeader = getHeadView(section, mCurrentHeaderViewType != viewType ? null : mCurrentHeader);
        ensurePinnedHeaderLayout(mCurrentHeader);
        mCurrentHeaderViewType = viewType;

        mHeaderOffset = 0.0f;

        for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++)
        {
            if (mAdapter.isHead(i))
            {
                View header = getChildAt(i - firstVisibleItem);
                float headerTop = header.getTop();
                float pinnedHeaderHeight = mCurrentHeader.getMeasuredHeight();
                header.setVisibility(VISIBLE);
                if (pinnedHeaderHeight >= headerTop && headerTop > 0)
                {
                    mHeaderOffset = headerTop - header.getHeight();
                }
                else
                    if (headerTop <= 0)
                    {
                        header.setVisibility(INVISIBLE);
                    }
            }
        }

        invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (mOnScrollListener != null)
        {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private View getHeadView(int section, View oldView)
    {
        boolean shouldLayout = section != mCurrentSection || oldView == null;

        View view = mAdapter.getHeadView(section, oldView, this);
        if (shouldLayout)
        {
            // a new section, thus a new header. We should lay it out again
            ensurePinnedHeaderLayout(view);
            mCurrentSection = section;
        }
        return view;
    }

    private void ensurePinnedHeaderLayout(View header)
    {
        if (header.isLayoutRequested())
        {
            int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), mWidthMode);

            int heightSpec;
            ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
            if (layoutParams != null && layoutParams.height > 0)
            {
                heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            }
            else
            {
                heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }
            header.measure(widthSpec, heightSpec);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);

        if ((mAdapter == null) || (mCurrentHeader == null) || ! mShouldPin)
        {
            return;
        }

        int saveCount = canvas.save();

        canvas.translate(0, mHeaderOffset);
        canvas.clipRect(0, 0, getWidth(), mCurrentHeader.getMeasuredHeight());
        mCurrentHeader.draw(canvas);

        canvas.restoreToCount(saveCount);
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener)
    {
        mOnScrollListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
    }

    public void setOnItemClickListener(PinnedListView.OnItemClickListener listener)
    {
        super.setOnItemClickListener(listener);
    }

    public static abstract class OnItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int rawPosition, long id)
        {
            PinnedListViewAdapter adapter;

            if (adapterView.getAdapter().getClass().equals(HeaderViewListAdapter.class))
            {
                HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView.getAdapter();
                adapter = (PinnedListViewAdapter) wrapperAdapter.getWrappedAdapter();
            }
            else
            {
                adapter = (PinnedListViewAdapter) adapterView.getAdapter();
            }

            int section = adapter.getSectionForPosition(rawPosition);
            int position = adapter.getPositionInSectionForPosition(rawPosition);

            if (position == -1)
            {
                onSectionClick(adapterView, view, section, id);
            }
            else
            {
                onItemClick(adapterView, view, section, position, id);
            }
        }

        public abstract void onItemClick(AdapterView<?> adapterView, View view, int section, int position, long id);

        public abstract void onSectionClick(AdapterView<?> adapterView, View view, int section, long id);

    }
}