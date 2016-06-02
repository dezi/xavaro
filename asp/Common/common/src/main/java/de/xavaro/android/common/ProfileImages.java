package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.provider.ContactsContract;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.InputStream;
import java.io.File;

public class ProfileImages
{
    private static final String LOGTAG = ProfileImages.class.getSimpleName();

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

    private static File getOwnerProfilePath()
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "xavaro.image." + SystemIdentity.getIdentity() + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    private static Bitmap getOwnerProfileBitmap(boolean circle)
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

            Simple.putFileBytes(getOwnerProfilePath(), data);

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

    private static Drawable ownerCachePlain;
    private static Drawable ownerCacheCircle;

    @Nullable
    public static Drawable getOwnerProfileDrawable(boolean circle)
    {
        if (circle)
        {
            if (ownerCacheCircle != null)
            {
                if (((BitmapDrawable) ownerCacheCircle).getBitmap().isRecycled())
                {
                    ownerCacheCircle = null;
                }
            }

            if (ownerCacheCircle == null)
            {
                ownerCacheCircle = Simple.getDrawable(getOwnerProfileBitmap(true));
            }

            return ownerCacheCircle;
        }

        if (ownerCachePlain != null)
        {
            if (((BitmapDrawable) ownerCachePlain).getBitmap().isRecycled())
            {
                ownerCachePlain = null;
            }
        }

        if (ownerCachePlain == null)
        {
            ownerCachePlain = Simple.getDrawable(getOwnerProfileBitmap(false));
        }

        return ownerCachePlain;
    }

    public static void sendOwnerImage(String remoteIdentity)
    {
        File ownerimage = getOwnerProfilePath();

        if (ownerimage.exists())
        {
            CommSender.sendFile(ownerimage.toString(), "profiles", remoteIdentity);
        }
    }

    //endregion Owner profile

    //region Xavaro profiles

