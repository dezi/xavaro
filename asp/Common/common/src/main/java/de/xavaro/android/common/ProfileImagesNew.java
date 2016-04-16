package de.xavaro.android.common;

import android.support.annotation.Nullable;

import android.provider.ContactsContract;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
    public static Drawable getOwnerProfileDrawable()
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

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            return Simple.getDrawable(BitmapFactory.decodeStream(bais));
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
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
    public static Bitmap getXavaroProfileBitmap(String identity)
    {
        File imagefile = getXavaroProfileImageFile(identity);

        if (imagefile.exists())
        {
            try
            {
                FileInputStream fi = new FileInputStream(imagefile);
                Bitmap bitmap = BitmapFactory.decodeStream(fi);
                fi.close();

                return bitmap;
            }
            catch (Exception ex)
            {
                OopsService.log(LOGTAG, ex);
            }
        }

        return null;
    }

    //endregion Xavaro profiles
}
