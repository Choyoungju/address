package com.example.cho.address;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by JH on 2015-05-27.
 */
public class ContactAdder extends Activity implements OnAccountsUpdateListener{
    private ArrayList<AccountData> mAccounts;
    private AccountAdapter mAccountAdapter;
    private Spinner mAccountSpinner;
    private EditText mContactEmailEditText;
    private ArrayList<Integer> mContactEmailTypes;
    private Spinner mContactEmailTypeSpinner;
    private EditText mContactNameEditText;
    private EditText mContactPhoneEditText;
    private ArrayList<Integer> mContactPhoneTypes;
    private Spinner mContactPhoneTypeSpinner;
    private Button mContactSaveButton;
    private AccountData mSelectedAccount;



    private class AccountAdapter extends ArrayAdapter<AccountData>{
        public AccountAdapter(Context context, ArrayList<AccountData> accountData){
            super(context, android.R.layout.simple_spinner_item, accountData);
            setDropDownViewResource(R.layout.account_entry);
        }
        public View getDropDownView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater layoutInflater = getLayoutInflater();
                convertView = layoutInflater.inflate(R.layout.account_entry, parent, false);
            }
            TextView firstAccountLine = (TextView) convertView.findViewById(R.id.firstAccountLine);
            TextView secondAccountLine = (TextView) convertView.findViewById(R.id.secondAccountLine);
            ImageView accountIcon = (ImageView) convertView.findViewById(R.id.accountIcon);

            AccountData data = getItem(position);
            firstAccountLine.setText(data.getName());
            secondAccountLine.setText(data.getTypeLabel());

            Drawable icon = data.getIcon();

            if(icon == null){
                icon = getResources().getDrawable(android.R.drawable.ic_menu_search);

            }
            accountIcon.setImageDrawable(icon);
            return convertView;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_adder);

        mContactNameEditText = (EditText)findViewById(R.id.contactNameEditText);
        mContactSaveButton = (Button) findViewById(R.id.contactSaveButton);
        mContactSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClicked();
            }
        });

        mAccountSpinner = (Spinner) findViewById(R.id.accountSpinner);
        mAccounts = new ArrayList<AccountData>();
        mAccountAdapter = new AccountAdapter(this, mAccounts);
        mAccountSpinner.setAdapter(mAccountAdapter);

        AccountManager.get(this).addOnAccountsUpdatedListener(this,null,true);
        mAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAccountSelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mContactPhoneEditText = (EditText) findViewById(R.id.contactPhoneEditText);
        mContactPhoneTypeSpinner = (Spinner) findViewById(R.id.contactPhoneTypeSpinner);

        mContactPhoneTypes = new ArrayList<Integer>();
        mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        mContactPhoneTypes.add(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Iterator<Integer> iter;
        iter = mContactPhoneTypes.iterator();

        while(iter.hasNext()){
            adapter.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    this.getResources(),
                    iter.next(),
                    getString(R.string.undefinedTypeLabel)).toString());
        }

        mContactPhoneTypeSpinner.setAdapter(adapter);
        mContactPhoneTypeSpinner.setPrompt(getString(R.string.selectLabel));



        mContactEmailEditText = (EditText) findViewById(R.id.contactEmailEditText);
        mContactEmailTypeSpinner = (Spinner) findViewById(R.id.contactEmailTypeSpinner);

        mContactEmailTypes = new ArrayList<Integer>();
        mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_HOME);
        mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_MOBILE);
        mContactEmailTypes.add(ContactsContract.CommonDataKinds.Email.TYPE_OTHER);


        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        iter = mContactEmailTypes.iterator();

        while(iter.hasNext()){
            adapter.add(ContactsContract.CommonDataKinds.Email.getTypeLabel(
                    this.getResources(),
                    iter.next(),
                    getString(R.string.undefinedTypeLabel)).toString());
        }

        mContactEmailTypeSpinner.setAdapter(adapter);
        mContactEmailTypeSpinner.setPrompt(getString(R.string.selectLabel));

    }
    protected void onSaveButtonClicked(){
        createContactEntry();
        finish();
    }
    protected void createContactEntry(){
        String name = mContactNameEditText.getText().toString();
        String phone = mContactPhoneEditText.getText().toString();
        String email = mContactEmailEditText.getText().toString();

        int phoneType = mContactPhoneTypes.get(mContactPhoneTypeSpinner.getSelectedItemPosition());
        int emailType = mContactEmailTypes.get(mContactEmailTypeSpinner.getSelectedItemPosition());

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, mSelectedAccount.getType())
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, mSelectedAccount.getName())
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailType)
                .build());

        try{
            getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);

        }catch(Exception e){
            Context ctx = getApplicationContext();
            CharSequence txt = getString(R.string.contactCreationFailure);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(ctx,txt,duration);
            toast.show();
        }

    }

    @Override
    protected void onDestroy() {
        AccountManager.get(this).removeOnAccountsUpdatedListener(this);
        super.onDestroy();
    }

    public void onAccountsUpdated(Account[] a){
        mAccounts.clear();

        AuthenticatorDescription[] accountTypes = AccountManager.get(this).getAuthenticatorTypes();

        for(int i = 0; i<a.length;i++){
            String systemAccountType = a[i].type;
            AuthenticatorDescription ad = getAuthenticatorDescription(systemAccountType, accountTypes);
            AccountData data = new AccountData(a[i].name,ad);
            mAccounts.add(data);
        }
        mAccountAdapter.notifyDataSetChanged();
    }

    private static AuthenticatorDescription getAuthenticatorDescription(String type, AuthenticatorDescription[] dictionary){
        for(int i = 0; i<dictionary.length; i++){
            if(dictionary[i].type.equals(type)){
                return dictionary[i];
            }
        }
        throw new RuntimeException("Unable to find matching authenticator");
    }

    private void updateAccountSelection(){
        mSelectedAccount = (AccountData) mAccountSpinner.getSelectedItem();
    }

    private class AccountData {
        private String mName;
        private String mType;
        private CharSequence mTypeLabel;
        private Drawable mIcon;

        public AccountData(String name, AuthenticatorDescription description) {
            mName = name;
            if(description != null){
                mType = description.type;

                String packageName = description.packageName;
                PackageManager pm = getPackageManager();

                if(description.labelId != 0){
                    mTypeLabel = pm.getText(packageName,description.labelId,null);
                    if(mTypeLabel == null){
                        throw new IllegalArgumentException("LabelID provided, but label not found");
                    }
                }else{
                    mTypeLabel = "";
                }
                if(description.iconId != 0){
                    mIcon = pm.getDrawable(packageName, description.iconId, null);
                    if(mIcon == null){
                        throw new IllegalArgumentException("Icon provided, but drawable not found");
                    }
                }else{
                    mIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
                }


            }
        }

        public String getType() {
            return mType;
        }

        public String getName() {

            return mName;
        }
        public CharSequence getTypeLabel(){
            return mTypeLabel;
        }

        public Drawable getIcon(){return mIcon;}

        public String toString(){return mName;}


    }

}
