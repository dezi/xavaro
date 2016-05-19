package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.provider.ContactsContract;
import android.database.Cursor;
import android.util.Log;

public class Owner
{
    private static final String LOGTAG = ProfileImages.class.getSimpleName();

    @Nullable
    public static String getOwnerFirstName()
    {
        String nameReverse = getOwnerName();
        if (nameReverse == null) return null;

        String[] parts = nameReverse.split(", ");
        return (parts.length > 0) ? parts[ 1 ] : null;
    }

    @Nullable
    public static String getOwnerGivenName()
    {
        String nameReverse = getOwnerName();
        if (nameReverse == null) return null;

        String[] parts = nameReverse.split(", ");
        return (parts.length > 0) ? parts[ 0 ] : null;
    }

    @Nullable
    private static String getOwnerName()
    {
        Cursor items = Simple.getAnyContext().getContentResolver().query(
                ContactsContract.Profile.CONTENT_RAW_CONTACTS_URI,
                null, null, null, null);

        if ((items == null) || ! items.moveToNext()) return null;

        String name = null;

        try
        {
            int nameCol = items.getColumnIndex("display_name_alt");
            name = items.getString(nameCol);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        items.close();

        return name;
    }
}
