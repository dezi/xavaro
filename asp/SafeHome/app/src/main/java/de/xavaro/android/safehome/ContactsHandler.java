package de.xavaro.android.safehome;

import android.util.Log;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

@SuppressWarnings("SameParameterValue")

public class ContactsHandler
{
    @SuppressWarnings("FieldCanBeLocal")
    private final String LOGTAG = "ContactsHandler";

    private final Context ctx;

    private Cursor items;

    private JSONObject joitem;

    public ContactsHandler(Context context)
    {
        ctx = context;
    }

    public String contacts2JSON()
    {
        String json = "";

        items = ctx.getContentResolver().query(
                Data.CONTENT_URI,
                null, null, null, null);

        long ri, ci;

        String mt;

        JSONObject jocontacts = new JSONObject();

        while (items.moveToNext())
        {
            ri = items.getLong(items.getColumnIndex(Data._ID));
            ci = items.getLong(items.getColumnIndex(Data.RAW_CONTACT_ID));
            mt = items.getString(items.getColumnIndex(Data.MIMETYPE));

            joitem = new JSONObject();

            putAny("ROW_ID", ri);

            if (mt.equals(StructuredName.CONTENT_ITEM_TYPE))
            {
                putKind("StructuredName");

                // @formatter:off
                putString("DISPLAY_NAME",         StructuredName.DISPLAY_NAME);
                putString("GIVEN_NAME",           StructuredName.GIVEN_NAME);
                putString("FAMILY_NAME",          StructuredName.FAMILY_NAME);
                putString("PREFIX",               StructuredName.PREFIX);
                putString("MIDDLE_NAME",          StructuredName.MIDDLE_NAME);
                putString("SUFFIX",               StructuredName.SUFFIX);
                putString("PHONETIC_GIVEN_NAME",  StructuredName.PHONETIC_GIVEN_NAME);
                putString("PHONETIC_MIDDLE_NAME", StructuredName.PHONETIC_MIDDLE_NAME);
                putString("PHONETIC_FAMILY_NAME", StructuredName.PHONETIC_FAMILY_NAME);
                // @formatter:on
            }

            if (mt.equals(Phone.CONTENT_ITEM_TYPE))
            {
                putKind("Phone");

                // @formatter:off
                putString("NUMBER", Phone.NUMBER);
                putInt   ("TYPE",   Phone.TYPE);
                putString("LABEL",  Phone.LABEL);
                // @formatter:on
            }

            if (mt.equals(Email.CONTENT_ITEM_TYPE))
            {
                putKind("Email");

                // @formatter:off
                putString("ADDRESS", Email.ADDRESS);
                putInt   ("TYPE",    Email.TYPE);
                putString("LABEL",   Email.LABEL);
                // @formatter:on
            }

            if (mt.equals(Photo.CONTENT_ITEM_TYPE))
            {
                putKind("Photo");

                // @formatter:off
                putString("PHOTO_FILE_ID", Photo.PHOTO_FILE_ID);
                putBlob  ("PHOTO",         Photo.PHOTO);
                // @formatter:on
            }

            if (mt.equals(Organization.CONTENT_ITEM_TYPE))
            {
                putKind("Organization");

                // @formatter:off
                putString("COMPANY",         Organization.COMPANY);
                putInt   ("TYPE",            Organization.TYPE);
                putString("LABEL",           Organization.LABEL);
                putString("TITLE",           Organization.TITLE);
                putString("DEPARTMENT",      Organization.DEPARTMENT);
                putString("JOB_DESCRIPTION", Organization.JOB_DESCRIPTION);
                putString("SYMBOL",          Organization.SYMBOL);
                putString("PHONETIC_NAME",   Organization.PHONETIC_NAME);
                putString("OFFICE_LOCATION", Organization.OFFICE_LOCATION);
                // @formatter:on
            }

            if (mt.equals(Im.CONTENT_ITEM_TYPE))
            {
                putKind("Im");

                // @formatter:off
                putString("DATA",            Im.DATA);
                putInt   ("TYPE",            Im.TYPE);
                putString("LABEL",           Im.LABEL);
                putString("PROTOCOL",        Im.PROTOCOL);
                putString("CUSTOM_PROTOCOL", Im.CUSTOM_PROTOCOL);
                // @formatter:on
            }

            if (mt.equals(Nickname.CONTENT_ITEM_TYPE))
            {
                putKind("Nickname");

                // @formatter:off
                putString("NAME",  Nickname.NAME);
                putInt   ("TYPE",  Nickname.TYPE);
                putString("LABEL", Nickname.LABEL);
                // @formatter:on
            }

            if (mt.equals(Note.CONTENT_ITEM_TYPE))
            {
                putKind("Note");

                // @formatter:off
                putString("NOTE", Note.NOTE);
                // @formatter:on
            }

            if (mt.equals(StructuredPostal.CONTENT_ITEM_TYPE))
            {
                putKind("StructuredPostal");

                // @formatter:off
                putString("FORMATTED_ADDRESS", StructuredPostal.FORMATTED_ADDRESS);
                putInt   ("TYPE",              StructuredPostal.TYPE);
                putString("LABEL",             StructuredPostal.LABEL);
                putString("STREET",            StructuredPostal.STREET);
                putString("POBOX",             StructuredPostal.POBOX);
                putString("NEIGHBORHOOD",      StructuredPostal.NEIGHBORHOOD);
                putString("CITY",              StructuredPostal.CITY);
                putString("REGION",            StructuredPostal.REGION);
                putString("POSTCODE",          StructuredPostal.POSTCODE);
                putString("COUNTRY",           StructuredPostal.COUNTRY);
                // @formatter:on
            }

            if (mt.equals(GroupMembership.CONTENT_ITEM_TYPE))
            {
                putKind("GroupMembership");

                // @formatter:off
                putLong("GROUP_ROW_ID", GroupMembership.GROUP_ROW_ID);
                // @formatter:on
            }

            if (mt.equals(Website.CONTENT_ITEM_TYPE))
            {
                putKind("Website");

                // @formatter:off
                putString("URL",   Website.URL);
                putInt   ("TYPE",  Website.TYPE);
                putString("LABEL", Website.LABEL);
                // @formatter:on
            }

            if (mt.equals(Event.CONTENT_ITEM_TYPE))
            {
                putKind("Event");

                // @formatter:off
                putString("START_DATE", Event.START_DATE);
                putInt   ("TYPE",       Event.TYPE);
                putString("LABEL",      Event.LABEL);
                // @formatter:on
            }

            if (mt.equals(Relation.CONTENT_ITEM_TYPE))
            {
                putKind("Relation");

                // @formatter:off
                putString("NAME",  Relation.NAME);
                putInt   ("TYPE",  Relation.TYPE);
                putString("LABEL", Relation.LABEL);
                // @formatter:on
            }

            if (mt.equals(SipAddress.CONTENT_ITEM_TYPE))
            {
                putKind("SipAddress");

                // @formatter:off
                putString("SIP_ADDRESS", SipAddress.SIP_ADDRESS);
                putInt   ("TYPE",        SipAddress.TYPE);
                putString("LABEL",       SipAddress.LABEL);
                // @formatter:on
            }

            if (! joitem.has("KIND"))
            {
                String kind = mt;

                if (! kind.startsWith("vnd.android.cursor.item/"))
                {
                    //
                    // We ignore items not within
                    // the android cursor namespace.
                    //

                    continue;
                }

                kind = kind.substring(24);

                putKind("@" + kind);

                // @formatter:off
                putString("DATA1",  Data.DATA1);
                putString("DATA2",  Data.DATA2);
                putString("DATA3",  Data.DATA3);
                putString("DATA3",  Data.DATA3);
                putString("DATA4",  Data.DATA3);
                putString("DATA5",  Data.DATA5);
                putString("DATA6",  Data.DATA6);
                putString("DATA7",  Data.DATA7);
                putString("DATA8",  Data.DATA8);
                putString("DATA9",  Data.DATA9);
                putString("DATA10", Data.DATA10);
                putString("DATA11", Data.DATA11);
                putString("DATA12", Data.DATA12);
                putString("DATA13", Data.DATA13);
                putString("DATA14", Data.DATA14);
                putBlob  ("DATA15", Data.DATA15);
                // @formatter:on
            }

             JSONArray jocontact;

            try
            {
                if (jocontacts.has(String.valueOf(ci)))
                {
                    jocontact = jocontacts.getJSONArray(String.valueOf(ci));
                }
                else
                {
                    jocontacts.put(String.valueOf(ci), jocontact = new JSONArray());
                }

                jocontact.put(joitem);

            }
            catch (JSONException ignored)
            {
            }
        }

        items.close();

        try
        {
            json = jocontacts.toString(2);
        }
        catch (JSONException ignored)
        {
        }

        String[] lines = json.split("\n");

        for (String line : lines)
        {
            Log.d(LOGTAG, line);
        }

        return json;
    }

