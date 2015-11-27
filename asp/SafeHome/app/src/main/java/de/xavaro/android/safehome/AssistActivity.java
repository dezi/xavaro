package de.xavaro.android.safehome;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class AssistActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assist);

        View contacts = findViewById(R.id.contacts);

        contacts.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Log.d("ASSIST","Contacts:");

                        ContactsHandler ch = new ContactsHandler(AssistActivity.this);

                        ch.contacts2JSON();

                        /*
                        ContentResolver crs = getContentResolver();

                        Cursor people = crs.query(
                                ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null);

                        String id,name,given,middle,family,phone,ptype,label,email;

                        while (people.moveToNext())
                        {
                            id = people.getString(people.getColumnIndex(
                                    ContactsContract.Data._ID));

                            name = people.getString(people.getColumnIndex(
                                    ContactsContract.Data.DISPLAY_NAME));

                            Log.d("ASSIST","Contacts: " + id + "=" + name);

                            Cursor sname = getContentResolver().query(
                                    ContactsContract.Data.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + id,
                                    null, null);

                            while (sname.moveToNext())
                            {
                                given = sname.getString(sname.getColumnIndex(
                                        CommonDataKinds.StructuredName.GIVEN_NAME));

                                middle = sname.getString(sname.getColumnIndex(
                                        CommonDataKinds.StructuredName.MIDDLE_NAME));

                                family = sname.getString(sname.getColumnIndex(
                                        CommonDataKinds.StructuredName.FAMILY_NAME));

                                Log.d("ASSIST","Contacts: " + id + "=" + given + "=" + middle + ":" + family);
                            }

                            Cursor phones = getContentResolver().query(
                                    CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);

                            Log.d("ASSIST","Contacts: " + id + "=" + name);

                            while (phones.moveToNext())
                            {
                                phone = phones.getString(phones.getColumnIndex(
                                        CommonDataKinds.Phone.NUMBER));

                                ptype = phones.getString(phones.getColumnIndex(
                                        CommonDataKinds.Phone.TYPE));

                                label = phones.getString(phones.getColumnIndex(
                                        CommonDataKinds.Phone.LABEL));

                                Log.d("ASSIST","Contacts: " + id + "=" + name + "=" + phone + ":" + ptype + ":" + label);
                            }

                            Cursor emails = getContentResolver().query(
                                    CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    CommonDataKinds.Email.CONTACT_ID + " = " + id,
                                    null, null);

                            while (emails.moveToNext())
                            {
                                email = emails.getString(emails.getColumnIndex(
                                        CommonDataKinds.Email.ADDRESS));

                                ptype = emails.getString(emails.getColumnIndex(
                                        CommonDataKinds.Email.TYPE));

                                label = emails.getString(emails.getColumnIndex(
                                        CommonDataKinds.Email.LABEL));

                                Log.d("ASSIST","Contacts: " + id + "=" + name + "=" + email + ":" + ptype + ":" + label);
                            }
                        }

                        people.close();
                    */
                    }
                });

    }
}
