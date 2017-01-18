package org.eclipse.leshan.server.demo.servlet;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.TimestampedLwM2mNode;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.exception.RequestFailedException;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.client.ClientRegistryListener;
import org.eclipse.leshan.server.client.ClientUpdate;
import org.eclipse.leshan.server.demo.serializers.ClientFormat;
import org.eclipse.leshan.server.observation.ObservationRegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Jon Ayerdi on 15/01/2017.
 */
public class ObserveServlet extends HttpServlet implements ClientRegistryListener, ObservationRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClientServlet.class);

    private static final String[] LIGHT_RESOURCES = {"Light ID","Device Type","Light State","User Type","User ID "
            ,"Light Color","Low Light","Group No","Location X","Location Y","Room ID","Behavior Deployment",};
    private static final String[] SENSOR_RESOURCES = {"Sensor ID","Device Type","Sensor State","User ID "
            ,"Group No","Location X","Location Y","Room ID"};

    LeshanServer server;
    Set<String> observers;
    Map<String,String> owners;

    public ObserveServlet(LeshanServer server) {
        this.server = server;
        observers = new HashSet<>();
        owners = Collections.synchronizedMap(new HashMap<String,String>());
        server.getClientRegistry().addListener(this);
        server.getObservationRegistry().addListener(this);
        observers.add("localhost:5434");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String observer = req.getParameter("host");
        if(observer!=null) {
            observers.add(observer);
            LOG.info("Observer ["+observer+"] added");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String observer = req.getParameter("host");
        if(observer!=null) {
            observers.add(observer);
            LOG.info("Observer ["+observer+"] added");
        }
    }

    // Send all the LWM2M light data from light_endpoint
    public void sendLightData(String endpoint, String[] values) throws Exception {
        String url = "/api/observe/light/"+endpoint;
        String json = "{";
        for(int i = 0 ; i < LIGHT_RESOURCES.length ; i++) {
            json += "\"";
            json += LIGHT_RESOURCES[i];
            json += "\":";
            json += "\"";
            json += values[i];
            json += "\"";
            if(i!=11) json += ",";
        }
        json += "}";
        for(String observer : observers)
            sendDataObserver(observer+url,json);
    }

    // Send all the LWM2M sensor data from sensor_endpoint
    public void sendSensorData(String endpoint, String[] values) throws Exception {
        String url = "/api/observe/sensor/"+endpoint;
        String json = "{";
        for(int i = 0 ; i < SENSOR_RESOURCES.length ; i++) {
            json += "\"";
            json += SENSOR_RESOURCES[i];
            json += "\":";
            json += "\"";
            json += values[i];
            json += "\"";
            if(i!=7) json += ",";
        }
        json += "}";
        for(String observer : observers)
            sendDataObserver("http://"+observer+url,json);
    }

    public void sendDataObserver(String url, String json) {
        try {
            LOG.info("Sending update to ["+url+"]");
            URL mUrl = new URL(url);
            HttpURLConnection urlConn = (HttpURLConnection) mUrl.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.addRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Content-Length", Integer.toString(json.length()));
            urlConn.setRequestProperty("charset", "utf-8");
            urlConn.connect();
            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.writeBytes(json);
            dos.flush();
            dos.close();
            urlConn.getResponseCode();
        } catch (Exception e) {
            LOG.warn("Exception in sendDataObserver()",e);
        }
    }

    //LWM2M ClientRegistryListener

    @Override
    public void registered(Client client) {
        try {
            LinkObject[] objs = client.getObjectLinks();
            for(int i = 0 ; i < objs.length ; i++) {
                if(objs[i].getUrl().equals("/10250/0")) {
                    // create & process observe request for resource
                    ObserveRequest request = new ObserveRequest(10250,0);
                    ObserveResponse cResponse = server.send(client, request);
                    //Register owner
                    String owner = ((LwM2mObjectInstance)cResponse.getContent()).getResource(4).getValue().toString();
                    owners.put(client.getEndpoint(),owner);
                    if(cResponse==null)
                        throw new RequestFailedException("10250 observe failed");
                }
                else if(objs[i].getUrl().equals("/10350/0")) {
                    // create & process observe request for resource
                    ObserveRequest request = new ObserveRequest(10350,0);
                    ObserveResponse cResponse = server.send(client, request);
                    if(cResponse==null)
                        throw new RequestFailedException("10350 observe failed");
                }
            }
        } catch (Exception e) {
            LOG.warn("Exception in registered()",e);
        }
    }

    public Map<String,String> getOwners() {
        return owners;
    }

    @Override
    public void updated(ClientUpdate update, Client clientUpdated) {    }

    @Override
    public void unregistered(Client client) {

    }

    //LWM2M ObservationRegistryListener

    @Override
    public void newObservation(Observation observation) {    }

    @Override
    public void cancelled(Observation observation) {    }

    @Override
    public void newValue(Observation observation, LwM2mNode mostRecentValue, List<TimestampedLwM2mNode> timestampedValues) {
        try {
            if(observation.getPath().toString().equals("/10250/0")) {
                String endpoint = server.getClientRegistry().findByRegistrationId(observation.getRegistrationId()).getEndpoint();
                String[] values = new String[LIGHT_RESOURCES.length];
                for(int i = 0 ; i < LIGHT_RESOURCES.length ; i++)
                    values[i] = ((LwM2mObjectInstance)mostRecentValue).getResource(i).getValue().toString();
                //Save new owner of the light
                owners.put(endpoint,values[4]);
                sendLightData(endpoint,values);
            }
            else if(observation.getPath().toString().equals("/10350/0")) {
                String endpoint = server.getClientRegistry().findByRegistrationId(observation.getRegistrationId()).getEndpoint();
                String[] values = new String[SENSOR_RESOURCES.length];
                for(int i = 0 ; i < SENSOR_RESOURCES.length ; i++)
                    values[i] = ((LwM2mObjectInstance)mostRecentValue).getResource(i).getValue().toString();
                sendSensorData(endpoint,values);
            }
        } catch (Exception e) {
            LOG.warn("Exception in newValue()",e);
        }
    }
}
