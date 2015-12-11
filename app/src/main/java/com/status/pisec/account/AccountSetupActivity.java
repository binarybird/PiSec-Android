package com.status.pisec.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.status.pisec.R;
import com.status.pisec.io.SSHConnect;
import com.status.pisec.sync.PiSecStatusContentProvider;
import com.status.pisec.util.AESCrypt;
import com.status.pisec.util.Constants;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.crypto.SecretKey;

/**
 * Created by binar on 10/27/2015.
 */
public class AccountSetupActivity extends AccountAuthenticatorActivity {

    EditText sshUserNameField = null;
    EditText sshPasswordField = null;
    EditText sshPortField = null;
    EditText sshIpField = null;
    EditText pisecIpField =null;
    EditText pisecPortField = null;
    Button ok = null;
    Button cancel = null;

    @Override
    protected void onCreate(Bundle icicle) {
        // TODO Auto-generated method stub
        super.onCreate(icicle);
        this.setContentView(R.layout.account_setup_activity);

        sshUserNameField = (EditText) this.findViewById(R.id.sshUserField);
        sshPasswordField = (EditText) this.findViewById(R.id.sshPasswordField);
        sshPortField = (EditText) this.findViewById(R.id.sshPortField);
        sshIpField = (EditText) this.findViewById(R.id.sshIpField);
        pisecIpField = (EditText) this.findViewById(R.id.pisecIpField);
        pisecPortField = (EditText) this.findViewById(R.id.pisecPortField);
        ok = (Button) this.findViewById(R.id.okButton);
        cancel = (Button) this.findViewById(R.id.cancelButton);

        sshUserNameField.setText("jamesrichardson");
        sshPasswordField.setText("Macbookprog5)!(@");
        sshPortField.setText("22");
        sshIpField.setText("192.168.241.232");
        pisecIpField.setText("192.168.241.232");
        pisecPortField.setText("3579");

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick(v);
            }
        });

    }

    public void onCancelClick(View v) {
        this.setResult(RESULT_CANCELED);
        this.finish();
    }

    public void onSaveClick(View v) {

        final String sshUserName = sshUserNameField.getText().toString();
        final String sshPassword = sshPasswordField.getText().toString();
        final String sshPort = sshPortField.getText().toString();
        final String sshIp = sshIpField.getText().toString();
        final String pisecIp = pisecIpField.getText().toString();
        final String pisecPort = pisecPortField.getText().toString();

        boolean success = testConnection(sshPassword, sshUserName, sshPort, sshIp, pisecIp, pisecPort);

        if(success) {
            onSucceussConnection(sshPassword, sshUserName, sshPort, sshIp, pisecIp, pisecPort);
        }else{
            this.setResult(RESULT_CANCELED);
            this.finish();
        }

    }

    private boolean testConnection(String sshPassword, String sshUserName,String sshPort,String sshIp,String pisecIp,String pisecPort){

        String res = SSHConnect.getPiSecStatus(sshPassword, sshUserName, sshPort, sshIp, pisecIp, pisecPort);
        if (res != null && res.contains("status")) {
            return true;
        }
        return false;
    }

    private void onSucceussConnection(String sshPassword, String sshUserName,String sshPort,String sshIp,String pisecIp,String pisecPort ){
        AccountManager accMgr = AccountManager.get(this);

        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putBoolean(Constants.PREF_SETUP_COMPLETE, true).commit();

        String e_sshPassword = "";
        String e_sshPort = "";
        String e_sshIp = "";
        String e_pisecIp = "";
        String e_pisecPort = "";
        try {
            e_sshPassword = AESCrypt.encrypt(Constants.k, sshPassword);
            e_sshPort = AESCrypt.encrypt(Constants.k, sshPort);
            e_sshIp = AESCrypt.encrypt(Constants.k, sshIp);
            e_pisecIp = AESCrypt.encrypt(Constants.k, pisecIp);
            e_pisecPort = AESCrypt.encrypt(Constants.k, pisecPort);
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }

        // This is the magic that addes the account to the Android Account Manager
        final Account account = new Account(sshUserName, Constants.ACCOUNT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SSHIP,e_sshIp);
        bundle.putString(Constants.SSHPORT,e_sshPort);
        bundle.putString(Constants.PISECIP,e_pisecIp);
        bundle.putString(Constants.PISECPORT,e_pisecPort);

        accMgr.addAccountExplicitly(account,e_sshPassword, bundle);

        //Sync stuff
        ContentResolver.setIsSyncable(account, PiSecStatusContentProvider.CONTENT_AUTHORITY, 1);
        // Inform the system that this account is eligible for auto sync when the network is up
        ContentResolver.setSyncAutomatically(account, PiSecStatusContentProvider.CONTENT_AUTHORITY, true);
        // Recommend a schedule for automatic synchronization. The system may modify this based
        // on other scheduled syncs and network utilization.
        ContentResolver.addPeriodicSync(account, PiSecStatusContentProvider.CONTENT_AUTHORITY, new Bundle(), Constants.SYNC_FREQUENCY);

        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(account, PiSecStatusContentProvider.CONTENT_AUTHORITY, b);

        //this or Context context = this.getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putBoolean(Constants.PREF_SETUP_COMPLETE, true).commit();


        // Now we tell our caller, could be the Andreoid Account Manager or even our own application
        // that the process was successful
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, sshUserName);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, Constants.ACCOUNT_TYPE);
        this.setAccountAuthenticatorResult(intent.getExtras());
        this.setResult(RESULT_OK, intent);
        this.finish();
    }
}
//for editing the account
//try {
//        Account account = getAccount();
//        AccountManager accountManager = AccountManager.get(getApplicationContext());
//final String e_sshPassword = accountManager.getPassword(account);
//final String e_sshPort = accountManager.getUserData(account, Constants.SSHPORT);
//final String e_sshIp = accountManager.getUserData(account, Constants.SSHIP);;
//final String e_pisecIp = accountManager.getUserData(account, Constants.PISECIP);
//final String e_pisecPort = accountManager.getUserData(account, Constants.PISECPORT);
//        sshUserNameField.setText(account.name);
//        sshPasswordField.setText(AESCrypt.decrypt(Constants.k, e_sshPassword));
//        sshPortField.setText(AESCrypt.decrypt(Constants.k, e_sshPort));
//        sshIpField.setText(AESCrypt.decrypt(Constants.k, e_sshIp));
//        pisecIpField.setText(AESCrypt.decrypt(Constants.k, e_pisecIp));
//        pisecPortField.setText(AESCrypt.decrypt(Constants.k, e_pisecPort));
//        }catch (GeneralSecurityException e){
//        e.printStackTrace();
//        }
