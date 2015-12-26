package de.xavaro.android.safehome;

//
// Obtain profile images for name or phones.
//

import android.support.annotation.Nullable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class ProfileImages
{
    private static final String LOGTAG = ProfileImages.class.getSimpleName();

    //
    // Cached contacts.
    //

    private static JSONObject contacts = null;

    //
    // Retrieve contacts phone number from skype account.
    //

    @Nullable
    public static String getDisplayFromPhoneOrSkype(Context context, String identtag)
    {
        boolean ismatch;
        String display;

        if (contacts == null)
        {
            //
            // Read into static contacts once.
            //

            contacts = new ContactsHandler(context).contacts2JSONObject();
        }

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                ismatch = false;
                display = null;

                for (int inx = 0; inx < contact.length(); inx++)
                {
                    JSONObject item = contact.getJSONObject(inx);

                    if (item.has("NUMBER"))
                    {
                        String number = item.getString("NUMBER").replaceAll(" ","");

                        if (number.endsWith(identtag))
                        {
                            ismatch = true;
                        }
                    }

                    if (item.has("DATA1"))
                    {
                        String number = item.getString("DATA1").replaceAll(" ","");

                        if (number.endsWith(identtag))
                        {
                            ismatch = true;
                        }
                    }

                    if (item.has("DISPLAY_NAME"))
                    {
                        //
                        // Workaround for Skype which puts
                        // nickname as display name and
                        // duplicates it into given name.
                        //

                        String disp = item.getString("DISPLAY_NAME");
                        String gina = item.getString("GIVEN_NAME");

                        if ((display == null) || ! disp.equals(gina)) display = disp;
                    }
                }

                if (ismatch && (display != null))
                {
                    //
                    // We have found a phone number within skype contact.
                    //

                    return display;
                }
            }
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    @Nullable
    public static String getPhoneFromSkype(Context context, String skypename)
    {
        boolean ismatch;
        String phone;

        if (contacts == null)
        {
            //
            // Read into static contacts once.
            //

            contacts = new ContactsHandler(context).contacts2JSONObject();
        }

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                ismatch = false;
                phone = null;

                for (int inx = 0; inx < contact.length(); inx++)
                {
                    JSONObject item = contact.getJSONObject(inx);

                    if (item.has("DATA1"))
                    {
                        String number = item.getString("DATA1").replaceAll(" ","");

                        if (number.endsWith(skypename))
                        {
                            ismatch = true;
                        }
                    }

                    if (item.has("NUMBER"))
                    {
                        phone = item.getString("NUMBER");

                        if (phone != null)
                        {
                            phone = phone.replaceAll("\\+", "");
                            phone = phone.replaceAll(" ", "");
                        }
                    }
                }

                if (ismatch && (phone != null))
                {
                    //
                    // We have found a phone number within skype contact.
                    //

                    return phone;
                }
            }
        }
        catch (JSONException ignore)
        {
        }

        return null;
    }

    //
    // Retrieve profile image bitmap for contacts telefone number.
    //

    @Nullable
    public static Bitmap getContactsProfileBitmap(Context context, String search)
    {
        boolean ismatch = false;
        String photo = null;

        search = search.replaceAll(" ", "");

        if (contacts == null)
        {
            //
            // Read into static contacts once.
            //

            contacts = new ContactsHandler(context).contacts2JSONObject();
        }

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                ismatch = false;
                photo = null;

                for (int inx = 0; inx < contact.length(); inx++)
                {
                    JSONObject item = contact.getJSONObject(inx);

                    if (item.has("NUMBER"))
                    {
                        String number = item.getString("NUMBER").replaceAll(" ","");

                        if (number.endsWith(search))
                        {
                            ismatch = true;
                        }
                    }

                    if (item.has("DATA1"))
                    {
                        String number = item.getString("DATA1").replaceAll(" ","");

                        if (number.endsWith(search))
                        {
                            ismatch = true;
                        }
                    }

                    if (item.has("PHOTO"))
                    {
                        photo = item.getString("PHOTO");
                    }
                }

                if (ismatch && (photo != null)) break;
            }
        }
        catch (JSONException ignore)
        {
        }

        if ((! ismatch) || (photo == null)) return null;

        byte[] rawbytes = StaticUtils.hexStringToBytes(photo);

        return BitmapFactory.decodeByteArray(rawbytes, 0, rawbytes.length);
    }

    //
    // Retrieve profile image bitmap for Sykpe account.
    //

    @Nullable
    public static Bitmap getSkypeProfileBitmap(Context context, String skypename)
    {
        //
        // Try to get generic contact image.
        //

        Bitmap bitmap = getContactsProfileBitmap(context,skypename);
        if (bitmap != null) return bitmap;

        //
        // Search for a matching phone number in contacts.
        //

        String phone = getPhoneFromSkype(context, skypename);
        if (phone == null) return null;

        //
        // Retry with WhatsApp profile image anyway.
        //

        return getWhatsAppProfileBitmap(context, phone);
    }

    //
    // Retrieve profile image bitmap for WhatsApp telefone number.
    //

    @Nullable
    public static Bitmap getWhatsAppProfileBitmap(Context context, String phone)
    {
        if (phone.startsWith("+")) phone = phone.substring(1);

        String pwa = GlobalConfigs.packageWhatsApp;

        File datadir = context.getFilesDir();
        File wappdir = new File(Environment.getExternalStorageDirectory().getPath(),"WhatsApp/Profile Pictures");

        //
        // Lookup in cache area.
        //

        if (datadir.isDirectory())
        {
            String target = pwa + "." + phone;

            String[] cachedirlist = datadir.list();
            if (cachedirlist == null) return null;

            for (String ppfile : cachedirlist)
            {
                if (ppfile.startsWith(target))
                {
                    try
                    {
                        File cachepath = new File(datadir, ppfile);
                        File profilepath = new File(wappdir, ppfile.substring(pwa.length() + 1));

                        if (profilepath.exists() &&
                                (profilepath.lastModified() > cachepath.lastModified()))
                        {
                            cachepath.delete();

                            Log.d(LOGTAG, "getWhatsAppProfileBitmap: Cache image cleared.");

                            continue;
                        }

                        FileInputStream fi = new FileInputStream(cachepath);
                        Bitmap thumbnail = BitmapFactory.decodeStream(fi);
                        fi.close();

                        Log.d(LOGTAG, "getWhatsAppProfileBitmap: From data OK.");

                        return thumbnail;
                    }
                    catch (Exception ex)
                    {
                        OopsService.log(LOGTAG,ex);
                    }
                }
            }
        }

        //
        // Lookup in profile area.
        //

        if (wappdir.isDirectory())
        {
            String[] wappdirlist = wappdir.list();
            if (wappdirlist == null) return null;

            for (String ppfile : wappdirlist)
            {
                if (ppfile.startsWith(phone))
                {
                    try
                    {
                        File profilepath = new File(wappdir, ppfile);
                        File cachepath = new File(datadir, pwa + "." + ppfile);

                        StaticUtils.fileCopy(profilepath, cachepath);

                        FileInputStream fi = new FileInputStream(profilepath);
                        Bitmap thumbnail = BitmapFactory.decodeStream(fi);
                        fi.close();

                        Log.d(LOGTAG, "getWhatsAppProfileBitmap: From profile OK.");

                        return thumbnail;
                    }
                    catch (Exception ex)
                    {
                        OopsService.log(LOGTAG, ex);
                    }
                }
            }
        }

        //
        // Retry with generic contacts profile image.
        //

        return getContactsProfileBitmap(context, phone);
    }
}