    private static File getXavaroProfilePath(String identity)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "xavaro.image." + identity + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    private static File getXavaroProfileFile(String identity)
    {
        File imagefile = getXavaroProfilePath(identity);

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    private static Bitmap getXavaroProfileBitmap(String identity, boolean circle)
    {
        File imagefile = getXavaroProfilePath(identity);

        if (imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    public static void removeXavaroProfileFile(String identity)
    {
        File file = getXavaroProfilePath(identity);

        if (file.exists() && file.delete())
        {
            Log.d(LOGTAG, "removeXavaroProfilePath: deleted=" + identity);
        }
    }

    //endregion Xavaro profiles

    //region Contacts profiles

    private static JSONObject contacts = null;

    private static File getContactsProfilePath(String phonenumber)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "contacts.image." + phonenumber.replaceAll(" ", "") + ".jpg";
        return new File(profilespath, profilename);
    }

    private static void getContactsProfileImage(File imagefile, String phonenumber)
    {
        if (phonenumber == null) return;

        String search = phonenumber.replaceAll(" ", "");

        if (contacts == null) contacts = ContactsHandler.getJSONData();
        if (contacts == null) return;

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                boolean ismatch = false;
                String photo = null;

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

                if (ismatch && (photo != null))
                {
                    Simple.putFileBytes(imagefile, Simple.getHexStringToBytes(photo));
                    break;
                }
            }
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @Nullable
    private static File getContactsProfileFile(String phonenumber)
    {
        if (phonenumber == null) return null;

        File imagefile = getContactsProfilePath(phonenumber);

        if (! imagefile.exists())
        {
            getContactsProfileImage(imagefile, phonenumber);
        }

        return imagefile.exists() ? imagefile : null;
    }


    @Nullable
    private static Bitmap getContactsProfileBitmap(String phonenumber, boolean circle)
    {
        if (phonenumber == null) return null;

        File imagefile = getContactsProfilePath(phonenumber);

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

    //endregion Contacts profiles

    //region WhatsApp profiles

    private static File getWhatsAppProfilePath(String phonenumber)
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
    private static File getWhatsAppProfileFile(String phonenumber)
    {
        if (phonenumber == null) return null;

        File imagefile = getWhatsAppProfilePath(phonenumber);

        if (! imagefile.exists())
        {
            getWhatsAppProfileImage(imagefile, phonenumber);

            if (! imagefile.exists())
            {
                //
                // Fallback to contacts image.
                //

                getContactsProfileImage(imagefile, phonenumber);
            }
        }

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    private static Bitmap getWhatsAppProfileBitmap(String phonenumber, boolean circle)
    {
        if (phonenumber == null) return null;

        File imagefile = getWhatsAppProfilePath(phonenumber);

        if (! imagefile.exists())
        {
            getWhatsAppProfileImage(imagefile, phonenumber);

            if (! imagefile.exists())
            {
                //
                // Fallback to contacts image.
                //

                getContactsProfileImage(imagefile, phonenumber);
            }
        }

        if (imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion WhatsApp profiles

    //region Skype profiles

    private static File getSkypeProfilePath(String skypename)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "skype.image." + skypename + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    private static File getSkypeProfileFile(String skypename)
    {
        if (skypename == null) return null;

        File imagefile = getSkypeProfilePath(skypename);

        if (! imagefile.exists())
        {
            String phonenumber = getPhoneFromSkype(skypename);

            if (phonenumber != null)
            {
                getContactsProfileImage(imagefile, phonenumber);

                if (! imagefile.exists())
                {
                    //
                    // Fallback to WhatsApp image.
                    //

                    getWhatsAppProfileImage(imagefile, phonenumber);
                }
            }
        }

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    private static Bitmap getSkypeProfileBitmap(String skypename, boolean circle)
    {
        if (skypename == null) return null;

        File imagefile = getSkypeProfilePath(skypename);

        if (! imagefile.exists())
        {
            String phonenumber = getPhoneFromSkype(skypename);

            if (phonenumber != null)
            {
                getContactsProfileImage(imagefile, phonenumber);

                if (! imagefile.exists())
                {
                    //
                    // Fallback to WhatsApp image.
                    //

                    getWhatsAppProfileImage(imagefile, phonenumber);
                }
            }
        }

        if (imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion Skype profiles

    //region Facebook profiles

    public static File getFacebookProfilePath(String facebookid)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "facebook.image." + facebookid + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static File getFacebookProfileFile(String facebookid)
    {
        if (facebookid == null) return null;

        File imagefile = getFacebookProfilePath(facebookid);

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    public static File getFacebookLoadProfileImage(String facebookid)
    {
        if (facebookid == null) return null;

        File imagefile = getFacebookProfilePath(facebookid);

        if (! imagefile.exists())
        {
            Simple.putFileBytes(imagefile, SocialFacebook.getInstance().getUserIconData(facebookid));
        }

        return imagefile;
    }

    @Nullable
    public static Bitmap getFacebookProfileBitmap(String facebookid, boolean circle)
    {
         File imagefile = getFacebookLoadProfileImage(facebookid);

        if ((imagefile != null) && imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion Facebook profiles

    //region Instagram profiles

    public static File getInstagramProfilePath(String instagramid)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "instagram.image." + instagramid + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static File getInstagramProfileFile(String instagramid)
    {
        if (instagramid == null) return null;

        File imagefile = getInstagramProfilePath(instagramid);

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    public static File getInstagramLoadProfileImage(String instagramid)
    {
        if (instagramid == null) return null;

        File imagefile = getInstagramProfilePath(instagramid);

        if (! imagefile.exists())
        {
            Simple.putFileBytes(imagefile, SocialInstagram.getInstance().getUserIconData(instagramid));
        }

        return imagefile;
    }

    @Nullable
    public static Bitmap getInstagramProfileBitmap(String instagramid, boolean circle)
    {
        File imagefile = getInstagramLoadProfileImage(instagramid);

        if ((imagefile != null) && imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion Instagram profiles

    //region Googleplus profiles

    public static File getGoogleplusProfilePath(String googleplusid)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "googleplus.image." + googleplusid + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static File getGoogleplusProfileFile(String googleplusid)
    {
        if (googleplusid == null) return null;

        File imagefile = getGoogleplusProfilePath(googleplusid);

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    public static File getGoogleplusLoadProfileImage(String googleplusid)
    {
        if (googleplusid == null) return null;

        File imagefile = getGoogleplusProfilePath(googleplusid);

        if (! imagefile.exists())
        {
            Simple.putFileBytes(imagefile, SocialGoogleplus.getInstance().getUserIconData(googleplusid));
        }

        return imagefile;
    }

    @Nullable
    public static Bitmap getGoogleplusProfileBitmap(String googleplusid, boolean circle)
    {
        File imagefile = getGoogleplusLoadProfileImage(googleplusid);

        if ((imagefile != null) && imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion Googleplus profiles

    //region Twitter profiles

    public static File getTwitterProfilePath(String twitterid)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "twitter.image." + twitterid + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static File getTwitterProfileFile(String twitterid)
    {
        if (twitterid == null) return null;

        File imagefile = getTwitterProfilePath(twitterid);

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    public static File getTwitterLoadProfileImage(String twitterid)
    {
        if (twitterid == null) return null;

        File imagefile = getTwitterProfilePath(twitterid);

        if (! imagefile.exists())
        {
            Simple.putFileBytes(imagefile, SocialTwitter.getInstance().getUserIconData(twitterid));
        }

        return imagefile;
    }

    @Nullable
    public static Bitmap getTwitterProfileBitmap(String twitterid, boolean circle)
    {
        File imagefile = getTwitterLoadProfileImage(twitterid);

        if ((imagefile != null) && imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion Twitter profiles

    //region Tinder profiles

    public static File getTinderProfilePath(String tinderid)
    {
        File profilespath = Simple.getMediaPath("profiles");
        String profilename = "tinder.image." + tinderid + ".jpg";
        return new File(profilespath, profilename);
    }

    @Nullable
    public static File getTinderProfileFile(String tinderid)
    {
        if (tinderid == null) return null;

        File imagefile = getTinderProfilePath(tinderid);

        return imagefile.exists() ? imagefile : null;
    }

    @Nullable
    public static File getTinderLoadProfileImage(String tinderid)
    {
        if (tinderid == null) return null;

        File imagefile = getTinderProfilePath(tinderid);

        if (! imagefile.exists())
        {
            Simple.putFileBytes(imagefile, SocialTinder.getInstance().getUserIconData(tinderid));
        }

        return imagefile;
    }

    @Nullable
    public static Bitmap getTinderProfileBitmap(String tinderid, boolean circle)
    {
        File imagefile = getTinderLoadProfileImage(tinderid);

        if ((imagefile != null) && imagefile.exists())
        {
            Bitmap bitmap = Simple.getBitmap(imagefile);
            if (circle) bitmap = getCircleBitmap(bitmap);
            return bitmap;
        }

        return null;
    }

    //endregion Tinder profiles

    //region Social profiles

    public static File getSocialUserImageFile(String platform, String pfid)
    {
        if (Simple.equals(platform, "facebook"))
        {
            return getFacebookProfileFile(pfid);
        }

        if (Simple.equals(platform, "instagram"))
        {
            return getInstagramProfileFile(pfid);
        }

        if (Simple.equals(platform, "googleplus"))
        {
            return getGoogleplusProfileFile(pfid);
        }

        if (Simple.equals(platform, "twitter"))
        {
            return getTwitterProfileFile(pfid);
        }

        if (Simple.equals(platform, "tinder"))
        {
            return getTinderProfileFile(pfid);
        }

        return null;
    }

    public static void loadSocialUserImageFile(String platform, String pfid)
    {
        if (Simple.equals(platform, "facebook"))
        {
            ProfileImages.getFacebookLoadProfileImage(pfid);
        }

        if (Simple.equals(platform, "instagram"))
        {
            ProfileImages.getInstagramLoadProfileImage(pfid);
        }

        if (Simple.equals(platform, "googleplus"))
        {
            ProfileImages.getGoogleplusLoadProfileImage(pfid);
        }

        if (Simple.equals(platform, "twitter"))
        {
            ProfileImages.getTwitterLoadProfileImage(pfid);
        }

        if (Simple.equals(platform, "tinder"))
        {
            ProfileImages.getTinderLoadProfileImage(pfid);
        }
    }

    public static Drawable getSocialProfileDrawable(String platform, String pfid, boolean circle)
    {
        Bitmap bitmap = null;

        if (Simple.equals(platform, "facebook"))
        {
            bitmap = ProfileImages.getFacebookProfileBitmap(pfid, circle);
        }

        if (Simple.equals(platform, "instagram"))
        {
            bitmap = ProfileImages.getInstagramProfileBitmap(pfid, circle);
        }

        if (Simple.equals(platform, "googleplus"))
        {
            bitmap = ProfileImages.getGoogleplusProfileBitmap(pfid, circle);
        }

        if (Simple.equals(platform, "twitter"))
        {
            bitmap = ProfileImages.getTwitterProfileBitmap(pfid, circle);
        }

        if (Simple.equals(platform, "tinder"))
        {
            bitmap = ProfileImages.getTinderProfileBitmap(pfid, circle);
        }

        return Simple.getDrawable(bitmap);
    }

    //endregion Social profiles

    private static final Map<String, Drawable> drawableCache = new HashMap<>();

    public static File getAnonProfileFile()
    {
        return new File("" + CommonConfigs.IconResAnon);
    }

    private static Bitmap getAnonProfileBitmap(boolean circle)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(Simple.getResources(), CommonConfigs.IconResAnon);

        if (circle && (bitmap != null))
        {
            return getCircleBitmap(bitmap);
        }

        return bitmap;
    }

    @Nullable
    public static File getProfileFile(String identtag)
    {
        File file = getWhatsAppProfileFile(identtag);
        if (file == null) file = getXavaroProfileFile(identtag);
        if (file == null) file = getContactsProfileFile(identtag);
        if (file == null) file = getSkypeProfileFile(identtag);
        if (file == null) file = getAnonProfileFile();

        return file;
    }

    public static boolean isAnonProfile(String identtag)
    {
        File file = getWhatsAppProfileFile(identtag);
        if (file == null) file = getXavaroProfileFile(identtag);
        if (file == null) file = getContactsProfileFile(identtag);
        if (file == null) file = getSkypeProfileFile(identtag);

        return (file == null);
    }

    @Nullable
    public static Drawable getProfileDrawable(String identtag, boolean circle)
    {
        String cachetag = circle + ":" + identtag;

        if (drawableCache.containsKey(cachetag))
        {
            //Log.d(LOGTAG, "getProfileDrawable: cached=" + cachetag);

            return drawableCache.get(cachetag);
        }

        Bitmap bitmap = getWhatsAppProfileBitmap(identtag, circle);
        if (bitmap == null) bitmap = getXavaroProfileBitmap(identtag, circle);
        if (bitmap == null) bitmap = getContactsProfileBitmap(identtag, circle);
        if (bitmap == null) bitmap = getSkypeProfileBitmap(identtag, circle);
        if (bitmap == null) bitmap = getAnonProfileBitmap(circle);

        Drawable drawable = Simple.getDrawable(bitmap);

        if (drawable != null) drawableCache.put(cachetag, drawable);

        return drawable;
    }

    //region Data getters

    @Nullable
    public static String getDisplayName(String identtag)
    {
        if (identtag == null) return null;

        identtag = identtag.replaceAll(" ", "");

        if (contacts == null) contacts = ContactsHandler.getJSONData();
        if (contacts == null) return null;

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                boolean ismatch = false;
                String display = null;

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
    public static String getPhoneFromName(String name)
    {
        if (name == null) return null;

        if (contacts == null) contacts = ContactsHandler.getJSONData();
        if (contacts == null) return null;

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                boolean ismatch = false;
                String phone = null;

                for (int inx = 0; inx < contact.length(); inx++)
                {
                    JSONObject item = contact.getJSONObject(inx);

                    if (item.has("NUMBER"))
                    {
                        phone = item.getString("NUMBER");

                        if (phone != null) phone = phone.replaceAll(" ", "");
                    }

                    if (item.has("DISPLAY_NAME"))
                    {
                        //
                        // Workaround for Skype which puts
                        // nickname as display name and
                        // duplicates it into given name.
                        //

                        String disp = item.getString("DISPLAY_NAME");


                        if (disp.equals(name))
                        {
                            ismatch = true;
                        }
                    }
                }

                if (ismatch && (phone != null))
                {
                    //
                    // We have found a phone number from display name.
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

    @Nullable
    public static String getPhoneFromSkype(String skypename)
    {
        if (contacts == null) contacts = ContactsHandler.getJSONData();
        if (contacts == null) return null;

        try
        {
            Iterator<?> ids = contacts.keys();

            while (ids.hasNext())
            {
                String id = (String) ids.next();
                JSONArray contact = contacts.getJSONArray(id);
                if (contact == null) continue;

                boolean ismatch = false;
                String phone = null;

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

                        if (phone != null) phone = phone.replaceAll(" ", "");
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
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    //endregion Data getters

    @Nullable
    private static Bitmap getCircleBitmap(Bitmap bitmap)
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
