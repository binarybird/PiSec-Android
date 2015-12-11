package com.status.pisec.util;

/**
 * Created by binar on 10/27/2015.
 */
public class Constants {

    /*TODO - actually secure this hard coded key - android keystore stops and encrypts itself
    *        when phone not active: password/pattern screen is displayed to unlock.
    *        Android keystore is not ideal to use for constant sync updates
    * */
    public static final String k = "klgl$JRMFDKLkr4l09i34#$%gnlMdjGgyhy6H5h46H56hhge343rrdf";//better then nothing at all :(

    public static final String ACCOUNT_TYPE = "com.status.pisec.ssh.account";
    public static final String PREF_SETUP_COMPLETE = "com.status.pisec.complete";
    public static final long SYNC_FREQUENCY = 60*5;//60 * 60;  // 1 hour (in seconds)

    public static final String SSHIP = "com.status.pisec.sship";
    public static final String SSHPORT = "com.status.pisec.sshport";
    public static final String PISECIP = "com.status.pisec.pisecip";
    public static final String PISECPORT = "com.status.pisec.pisecport";

    public static final int TUNNELPORT = 8763;
    public static final String NAMESPACE = "http://jaxws.io.pisec/";
    public static String URL = "http://localhost:"+TUNNELPORT+"/status?wsdl";
    public static final String METHOD_NAME = "getStatus";
    public static final String SOAP_ACTION =  "http://jaxws.io.pisec/status";


}
