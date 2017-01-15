package org.eclipse.leshan.server.demo.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.ResponseCode;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.exception.RequestFailedException;
import org.eclipse.leshan.core.request.exception.ResourceAccessException;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.demo.serializers.ClientFormat;
import org.eclipse.leshan.server.demo.serializers.ClientFormatSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jon Ayerdi on 13/01/2017.
 */
public class SensorServlet extends HttpServlet {

    private static final String[] SENSOR_RESOURCES = {"Sensor ID","Device Type","Sensor State","User ID "
            ,"Group No","Location X","Location Y","Room ID"};

    private static final Logger LOG = LoggerFactory.getLogger(ClientServlet.class);

    private static final long TIMEOUT = 5000; // ms

    private static final long serialVersionUID = 1L;

    private final LwM2mServer server;

    private final Gson gson;

    public SensorServlet(LwM2mServer server) {
        this.server = server;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(ClientFormat.class, new ClientFormatSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // /api/sensors
            if (req.getPathInfo() == null) {
                // all registered sensors
                getSensors(req, resp);
                return;
            }
            // ?
            String[] path = StringUtils.split(req.getPathInfo(), '/');
            if (path.length < 1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }
            String clientEndpoint = path[0];
            Client client = server.getClientRegistry().get(clientEndpoint);
            if (client != null) {
                // /api/Sensors/<sensor_endpoint>
                if (path.length == 1) {
                    getSensorData(req, resp, client);
                    return;
                }
                // /api/Sensors/<Sensor_endpoint>/<resource_id>
                int rid = 0;
                try {
                    rid = Integer.valueOf(path[1]);
                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().append("Resource id must be an integer").flush();
                    return;
                }
                if (path.length == 2) {
                    getSensorData(req, resp, client, rid);
                    return;
                }
                // /api/Sensors/<Sensor_endpoint>/<resource_id>/set
                if (path.length == 3 && path[2].equals("set")) {
                    setSensorData(req, resp, client, rid);
                    return;
                }
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().append("Invalid URL").flush();
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
            }
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid request", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (ResourceAccessException | RequestFailedException e) {
            LOG.warn(String.format("Error accessing resource %s%s.", req.getServletPath(), req.getPathInfo()), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (InterruptedException e) {
            LOG.warn("Thread Interrupted", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append(e.getMessage()).flush();
        } catch (Exception e) {
            LOG.warn("Exception: " + e.getClass().getName());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Exception: " + e.getClass().getName()).flush();
        }
    }

    // /api/sensors
    //Get the list of registered Sensor devices
    public void getSensors(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Collection<Client> clients = server.getClientRegistry().allClients();
        ArrayList<ClientFormat> sensors = new ArrayList<>();
        for(Client c : clients) {
            LinkObject[] objs = c.getObjectLinks();
            for(int i = 0 ; i < objs.length ; i++) {
                if(objs[i].getUrl().equals("/10350/0")) {
                    //Add to sensors list
                    ClientFormat cf = ClientFormat.create(getSensorResource(c,0),c.getEndpoint());
                    sensors.add(cf);
                    break;
                }
            }
        }
        //format list to json and send
        String json = this.gson.toJson(sensors.toArray(new ClientFormat[] {}));
        resp.setContentType("application/json");
        resp.getOutputStream().write(json.getBytes("UTF-8"));
        resp.setStatus(HttpServletResponse.SC_OK);
        return;
    }

    // /api/Sensors/<sensor_endpoint>
    // Get all the LWM2M Sensor data from sensor_endpoint
    public void getSensorData(HttpServletRequest req, HttpServletResponse resp, Client client) throws Exception {
        resp.setContentType("application/json");
        String json = "{";
        for(int i = 0 ; i < 8 ; i++) {
            json += "\"";
            json += SENSOR_RESOURCES[i];
            json += "\":";
            json += "\"";
            json += getSensorResource(client,i);
            json += "\"";
            if(i!=7) json += ",";
        }
        json += "}";
        resp.getOutputStream().write(json.getBytes("UTF-8"));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // /api/Sensors/<sensor_endpoint>/<resource_id>
    // Get the LWM2M sensor data from Sensor_endpoint
    public void getSensorData(HttpServletRequest req, HttpServletResponse resp, Client client, int rid) throws Exception {
        resp.setContentType("application/json");
        String json = "{";
        json += "\"";
        json += SENSOR_RESOURCES[rid];
        json += "\":";
        json += "\"";
        json += getSensorResource(client,rid);
        json += "\"";
        json += "}";
        resp.getOutputStream().write(json.getBytes("UTF-8"));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // /api/sensors/<sensor_endpoint>/<resource_id>/set
    // Set the LWM2M sensor data from sensor_endpoint
    public void setSensorData(HttpServletRequest req, HttpServletResponse resp, Client client, int rid) throws Exception {
        String value = req.getParameter("value");
        if(value==null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append("No value attribute").flush();
            return;
        }
        if(rid<2 || rid>7) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append("Cannot write to resource, bad id").flush();
            return;
        }
        setSensorResource(client,rid,value);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public String getSensorResource(Client c, int rid) throws Exception {
        // create & process request for resource
        ReadRequest request = new ReadRequest(null, "/10350/0/"+rid);
        ReadResponse cResponse = server.send(c, request, TIMEOUT);
        String val = "";
        if(cResponse!=null) val = ((LwM2mResource)cResponse.getContent()).getValue().toString();
        return val;
    }

    public void setSensorResource(Client c, int rid, String value) throws Exception {
        // create & process request for resource
        WriteRequest request = new WriteRequest(WriteRequest.Mode.REPLACE, null
                , "/10350/0/"+rid, cast(rid,value));
        WriteResponse cResponse = server.send(c, request, TIMEOUT);
        if(cResponse==null || cResponse.getCode()!= ResponseCode.CHANGED)
            throw new RequestFailedException("Request failed");
    }

    public LwM2mSingleResource cast(int rid, String value) throws Exception {
        switch(rid) {
            case 4:
                return LwM2mSingleResource.newIntegerResource(rid, Integer.valueOf(value));
            case 5:
                return LwM2mSingleResource.newFloatResource(rid, Float.valueOf(value));
            case 6:
                return LwM2mSingleResource.newFloatResource(rid, Float.valueOf(value));
            default:
                return LwM2mSingleResource.newStringResource(rid, value);
        }
    }

}
