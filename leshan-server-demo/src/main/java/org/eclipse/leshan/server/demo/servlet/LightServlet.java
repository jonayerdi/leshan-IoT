package org.eclipse.leshan.server.demo.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.exception.RequestFailedException;
import org.eclipse.leshan.core.request.exception.ResourceAccessException;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.demo.serializers.ClientFormat;
import org.eclipse.leshan.server.demo.serializers.LightClientSerializer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeDeserializer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.ResponseSerializer;
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

    private static final Logger LOG = LoggerFactory.getLogger(ClientServlet.class);

    private static final long TIMEOUT = 5000; // ms

    private static final long serialVersionUID = 1L;

    private final LwM2mServer server;

    private final Gson gson;

    public LightServlet(LwM2mServer server) {
        this.server = server;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(ClientFormat.class, new LightClientSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // /api/lights
            if (req.getPathInfo() == null) {
                // all registered lights
                getLights(req, resp);
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
                // /api/lights/<light_endpoint>
                if (path.length == 1) {
                    getLightData(req, resp, client);
                    return;
                }
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
            LOG.warn("Exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append(e.getMessage()).flush();
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
                    ClientFormat cf = ClientFormat.create(getLightResource(c,0),c.getEndpoint());
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

    public String getLightResource(Client c, int rid) throws Exception {
        // create & process request for resource
        ReadRequest request = new ReadRequest(null, "/10250/0/"+rid);
        ReadResponse cResponse = server.send(c, request, TIMEOUT);
        String val = "";
        if(cResponse!=null) val = ((LwM2mResource)cResponse.getContent()).getValue().toString();
        return val;
    }

}
