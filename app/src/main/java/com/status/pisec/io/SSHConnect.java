package com.status.pisec.io;

import android.os.StrictMode;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.status.pisec.util.Constants;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jamesrichardson on 10/28/15.
 */
public class SSHConnect {
    private static int tunnelport = Constants.TUNNELPORT;
    private static Session session = null;
    private static HttpTransportSE androidHttpTransport = null;
    private static boolean sshSessionReset = false;
    private static boolean soapSessionReset = false;
    private static Timer timer;
    private static JSch jsch = new JSch();

    public static String getPiSecStatus(final String sshPassword, final String sshUserName, final String sshPort, final String sshIp, final String pisecIp, final String pisecPort){

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if(session == null || !session.isConnected() || sshSessionReset)
            sshSessionReset=false;
            try {
                if(session != null){
                    Log.i("TOPERR","START");
                    //try {session.delPortForwardingL(pisecIp, tunnelport);}catch(Exception e){Log.i("TOPERR","1");e.printStackTrace();}
                    try {session.delPortForwardingL(tunnelport);}catch(Exception e){Log.i("TOPERR","2");e.printStackTrace();}
                    try {session.delPortForwardingR(Integer.valueOf(pisecPort));}catch(Exception e){Log.i("TOPERR","3");e.printStackTrace();}
                    try {session.delPortForwardingR(pisecIp, Integer.valueOf(pisecPort));}catch(Exception e){Log.i("TOPERR","4");e.printStackTrace();}
                    session.disconnect();
                    session = null;
                    Log.i("TOPERR","STOP");
                }
                session = jsch.getSession(sshUserName, sshIp, Integer.valueOf(sshPort));

                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);

                session.setPassword(sshPassword);

                //lport,rhost,rport
                session.setPortForwardingL(tunnelport, pisecIp, Integer.valueOf(pisecPort));

                session.connect();

                if(timer != null){
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            session.sendKeepAliveMsg();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask, 0, 45000);

            } catch (JSchException e) {
                e.printStackTrace();
                sshSessionReset = true;
                //tunnelport+=1;
            }


        try {
            if(!sshSessionReset) {
                if (androidHttpTransport == null || androidHttpTransport.getServiceConnection() == null || soapSessionReset) {
                    soapSessionReset=false;
                    androidHttpTransport = new HttpTransportSE(Constants.URL);
                }
                SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.METHOD_NAME);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);

                androidHttpTransport.call(Constants.SOAP_ACTION, envelope);

                SoapPrimitive resultsRequestSOAP = (SoapPrimitive) envelope.getResponse();

                String res = resultsRequestSOAP.toString();

                return res;
            }

        } catch (Exception e) {
            soapSessionReset = true;
            e.printStackTrace();
        }

        return null;
    }
}

