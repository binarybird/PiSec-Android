package com.status.pisec;

/*
http://nelenkov.blogspot.fr/2012/05/storing-application-secrets-in-androids.html
 */
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.os.Bundle;


import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.status.pisec.status.StatusModel;
import com.status.pisec.status.StatusXMLParser;
import com.status.pisec.sync.PiSecStatusContentProvider;
import com.status.pisec.util.Constants;
import com.status.pisec.util.enums.AlarmState;



public class PiSecActivity extends AppCompatActivity {


    Button refresh = null;
    ContentObserver obs = null;
    Context thisContext = null;
    TextView uimanager_message;
    TextView uimanager_wifi;
    TextView uimanager_winopen;

    TextView alarmmanager_failedlogins;
    TextView alarmmanager_mode;
    TextView alarmmanager_state;

    TextView webservice_running;
    TextView webservice_location;
    TextView webservice_port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi_sec);

        thisContext = getApplicationContext();
        refresh = (Button) findViewById(R.id.refresh);
        uimanager_message=((TextView)findViewById(R.id.uimanager_message));
        uimanager_wifi=((TextView)findViewById(R.id.uimanager_wifi));
        uimanager_winopen=((TextView)findViewById(R.id.uimanager_winopen));

        alarmmanager_failedlogins=((TextView)findViewById(R.id.alarmmanager_failedlogins));
        alarmmanager_mode=((TextView)findViewById(R.id.alarmmanager_mode));
        alarmmanager_state =((TextView)findViewById(R.id.alarmmanager_state));

        webservice_location=((TextView)findViewById(R.id.webservice_location));
        webservice_port=((TextView)findViewById(R.id.webservice_port));
        webservice_running=((TextView)findViewById(R.id.webservice_running));


        boolean aBoolean = PreferenceManager.getDefaultSharedPreferences(PiSecActivity.this).getBoolean(Constants.PREF_SETUP_COMPLETE, false);
        if (!aBoolean) {
            startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
        }else {
            appSetUp();
        }
    }

    private void appSetUp(){
        //keep the db small - get rid of old entries
        try {
            Uri parse = Uri.parse(PiSecStatusContentProvider.URL);
            int delete = getContentResolver().delete(parse, "1", null);
            Log.i("CHANGE", "Deleted " + delete + " at " + parse);
        }catch(Exception e){}

        if(obs == null)
            obs = getContentObserver();

        getContentResolver().registerContentObserver(PiSecStatusContentProvider.CONTENT_URI, true, obs);

        Account account = getAccount();
        if(account != null)
            ContentResolver.addPeriodicSync(account, PiSecStatusContentProvider.CONTENT_AUTHORITY, Bundle.EMPTY, Constants.SYNC_FREQUENCY);
        onClickRefresh(null);
        //ContentResolver.setSyncAutomatically(accnt, PiSecStatusContentProvider.CONTENT_AUTHORITY, true);
    }



    @Override
    protected void onResume(){
        super.onResume();
        boolean aBoolean = PreferenceManager.getDefaultSharedPreferences(PiSecActivity.this).getBoolean(Constants.PREF_SETUP_COMPLETE, false);
        if(aBoolean){
            if(obs == null)
                obs = getContentObserver();
        }
    }


    public Account getAccount(){
        Account[] accountsByType = AccountManager.get(getApplicationContext()).getAccountsByType(Constants.ACCOUNT_TYPE);
        Account accnt = null;
        if(accountsByType.length == 1){
            accnt = accountsByType[0];
        }
        return accnt;
    }

    public void onClickRefresh(View v){

        Account accnt = getAccount();
        if(accnt != null) {
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            ContentResolver.requestSync(accnt, PiSecStatusContentProvider.CONTENT_AUTHORITY, settingsBundle);
        }
    }

    private ContentObserver getContentObserver(){
        return new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                Cursor cursor = getContentResolver().query(PiSecStatusContentProvider.CONTENT_URI, null, null, null, null);

                String get = "";
                long newContentrow = ContentUris.parseId(uri);

                if (cursor.moveToFirst()) {
                    do {
                        if(cursor.getLong(0) == newContentrow)
                            get=cursor.getString(cursor.getColumnIndex(PiSecStatusContentProvider.status_col));
                    } while (cursor.moveToNext());
                }

                refreshUI(get);

            }
        };
    }

    private void refreshUI(String rawXML){
        final StatusXMLParser parser = new StatusXMLParser();
        final StatusModel statusModel = parser.parse(rawXML);

        if(uimanager_message!=null)
         uimanager_message.setText(statusModel.getUiManager().getMessage()+"");
        if(uimanager_wifi!=null)
         uimanager_wifi.setText(statusModel.getUiManager().getWifiSignal()+"");
        if(uimanager_winopen!=null)
         uimanager_winopen.setText(statusModel.getUiManager().getWindowsOpen()+"");

        if(alarmmanager_failedlogins!=null)
         alarmmanager_failedlogins.setText(statusModel.getAlarmManager().getFailedLogins()+"");
        if(alarmmanager_mode!=null)
         alarmmanager_mode.setText(statusModel.getAlarmManager().getMode().toString()+"");
        if(alarmmanager_state!=null)
         alarmmanager_state.setText(statusModel.getAlarmManager().getState().toString()+"");

        if(webservice_running!=null)
         webservice_running.setText(statusModel.getWebService().getRunning()+"");
        if(webservice_location!=null)
         webservice_location.setText(statusModel.getWebService().getLocation()+"");
        if(webservice_port!=null)
         webservice_port.setText(statusModel.getWebService().getPort()+"");

        if(statusModel.getAlarmManager().getState() == AlarmState.ALARMED){
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setContentInfo("PiSec Alarm Triggered!");
            builder.setContentText("An alarm has been triggered from the associated SSH account!");
            builder.setContentTitle("PiSec Alarm Triggered!");
            builder.setSmallIcon(R.mipmap.ic_launcher);

            Intent resultIntent = new Intent(this, PiSecActivity.class);//TODO - pressing the notification results in crash
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(PiSecActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(1337, builder.build());

        }

    }


}

