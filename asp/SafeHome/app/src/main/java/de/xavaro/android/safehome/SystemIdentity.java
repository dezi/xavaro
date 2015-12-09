package de.xavaro.android.safehome;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
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

    public static String identity;
    public static String randomiz;

    private static String foundInContacts;
    private static String foundInStorage;
    private static String foundInCookies;

    //
    // Retrieve UUID or generate a new one.
    //

    public static void initialize(Context context)
    {
        retrieveFromStorage(context);
        retrieveFromCookies(context);
        retrieveFromContacts(context);

        try
        {
            if (foundInContacts != null)
            {
                identity = foundInContacts.split(":")[ 0 ];
                randomiz = foundInContacts.split(":")[ 1 ];
            }
            else
            if (foundInStorage != null)
            {
                identity = foundInStorage.split(":")[ 0 ];
                randomiz = foundInStorage.split(":")[ 1 ];
            }
            else
            if (foundInCookies != null)
            {
                identity = foundInCookies.split(":")[ 0 ];
                randomiz = foundInCookies.split(":")[ 1 ];
            }
        }
        catch (Exception ex)
        {
        }

        if ((identity == null) || (randomiz == null))
        {
            //
            // Master create new uuid.
            //

            identity = UUID.randomUUID().toString();
            randomiz = UUID.randomUUID().toString();

            foundInStorage  = null;
            foundInCookies  = null;
            foundInContacts = null;
        }

        if (foundInStorage == null) storeIntoStorage(context);

        if (foundInContacts == null) storeIntoContacts(context);

        if (foundInCookies == null) storeIntoCookies(context);
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
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (cursor != null) cursor.close();

        if (foundInContacts != null) Log.d(LOGTAG, "foundInContacts: " + foundInContacts);
    }

    private static void storeIntoContacts(Context context)
    {
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        String idvalue = identity + ":" + randomiz;

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
            ident.put("identity",identity + ":" + randomiz);

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
        wkCookieManager.setCookie("http://" + context.getPackageName(),"identity=" + identity + ":" + randomiz);
    }
}
