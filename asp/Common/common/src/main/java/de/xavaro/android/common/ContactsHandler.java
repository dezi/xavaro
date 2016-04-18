package de.xavaro.android.common;

import android.support.annotation.Nullable;
import android.provider.ContactsContract;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class ContactsHandler
{
    private static final String LOGTAG = ContactsHandler.class.getSimpleName();

    public static JSONObject getJSONData()
    {
        return new ContactsHandler().getJSONDataInternal();
    }

    private JSONObject joitem;
    private Cursor items;

    @Nullable
    private JSONObject getJSONDataInternal()
    {
        Log.d(LOGTAG, "getJSONDataInternal");

        items = Simple.getAnyContext().getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                null, null, null, null);

        if (items == null) return null;

        long ri, ci;
        String mt;

        JSONObject jocontacts = new JSONObject();

        while (items.moveToNext())
        {
            ri = items.getLong(items.getColumnIndex(ContactsContract.Data._ID));
            ci = items.getLong(items.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            mt = items.getString(items.getColumnIndex(ContactsContract.Data.MIMETYPE));

            joitem = new JSONObject();

            putAny("ROW_ID", ri);

            if (mt.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
            {
                putKind("StructuredName");

                // @formatter:off
                putString("DISPLAY_NAME",         ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
                putString("GIVEN_NAME",           ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
                putString("FAMILY_NAME",          ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
                putString("PREFIX",               ContactsContract.CommonDataKinds.StructuredName.PREFIX);
                putString("MIDDLE_NAME",          ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
                putString("SUFFIX",               ContactsContract.CommonDataKinds.StructuredName.SUFFIX);
                putString("PHONETIC_GIVEN_NAME",  ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME);
                putString("PHONETIC_MIDDLE_NAME", ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME);
                putString("PHONETIC_FAMILY_NAME", ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
            {
                putKind("Phone");

                // @formatter:off
                putString("NUMBER", ContactsContract.CommonDataKinds.Phone.NUMBER);
                putInt   ("TYPE",   ContactsContract.CommonDataKinds.Phone.TYPE);
                putString("LABEL",  ContactsContract.CommonDataKinds.Phone.LABEL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE))
            {
                putKind("Email");

                // @formatter:off
                putString("ADDRESS", ContactsContract.CommonDataKinds.Email.ADDRESS);
                putInt   ("TYPE",    ContactsContract.CommonDataKinds.Email.TYPE);
                putString("LABEL",   ContactsContract.CommonDataKinds.Email.LABEL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE))
            {
                putKind("Photo");

                // @formatter:off
                putString("PHOTO_FILE_ID", ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID);
                putBlob  ("PHOTO",         ContactsContract.CommonDataKinds.Photo.PHOTO);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE))
            {
                putKind("Organization");

                // @formatter:off
                putString("COMPANY",         ContactsContract.CommonDataKinds.Organization.COMPANY);
                putInt   ("TYPE",            ContactsContract.CommonDataKinds.Organization.TYPE);
                putString("LABEL",           ContactsContract.CommonDataKinds.Organization.LABEL);
                putString("TITLE",           ContactsContract.CommonDataKinds.Organization.TITLE);
                putString("DEPARTMENT",      ContactsContract.CommonDataKinds.Organization.DEPARTMENT);
                putString("JOB_DESCRIPTION", ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION);
                putString("SYMBOL",          ContactsContract.CommonDataKinds.Organization.SYMBOL);
                putString("PHONETIC_NAME",   ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME);
                putString("OFFICE_LOCATION", ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE))
            {
                putKind("Im");

                // @formatter:off
                putString("DATA",            ContactsContract.CommonDataKinds.Im.DATA);
                putInt   ("TYPE",            ContactsContract.CommonDataKinds.Im.TYPE);
                putString("LABEL",           ContactsContract.CommonDataKinds.Im.LABEL);
                putString("PROTOCOL",        ContactsContract.CommonDataKinds.Im.PROTOCOL);
                putString("CUSTOM_PROTOCOL", ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
            {
                putKind("Nickname");

                // @formatter:off
                putString("NAME",  ContactsContract.CommonDataKinds.Nickname.NAME);
                putInt   ("TYPE",  ContactsContract.CommonDataKinds.Nickname.TYPE);
                putString("LABEL", ContactsContract.CommonDataKinds.Nickname.LABEL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE))
            {
                putKind("Note");

                // @formatter:off
                putString("NOTE", ContactsContract.CommonDataKinds.Note.NOTE);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE))
            {
                putKind("StructuredPostal");

                // @formatter:off
                putString("FORMATTED_ADDRESS", ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
                putInt   ("TYPE",              ContactsContract.CommonDataKinds.StructuredPostal.TYPE);
                putString("LABEL",             ContactsContract.CommonDataKinds.StructuredPostal.LABEL);
                putString("STREET",            ContactsContract.CommonDataKinds.StructuredPostal.STREET);
                putString("POBOX",             ContactsContract.CommonDataKinds.StructuredPostal.POBOX);
                putString("NEIGHBORHOOD",      ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD);
                putString("CITY",              ContactsContract.CommonDataKinds.StructuredPostal.CITY);
                putString("REGION",            ContactsContract.CommonDataKinds.StructuredPostal.REGION);
                putString("POSTCODE",          ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
                putString("COUNTRY",           ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE))
            {
                putKind("GroupMembership");

                // @formatter:off
                putLong("GROUP_ROW_ID", ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE))
            {
                putKind("Website");

                // @formatter:off
                putString("URL",   ContactsContract.CommonDataKinds.Website.URL);
                putInt   ("TYPE",  ContactsContract.CommonDataKinds.Website.TYPE);
                putString("LABEL", ContactsContract.CommonDataKinds.Website.LABEL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE))
            {
                putKind("Event");

                // @formatter:off
                putString("START_DATE", ContactsContract.CommonDataKinds.Event.START_DATE);
                putInt   ("TYPE",       ContactsContract.CommonDataKinds.Event.TYPE);
                putString("LABEL",      ContactsContract.CommonDataKinds.Event.LABEL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE))
            {
                putKind("Relation");

                // @formatter:off
                putString("NAME",  ContactsContract.CommonDataKinds.Relation.NAME);
                putInt   ("TYPE",  ContactsContract.CommonDataKinds.Relation.TYPE);
                putString("LABEL", ContactsContract.CommonDataKinds.Relation.LABEL);
                // @formatter:on
            }

            if (mt.equals(ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE))
            {
                putKind("SipAddress");

                // @formatter:off
                putString("SIP_ADDRESS", ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS);
                putInt   ("TYPE",        ContactsContract.CommonDataKinds.SipAddress.TYPE);
                putString("LABEL",       ContactsContract.CommonDataKinds.SipAddress.LABEL);
                // @formatter:on
            }

            if (!joitem.has("KIND"))
            {
                String kind = mt;

                if (kind.startsWith("vnd.android.cursor.item/"))
                {
                    kind = kind.substring(24);
                }

                putKind("@" + kind);

                // @formatter:off
                putString("DATA1",  ContactsContract.Data.DATA1);
                putString("DATA2",  ContactsContract.Data.DATA2);
                putString("DATA3",  ContactsContract.Data.DATA3);
                putString("DATA3",  ContactsContract.Data.DATA3);
                putString("DATA4",  ContactsContract.Data.DATA3);
                putString("DATA5",  ContactsContract.Data.DATA5);
                putString("DATA6",  ContactsContract.Data.DATA6);
                putString("DATA7",  ContactsContract.Data.DATA7);
                putString("DATA8",  ContactsContract.Data.DATA8);
                putString("DATA9",  ContactsContract.Data.DATA9);
                putString("DATA10", ContactsContract.Data.DATA10);
                putString("DATA11", ContactsContract.Data.DATA11);
                putString("DATA12", ContactsContract.Data.DATA12);
                putString("DATA13", ContactsContract.Data.DATA13);
                putString("DATA14", ContactsContract.Data.DATA14);
                putBlob  ("DATA15", ContactsContract.Data.DATA15);
                // @formatter:on
            }

            JSONArray jocontact;

            try
            {
                if (jocontacts.has(String.valueOf(ci)))
                {
                    jocontact = jocontacts.getJSONArray(String.valueOf(ci));
                } else
                {
                    jocontacts.put(String.valueOf(ci), jocontact = new JSONArray());
                }

                jocontact.put(joitem);

            } catch (JSONException ignored)
            {
            }
        }

        items.close();

        return jocontacts;
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

        try
        {
            joitem.put(name, Simple.getHexBytesToString(bytes));
        }
        catch (JSONException ignore)
        {
        }
    }
}
