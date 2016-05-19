package de.xavaro.android.common;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

//
// Maintain our system identity guid. The
// copy is stored in individual places,
// file storage, contact and cookies
// to avoid deletion by system or user.
//

public class SystemIdentity
{
    private static final String LOGTAG = SystemIdentity.class.getSimpleName();

    private static String appsname;
    private static String identity;
    private static String randomiz;

    private static String foundInPrefers;
    private static String foundInContact;
    private static String foundInStorage;
    private static String foundInCookies;

    //
    // Retrieve UUID or generate a new one.
    //

    public static String getIdentity()
    {
        if (identity == null) initialize();

        return identity;
    }

    private static void initialize()
    {
        if (identity != null) return;

        Context context = Simple.getAnyContext();

        retrieveFromPrefers(context);
        retrieveFromStorage(context);
        retrieveFromCookies(context);
        retrieveFromContact(context);

        try
        {
            if ((foundInPrefers != null) && (identity == null))
            {
                appsname = foundInPrefers.split(":")[ 0 ];
                identity = foundInPrefers.split(":")[ 1 ];
                randomiz = foundInPrefers.split(":")[ 2 ];
            }
        }
        catch (Exception ignore)
        {
        }

        try
        {
            if ((foundInStorage != null) && (identity == null))
            {
                appsname = foundInContact.split(":")[ 0 ];
                identity = foundInContact.split(":")[ 1 ];
                randomiz = foundInContact.split(":")[ 2 ];
            }
        }
        catch (Exception ignore)
        {
        }

        try
        {
            if ((foundInCookies != null) && (identity == null))
            {
                appsname = foundInCookies.split(":")[ 0 ];
                identity = foundInCookies.split(":")[ 1 ];
                randomiz = foundInCookies.split(":")[ 2 ];
            }
        }
        catch (Exception ignore)
        {
        }

        try
        {
            if ((foundInContact != null) && (identity == null))
            {
                String[] packageName = context.getPackageName().split("\\.");

                String[] ids = foundInContact.split(";");

                for (String id : ids)
                {
                    if (id.split(":")[ 0 ].equals(packageName[ packageName.length - 1 ]))
                    {
                        appsname = id.split(":")[ 0 ];
                        identity = id.split(":")[ 1 ];
                        randomiz = id.split(":")[ 2 ];

                        break;
                    }
                }
            }
        }
        catch (Exception ignore)
        {
        }

        if ((identity == null) || (randomiz == null))
        {
            //
            // Master create new uuid.
            //

            String[] packageName = context.getPackageName().split("\\.");

            appsname = packageName[ packageName.length - 1];
            identity = UUID.randomUUID().toString();
            randomiz = UUID.randomUUID().toString();

            foundInStorage = null;
            foundInCookies = null;
            foundInContact = null;
        }

        if (foundInPrefers == null) storeIntoPrefers(context);
        if (foundInStorage == null) storeIntoStorage(context);
        if (foundInContact == null) storeIntoContact(context);
        if (foundInCookies == null) storeIntoCookies(context);
    }

    private static void retrieveFromPrefers(Context context)
    {
        try
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            foundInPrefers = sp.getString("system.identity", null);
        }
        catch (Exception ignore)
        {
        }

        if (foundInPrefers != null) Log.d(LOGTAG, "foundInPrefers: " + foundInPrefers);
    }

    private static void storeIntoPrefers(Context context)
    {
        try
        {
            String value = appsname + ":" + identity + ":" + randomiz;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sp.edit().putString("system.identity", value).apply();
        }
        catch (Exception ignore)
        {
        }
    }

    private static void retrieveFromContact(Context context)
    {
        foundInContact = null;

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
                    String uuid = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.SOURCE_ID));

                    if ((uuid == null) || ! uuid.contains(":"))
                    {
                        //
                        // Bogus contact w/o UUID.
                        //

                        if (gone.equals("0"))
                        {
                            Uri delete = Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, rawi);
                            cr.delete(delete, null, null);
                            Log.d(LOGTAG, "retrieveFromContacts: delete=" + delete.toString());
                        }

                        continue;
                    }

                    if (gone.equals("0"))
                    {
                        //
                        // First class valid UUID.
                        //

                        foundInContact = uuid;
                    }
                    else
                    {
                        //
                        // Deleted account UUID. Accept if nothing else present.
                        //

                        if (foundInContact == null) foundInContact = uuid;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (cursor != null) cursor.close();

        if (foundInContact != null) Log.d(LOGTAG, "foundInContact: " + foundInContact);
    }

    private static void storeIntoContact(Context context)
    {
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        String idvalue = "";

        if (foundInContact != null)
        {
            String[] oldids = foundInContact.split(";");

            for (String oldid : oldids)
            {
                if (! oldid.split(":")[ 0 ].equals(appsname))
                {
                    //
                    // Identity from different app.
                    //

                    if (idvalue.length() > 0) idvalue += ";";
                    idvalue += oldid;
                }
            }
        }

        if (idvalue.length() > 0) idvalue += ";";
        idvalue += appsname + ":" + identity + ":" + randomiz;

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, context.getPackageName())
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "XAVARO")
                .withValue(ContactsContract.RawContacts.SOURCE_ID, idvalue)
                .build());

        cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, "XAVARO")
                .withValue(ContactsContract.Data.DATA15, idvalue)
                .build());

        try
        {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo);
        }
        catch (RemoteException | OperationApplicationException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static void retrieveFromStorage(Context context)
    {
        foundInStorage = null;

        String filename = context.getPackageName() + ".identity.json";
        FileInputStream inputStream;

        try
        {
            byte[] content = new byte[ 4096 ];

            inputStream = context.openFileInput(filename);
            int xfer = inputStream.read(content);
            inputStream.close();

            JSONObject ident = new JSONObject(new String(content,0,xfer));
            foundInStorage = ident.getString("identity");
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (foundInStorage != null) Log.d(LOGTAG,"foundInStorage: " + foundInStorage);
    }

    private static void storeIntoStorage(Context context)
    {
        String filename = context.getPackageName() + ".identity.json";
        FileOutputStream outputStream;

        try
        {
            JSONObject ident = new JSONObject();
            ident.put("identity", appsname + ":" + identity + ":" + randomiz);

            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(ident.toString(2).getBytes());
            outputStream.close();
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    private static void retrieveFromCookies(Context context)
    {
        foundInCookies = null;

        android.webkit.CookieManager wkCookieManager = android.webkit.CookieManager.getInstance();
        String cookies = wkCookieManager.getCookie("http://" + context.getPackageName());

        if ((cookies != null) && cookies.contains("identity="))
        {
            String temp = cookies.substring(cookies.indexOf("identity=") + 9);
            if (temp.contains(";")) temp = temp.substring(0,temp.indexOf(";"));

            foundInCookies = temp;
        }

        if (foundInCookies != null) Log.d(LOGTAG,"foundInCookies: " + foundInCookies);
    }

    private static void storeIntoCookies(Context context)
    {
        android.webkit.CookieManager wkCookieManager = android.webkit.CookieManager.getInstance();

        wkCookieManager.setCookie("http://" + context.getPackageName(),"identity=" + appsname + ":" + identity + ":" + randomiz);
    }
}
