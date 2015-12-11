package com.status.pisec.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.status.pisec.util.Constants;

/**
 * Created by binar on 10/27/2015.
 */
public class AccountAuthenticationService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return new PiSecSSHAccountAuthenticator(this).getIBinder();
    }

    public class PiSecSSHAccountAuthenticator extends AbstractAccountAuthenticator {
        private Context mContext;

        public PiSecSSHAccountAuthenticator(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            Bundle result;
            Intent intent;

            intent = new Intent(this.mContext, AccountSetupActivity.class);
            intent.putExtra(Constants.ACCOUNT_TYPE, authTokenType);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

            result = new Bundle();
            result.putParcelable(AccountManager.KEY_INTENT, intent);
            return result;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
            // TODO Auto-generated method stub
            System.out.println("confirmCreds");
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            // TODO Auto-generated method stub
            System.out.println("editProperties");
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            // TODO Auto-generated method stub
            System.out.println("getAuthToken");
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            // TODO Auto-generated method stub
            System.out.println("getAuthToken");
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
            // TODO Auto-generated method stub
            System.out.println("hasFeature");
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            // TODO Auto-generated method stub
            System.out.println("updateCreds");
            return null;
        }
    }
}
