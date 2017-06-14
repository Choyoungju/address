package com.example.cho.address;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


/**
 * Created by JH on 2015-05-27.
 */
public class ContactManager extends Activity {
    private Button mAddAcountButton;
    private ListView mContactList;
    private boolean mShowInvisible;
    private CheckBox mShowInvisibleControl;

    @Override
    protected void onResume() {
        populateContactList();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_manager);

        mAddAcountButton = (Button) findViewById(R.id.addContactButton);
        mContactList = (ListView)findViewById(R.id.contactList);
        mShowInvisibleControl = (CheckBox) findViewById(R.id.showInvisible);

        mShowInvisible = false;
        mShowInvisibleControl.setChecked(mShowInvisible);

        mAddAcountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchContactAdder();
            }
        });

        mShowInvisibleControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mShowInvisible = isChecked;
                populateContactList();
            }
        });

        populateContactList();
    }

    private void populateContactList(){
        Cursor cursor = getContacts();
        String[] fields = new String[] {ContactsContract.Data.DISPLAY_NAME};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_entry, cursor, fields, new int[]{R.id.contactEntryText},0);
        mContactList.setAdapter(adapter);

    }

    private Cursor getContacts(){
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"+(mShowInvisible? "0" : "1") + "'";
        String[] selectionAgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return getContentResolver().query(uri,projection,selection,selectionAgs,sortOrder);
    }

    protected void launchContactAdder(){
        Intent i = new Intent(this, ContactAdder.class);
        startActivity(i);
    }
}
