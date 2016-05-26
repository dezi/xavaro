package de.xavaro.android.common;

//
// Stub class for checking if a notification is still required.
//

public class NotifyChecker
{
    //
    // Return true if notification still required.
    //

    public  boolean onCheckNotifyCondition(NotifyIntent intent)
    {
        //
        // To be overriden.
        //

        return true;
    }
}
