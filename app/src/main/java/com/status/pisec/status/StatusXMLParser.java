package com.status.pisec.status;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.status.pisec.status.StatusModel.WebService;
import com.status.pisec.status.StatusModel.UIManager;
import com.status.pisec.status.StatusModel.AlarmManager;
import com.status.pisec.status.StatusModel.Alarm;
import com.status.pisec.util.enums.AlarmMode;
import com.status.pisec.util.enums.AlarmState;
import com.status.pisec.util.enums.Zone;

/**
 * Created by binar on 10/28/2015.
 */
public class StatusXMLParser extends StatusModel{
    private Document doc = null;

    public StatusModel parse(String xml){
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            InputSource is = new InputSource(new StringReader(xml));
            Document doc = docBuilder.parse(is);
            this.doc = doc;

            return parse(doc);

        }catch(Exception e){}
        return null;
    }

    private StatusModel parse(Document doc){
        StatusModel m = null;

        NodeList runningElements = doc.getElementsByTagName("running");
        NodeList portElements = doc.getElementsByTagName("port");
        NodeList locationElements = doc.getElementsByTagName("location");
        WebService s = parseWebService(runningElements, portElements, locationElements);

        NodeList stateElements = doc.getElementsByTagName("state");
        NodeList modeElements = doc.getElementsByTagName("mode");
        NodeList failedloginsElements = doc.getElementsByTagName("failedlogins");
        NodeList alarmElements = doc.getElementsByTagName("alarm");
        AlarmManager a = parseAlarmManager(stateElements, modeElements, failedloginsElements, alarmElements);

        NodeList windowsElements = doc.getElementsByTagName("windows");
        NodeList messageElements = doc.getElementsByTagName("message");
        NodeList wifisignalsElements = doc.getElementsByTagName("wifisignal");
        UIManager u = parseUIMnanager(windowsElements, messageElements, wifisignalsElements);

        m = new StatusModel(s,a,u);

        return m;
    }

    private WebService parseWebService(NodeList runningElements,NodeList portElements,NodeList locationElements){
        WebService webService = null;

        boolean r = Boolean.valueOf(getSingle(runningElements));
        int p = Integer.valueOf(getSingle(portElements));
        String l = getSingle(locationElements);

        webService = new WebService(r,p,l);

        return webService;
    }
    private AlarmManager parseAlarmManager(NodeList stateElements,NodeList modeElements,NodeList failedloginsElements,NodeList alarmElements){
        AlarmManager manager = null;

        AlarmState as = AlarmState.valueOf(getSingle(stateElements));
        AlarmMode m = AlarmMode.valueOf(getSingle(modeElements));
        int fl = Integer.valueOf(getSingle(failedloginsElements));
        ArrayList<Alarm> aa = getAlarms(alarmElements);

        manager = new AlarmManager(as,m,fl,aa);

        return manager;
    }
    private UIManager parseUIMnanager(NodeList windowsElements,NodeList messageElements,NodeList wifisignalsElements){
        UIManager manager = null;

        int w = Integer.valueOf(getSingle(windowsElements));
        String m = getSingle(messageElements);
        int wi = Integer.valueOf(getSingle(wifisignalsElements));

        manager = new UIManager(w,m,wi);

        return manager;
    }

    private String getSingle(NodeList list){
        String ret = null;

        if(list.getLength()==1){
            Node node = list.item(0);
            ret = node.getTextContent();
        }

        return ret;
    }

    private ArrayList<Alarm> getAlarms(NodeList alarmNodes){
        ArrayList<Alarm> alarms = new ArrayList<>();
        for(int i=0;i<alarmNodes.getLength();i++){
            Node n = alarmNodes.item(i);
            alarms.add(parseAlarm());
        }
        return alarms;
    }

    private Alarm parseAlarm(){
        Alarm a = null;

        NodeList dates = doc.getElementsByTagName("date");
        NodeList alarmmessages = doc.getElementsByTagName("alarmmessage");
        NodeList zones = doc.getElementsByTagName("zone");
        NodeList picturelocations = doc.getElementsByTagName("picturelocation");

        String date = getSingle(dates);
        String mes = getSingle(alarmmessages);
        Zone z = Zone.valueOf(getSingle(zones));
        String pl = getSingle(picturelocations);

        a = new Alarm(mes,date,z,pl);

        return a;
    }
}
