package com.status.pisec.status;

import android.net.wifi.WifiConfiguration;

import com.status.pisec.util.enums.AlarmMode;
import com.status.pisec.util.enums.AlarmState;
import com.status.pisec.util.enums.Zone;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by binar on 10/28/2015.
 */
public class StatusModel {

    private WebService webService;
    private AlarmManager alarmManager;
    private UIManager uiManager;

    public StatusModel(){}

    public StatusModel(WebService w, AlarmManager m,UIManager u){
        webService = w;
        alarmManager = m;
        uiManager = u;
    }

    public WebService getWebService(){
        return webService;
    }

    public AlarmManager getAlarmManager(){
        return alarmManager;
    }

    public UIManager getUiManager(){
        return uiManager;
    }

    public class WebService{
        private boolean running;
        private int port;
        private String location;
        public WebService(boolean running,int port, String location){
            this.running = running;
            this.port=port;
            this.location=location;
        }
        public boolean getRunning(){
            return running;
        }
        public int getPort(){
            return port;
        }
        public String getLocation(){
            return location;
        }

    }
    public class AlarmManager{
        private AlarmState state;
        private AlarmMode mode;
        private int failedLogins;
        private ArrayList<Alarm> alarms;
        public AlarmManager(AlarmState state,AlarmMode mode,int failedLogins,ArrayList<Alarm> alarms){
            this.alarms = alarms;
            this.state=state;
            this.mode=mode;
            this.failedLogins=failedLogins;
        }
        public AlarmState getState(){
            return state;
        }
        public AlarmMode getMode(){
            return mode;
        }
        public int getFailedLogins(){
            return failedLogins;
        }
        private ArrayList<Alarm> getAlarms(){
            return alarms;
        }

    }
    public class UIManager{
        private int windowsOpen;
        private String message;
        private int wifiSignal;
        public UIManager(int windowsOpen, String message,int wifiSignal){
            this.windowsOpen=windowsOpen;
            this.message=message;
            this.wifiSignal=wifiSignal;
        }
        public int getWindowsOpen(){
            return windowsOpen;
        }
        public String getMessage(){
            return message;
        }
        public int getWifiSignal(){
            return wifiSignal;
        }

    }
    public class Alarm {
        private String alarmDate = null;
        private String message = null;
        private Zone zone = Zone.NONE;
        private String pictureLocation = "";

        public Alarm(String message,String date, Zone zone, String imageSaveLocation){
            this.alarmDate = date;
            this.message = message;
            this.zone = zone;
            this.pictureLocation = imageSaveLocation;
        }
        public String getDate(){
            return alarmDate;
        }
        public String getMessage(){
            return message;
        }
        public Zone getZone(){
            return zone;
        }
        public String getPictureLocation(){return pictureLocation;}

        @Override
        public String toString(){
            return Alarm.class.getName()+"[Date:\""+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(alarmDate)+"\", Message:\""+message+"\", Zone:\""+zone.toString()+"\", Images:\""+pictureLocation+"\"]";
        }
    }
}
