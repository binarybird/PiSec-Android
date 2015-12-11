package com.status.pisec.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.status.pisec.io.SSHConnect;
import com.status.pisec.util.AESCrypt;
import com.status.pisec.util.Constants;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.security.GeneralSecurityException;

/**
 * Created by binar on 10/27/2015.
 */
public class PiSecSyncAdapterService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapterImpl sSyncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SERVICE", "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapterImpl(this.getApplicationContext());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        public SyncAdapterImpl(Context context) {
            super(context, true);
            mContext = context;
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

            AccountManager accountManager = AccountManager.get(mContext);
            final String sshUserName = account.name;
            final String e_sshPassword = accountManager.getPassword(account);
            final String e_sshPort = accountManager.getUserData(account, Constants.SSHPORT);
            final String e_sshIp = accountManager.getUserData(account, Constants.SSHIP);
            ;
            final String e_pisecIp = accountManager.getUserData(account, Constants.PISECIP);
            final String e_pisecPort = accountManager.getUserData(account, Constants.PISECPORT);

            String sshPassword = "";
            String sshPort = "";
            String sshIp = "";
            String pisecIp = "";
            String pisecPort = "";

            try {
                sshPassword = AESCrypt.decrypt(Constants.k, e_sshPassword);
                sshPort = AESCrypt.decrypt(Constants.k, e_sshPort);
                sshIp = AESCrypt.decrypt(Constants.k, e_sshIp);
                pisecIp = AESCrypt.decrypt(Constants.k, e_pisecIp);
                pisecPort = AESCrypt.decrypt(Constants.k, e_pisecPort);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

            String res = SSHConnect.getPiSecStatus(sshPassword, sshUserName, sshPort, sshIp, pisecIp, pisecPort);

            if(res != null) {
                ContentValues values = new ContentValues();
                values.put(PiSecStatusContentProvider.status_col, res);


                Uri uri = mContext.getContentResolver().insert(PiSecStatusContentProvider.CONTENT_URI, values);
                mContext.getContentResolver().notifyChange(uri, null, false);
            }else{
                Log.i("SYNC","Result of query is null!");
            }

        }
    }


}
