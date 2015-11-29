package de.xavaro.android.safehome;

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

                        ch.contacts2JSONString();
                    }
                });

    }
}