    private void putAny(String name, Object value)
    {
        try
        {
            joitem.put(name, value);
        }
        catch (JSONException ignored)
        {
        }
    }

    private void putKind(String value)
    {
        try
        {
            joitem.put("KIND", value);
        }
        catch (JSONException ignored)
        {
        }
    }

    private void putString(String name, String column)
    {
        try
        {
            joitem.put(name, items.getString(items.getColumnIndex(column)));
        }
        catch (JSONException ignored)
        {
        }
    }

    private void putInt(String name, String column)
    {
        try
        {
            joitem.put(name, items.getInt(items.getColumnIndex(column)));
        }
        catch (JSONException ignored)
        {
        }
    }

    private void putLong(String name, String column)
    {
        try
        {
            joitem.put(name, items.getLong(items.getColumnIndex(column)));
        }
        catch (JSONException ignored)
        {
        }
    }

    private void putBlob(String name, String column)
    {
        byte[] bytes = items.getBlob(items.getColumnIndex(column));

        if (bytes == null) return;

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[ bytes.length << 1 ];

        for (int inx = 0; inx < bytes.length; inx++)
        {
            //noinspection PointlessArithmeticExpression
            hexChars[ (inx << 1) + 0 ] = hexArray[ (bytes[ inx ] >> 4) & 0x0f ];
            //noinspection PointlessBitwiseExpression
            hexChars[ (inx << 1) + 1 ] = hexArray[ (bytes[ inx ] >> 0) & 0x0f ];
        }

        try
        {
            joitem.put(name,String.valueOf(hexChars));
        }
        catch (JSONException ignore)
        {
        }
    }
}
