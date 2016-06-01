package de.xavaro.android.safehome;

import android.content.Context;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.util.AttributeSet;
import android.util.Log;

import de.xavaro.android.common.BackKeyClient;

public class LaunchFrame extends FrameLayout implements BackKeyClient
{
    private static final String LOGTAG = LaunchFrame.class.getSimpleName();

    protected final LaunchItem parent;

    public LaunchFrame(Context context, LaunchItem parent)
    {
        super(context);

        this.parent = parent;
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        //
        // Adjust pages bullets and subtitle.
        //

        ViewParent parentview = getParent();

        while (parentview != null)
        {
            if (parentview instanceof HomeFrame)
            {
                String sublabel = (parent != null) ? (String) parent.label.getText() : null;

                ((HomeFrame) parentview).setActivePage(1, 1, sublabel);

                break;
            }

            parentview = parentview.getParent();
        }
    }

    public boolean onBackKeyWanted()
    {
        //
        // To be overwritten.
        //

        Log.d(LOGTAG, "onBackKeyWanted");

        return false;
    }

    public void onBackKeyExecuted()
    {
        //
        // To be overwritten.
        //

        Log.d(LOGTAG, "onBackKeyExecuted");
    }
}
