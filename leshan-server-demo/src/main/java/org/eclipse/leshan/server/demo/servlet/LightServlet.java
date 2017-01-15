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
public class LightServlet extends HttpServlet {

    private static final String[] LIGHT_RESOURCES = {"Light ID","Device Type","Light State","User Type","User ID "
            ,"Light Color","Low Light","Group No","Location X","Location Y","Room ID","Behavior Deployment",};

    private static final Logger LOG = LoggerFactory.getLogger(LightServlet.class);

    private static final long TIMEOUT = 5000; // ms

    private static final long serialVersionUID = 1L;

    private final LwM2mServer server;

    private final Gson gson;

    public LightServlet(LwM2mServer server) {
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
            // Allow external AJAX calls
            resp.setHeader("Access-Control-Allow-Origin","*");
            // /api/lights
            if (req.getPathInfo() == null) {
                // all registered lights
                getLights(req, resp);
                return;
            }
            // ?
            String[] path = StringUtils.split(req.getPathInfo(), '/');
            // /api/lights/
            if (path.length < 1) {
                // all registered lights
                getLights(req, resp);
                return;
            }
            String clientEndpoint = path[0];
            Client client = server.getClientRegistry().get(clientEndpoint);
            if (client != null) {
                // /api/lights/<light_endpoint>
                if (path.length == 1) {
                    getLightData(req, resp, client);
                    return;
                }
                // /api/lights/<light_endpoint>/<resource_id>
                int rid = 0;
                try {
                    rid = Integer.valueOf(path[1]);
                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().append("Resource id must be an integer").flush();
                    return;
                }
                if (path.length == 2) {
                    getLightData(req, resp, client, rid);
                    return;
                }
                // /api/lights/<light_endpoint>/<resource_id>/set
                if (path.length == 3 && path[2].equals("set")) {
                    setLightData(req, resp, client, rid);
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

    // /api/lights
    //Get the list of registered light devices
    public void getLights(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Collection<Client> clients = server.getClientRegistry().allClients();
        ArrayList<ClientFormat> lights = new ArrayList<>();
        for(Client c : clients) {
            LinkObject[] objs = c.getObjectLinks();
            for(int i = 0 ; i < objs.length ; i++) {
                if(objs[i].getUrl().equals("/10250/0")) {
                    //Add to lights list
                    ClientFormat cf = ClientFormat.create(c.getEndpoint());
                    lights.add(cf);
                    break;
                }
            }
        }
        //format list to json and send
        String json = this.gson.toJson(lights.toArray(new ClientFormat[] {}));
        resp.setContentType("application/json");
        resp.getOutputStream().write(json.getBytes("UTF-8"));
        resp.setStatus(HttpServletResponse.SC_OK);
        return;
    }

    // /api/lights/<light_endpoint>
    // Get all the LWM2M light data from light_endpoint
    public void getLightData(HttpServletRequest req, HttpServletResponse resp, Client client) throws Exception {
        resp.setContentType("application/json");
        String json = "{";
        for(int i = 0 ; i < 12 ; i++) {
            json += "\"";
            json += LIGHT_RESOURCES[i];
            json += "\":";
            json += "\"";
            json += getLightResource(client,i);
            json += "\"";
            if(i!=11) json += ",";
        }
        json += "}";
        resp.getOutputStream().write(json.getBytes("UTF-8"));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // /api/lights/<light_endpoint>/<resource_id>
    // Get the LWM2M light data from light_endpoint
    public void getLightData(HttpServletRequest req, HttpServletResponse resp, Client client, int rid) throws Exception {
        resp.setContentType("application/json");
        String json = "{";
        json += "\"";
        json += LIGHT_RESOURCES[rid];
        json += "\":";
        json += "\"";
        json += getLightResource(client,rid);
        json += "\"";
        json += "}";
        resp.getOutputStream().write(json.getBytes("UTF-8"));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // /api/lights/<light_endpoint>/<resource_id>/set
    // Set the LWM2M light data from light_endpoint
    public void setLightData(HttpServletRequest req, HttpServletResponse resp, Client client, int rid) throws Exception {
        String value = req.getParameter("value");
        if(value==null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append("No value attribute").flush();
            return;
        }
        if(rid<2 || rid>13) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append("Cannot write to resource, bad id").flush();
            return;
        }
        setLightResource(client,rid,value);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public String getLightResource(Client c, int rid) throws Exception {
        // create & process request for resource
        ReadRequest request = new ReadRequest(null, "/10250/0/"+rid);
        ReadResponse cResponse = server.send(c, request, TIMEOUT);
        String val = "";
        if(cResponse!=null) val = ((LwM2mResource)cResponse.getContent()).getValue().toString();
        return val;
    }

    public void setLightResource(Client c, int rid, String value) throws Exception {
        // create & process request for resource
        WriteRequest request = new WriteRequest(WriteRequest.Mode.REPLACE, null
                , "/10250/0/"+rid, cast(rid,value));
        WriteResponse cResponse = server.send(c, request, TIMEOUT);
        if(cResponse==null || cResponse.getCode()!= ResponseCode.CHANGED)
            throw new RequestFailedException("Request failed");
    }

    public LwM2mSingleResource cast(int rid, String value) throws Exception {
        switch(rid) {
            case 6:
                return LwM2mSingleResource.newBooleanResource(rid, Boolean.valueOf(value));
            case 7:
                return LwM2mSingleResource.newIntegerResource(rid, Integer.valueOf(value));
            case 8:
                return LwM2mSingleResource.newFloatResource(rid, Float.valueOf(value));
            case 9:
                return LwM2mSingleResource.newFloatResource(rid, Float.valueOf(value));
            default:
                return LwM2mSingleResource.newStringResource(rid, value);
        }
    }

}
