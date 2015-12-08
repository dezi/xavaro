package de.xavaro.android.safehome;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

//
// Maintain our system identity guid.
//


public class SystemIdentity
{
    private static final String LOGTAG = SystemIdentity.class.getSimpleName();

    public static String identity;

    private static String foundInContacts;
    private static String foundInStorage;

    //
    // Retrieve UUID or generate a new one.
    //

    public static void initialize(Context context)
    {
        retrieveFromContacts(context);
        retrieveFromStorage(context);

        if ((foundInContacts == null) && (foundInStorage == null))
        {
            //
            // Master create new uuid.
            //

            identity = UUID.randomUUID().toString();
        }
        else
        {
            //
            // We give preference to the contacts UUID,
            // since user or system might clear app data
            // every now and then.
            //

            if (foundInContacts != null)
            {
                identity = foundInContacts;
            }
            else
            {
                identity = foundInStorage;
            }
        }
    }

    private static void retrieveFromContacts(Context context)
    {
        foundInContacts = null;

        Cursor cursor = null;

        try
        {
            ContentResolver cr = context.getContentResolver();
            Uri uri = ContactsContract.RawContacts.CONTENT_URI;
            String selection = ContactsContract.RawContacts.ACCOUNT_TYPE + " = ?";
            String[] selectionArguments = { context.getPackageName() };
            cursor = cr.query(uri, null, selection, selectionArguments, null);

            if (cursor != null)
            {
                while (cursor.moveToNext())
                {
                    String rawi = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                    String gone = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DELETED));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                    String type = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    String uuid = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.SOURCE_ID));

                    Log.d(LOGTAG,"retrieveFromContacts: (" + rawi + ") " + name + "=" + type + "=" + uuid);

                    if (uuid == null)
                    {
                        //
                        // Bogus contact w/o UUID.
                        //

                        Uri delete = Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, rawi);
                        Log.d(LOGTAG, "retrieveFromContacts: delete=" + delete.toString());
                        cr.delete(delete, null, null);

                        continue;
                    }

                    foundInContacts = uuid;

                    if (gone.equals("0"))
                    {
                        //
                        // First class valid UUID.
                        //

                        foundInContacts = uuid;
                    }
                    else
                    {
                        //
                        // Deleted account UUID. Accept if nothing else present.
                        //

                        if (foundInContacts == null) foundInContacts = uuid;
                    }
                }
            }

            int x = 0;
            int y = 2 / x;
        }
        catch (Exception ex)
        {
            OopsService.Log(LOGTAG, ex);
        }

        if (cursor != null) cursor.close();
    }

    private static void retrieveFromStorage(Context context)
    {
        foundInStorage = null;
    }

    private static void create(Context context)
    {
        String uuid = UUID.randomUUID().toString();

        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, context.getPackageName())
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "XAVARO")
                .withValue(ContactsContract.RawContacts.SOURCE_ID, uuid)
                .build());

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "XAVARO")
                .withValue(ContactsContract.Data.DATA15, uuid)
                .build());

        try
        {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
        }
        catch (RemoteException ex)
        {
            //logs;
        }
        catch (OperationApplicationException ex)
        {
            ex.printStackTrace();
        }
    }
}
