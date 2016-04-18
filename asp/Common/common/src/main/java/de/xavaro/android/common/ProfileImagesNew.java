package de.xavaro.android.common;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import android.provider.ContactsContract;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

public class ProfileImagesNew
{
    private static final String LOGTAG = ProfileImagesNew.class.getSimpleName();

    //region Owner profile

    @Nullable
    private static String getOwnerProfileImageUrl()
    {
        Cursor items = Simple.getAnyContext().getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                null, null, null, null);

        if ((items == null) || ! items.moveToNext()) return null;

        int photoCol = items.getColumnIndex(ContactsContract.Profile.PHOTO_URI);
        String photoUrl = items.getString(photoCol);
        items.close();

        return photoUrl;
    }

    private static File getOwnerProfileImageFile()
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "xavaro.image." + SystemIdentity.getIdentity() + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static Bitmap getOwnerProfileBitmap(boolean circle)
    {
        String photoUrl = getOwnerProfileImageUrl();
        if (photoUrl == null) return null;

        try
        {
            InputStream photoStream = Simple.getInputStream(photoUrl);
            if (photoStream == null) return null;

            byte[] data = Simple.getAllInputData(photoStream);
            photoStream.close();
            if (data == null) return null;

            //
            // Save data to profiles directory under own identity.
            //

            Simple.putFileBytes(getOwnerProfileImageFile(), data);

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static Drawable getOwnerProfileDrawable(boolean circle)
    {
        return Simple.getDrawable(getOwnerProfileBitmap(circle));
    }

    public static void sendOwnerImage(String remoteIdentity)
    {
        File ownerimage = getOwnerProfileImageFile();

        if (ownerimage.exists())
        {
            CommSender.sendFile(ownerimage.toString(), "profiles", remoteIdentity);
        }
    }

    //endregion Owner profile

    //region Xavaro profiles

    private static File getXavaroProfileImageFile(String identity)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "xavaro.image." + identity + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static Bitmap getXavaroProfileBitmap(String identity, boolean circle)
    {
        File imagefile = getXavaroProfileImageFile(identity);

        if (imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    @Nullable
    public static Drawable getXavaroProfileDrawable(String identity, boolean circle)
    {
        return Simple.getDrawable(getXavaroProfileBitmap(identity, circle));
    }

    //endregion Xavaro profiles

    //region Contacts profiles

    private static JSONObject contacts = null;

    private static File getContactsProfileImageFile(String phonenumber)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "contacts.image." + phonenumber.replaceAll(" ", "") + ".jpg";
        return new File(profilespath, profilename);
    }

    private static void getContactsProfileImage(File imagefile, String phonenumber)
    {
        String search = phonenumber.replaceAll(" ", "");
        boolean ismatch = false;
        String photo = null;

        if (contacts == null)
        {
            //
            // Read into static contacts once.
            //

            contacts = ContactsHandler.getJSONData();
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
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        if (ismatch && (photo != null))
        {
            Simple.putFileBytes(imagefile, Simple.getHexStringToBytes(photo));
        }
    }

    @Nullable
    public static Bitmap getContactsProfileBitmap(String phonenumber, boolean circle)
    {
        if (phonenumber == null) return null;

        File imagefile = getContactsProfileImageFile(phonenumber);

        if (! imagefile.exists())
        {
            getContactsProfileImage(imagefile, phonenumber);
        }

        if (imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    @Nullable
    public static Drawable getContactsProfileDrawable(String phonenumber, boolean circle)
    {
        return Simple.getDrawable(getContactsProfileBitmap(phonenumber, circle));
    }

    //endregion Contacts profiles

    //region WhatsApp profiles

    private static File getWhatsAppProfileImageFile(String phonenumber)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "whatsapp.image." + phonenumber.replaceAll(" ", "") + ".jpg";
        return new File(profilespath, profilename);
    }

    private static void getWhatsAppProfileImage(File imagefile, String phonenumber)
    {
        if (phonenumber.startsWith("+")) phonenumber = phonenumber.substring(1);
        phonenumber = phonenumber.replaceAll(" ", "");

        String wappsub = "WhatsApp/Profile Pictures";
        File wappdir = new File(Simple.getExternalStorageDir(), wappsub);
        if (! wappdir.isDirectory()) return;

        String[] wappdirlist = wappdir.list();
        if (wappdirlist == null) return;

        for (String ppfile : wappdirlist)
        {
            if (ppfile.startsWith(phonenumber))
            {
                File profilefile = new File(wappdir, ppfile);
                Simple.fileCopy(profilefile, imagefile);
            }
        }
    }

    @Nullable
    public static Bitmap getWhatsAppProfileBitmap(String phonenumber, boolean circle)
    {
        if (phonenumber == null) return null;

        File imagefile = getWhatsAppProfileImageFile(phonenumber);

        if (! imagefile.exists())
        {
            getWhatsAppProfileImage(imagefile, phonenumber);
        }

        if (imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    @Nullable
    public static Drawable getWhatsAppProfileDrawable(String phonenumber, boolean circle)
    {
        return Simple.getDrawable(getWhatsAppProfileBitmap(phonenumber, circle));
    }

    //endregion WhatsApp profiles

    @Nullable
    public static Bitmap getCircleBitmap(Bitmap bitmap)
    {
        if (bitmap == null) return null;

        Bitmap output = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        int color = Color.RED;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
