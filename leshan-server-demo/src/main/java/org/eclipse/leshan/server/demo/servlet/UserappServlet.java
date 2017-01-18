package org.eclipse.leshan.server.demo.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.LinkObject;
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
 * Created by Jon Ayerdi on 15/01/2017.
 */
public class UserappServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ClientServlet.class);

    private final Gson gson;

    LwM2mServer server;
    UsersServlet usersServlet;
    ObserveServlet observeServlet;

    public UserappServlet(LwM2mServer s, UsersServlet us, ObserveServlet os) {
        server = s;
        usersServlet = us;
        observeServlet = os;

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
        // Allow external AJAX calls
        resp.setHeader("Access-Control-Allow-Origin","*");
        // /api/lights
        String user = req.getParameter("user");
        String password = req.getParameter("password");
        String pwd = usersServlet.getUsers().get(user);
        if(pwd==null || !pwd.equals(password)) {
            resp.setStatus(401);
            return;
        }
        //Fetch light endpoints
        Collection<Client> clients = server.getClientRegistry().allClients();
        ArrayList<ClientFormat> lights = new ArrayList<>();
        for(Client c : clients) {
            LinkObject[] objs = c.getObjectLinks();
            for(int i = 0 ; i < objs.length ; i++) {
                if(objs[i].getUrl().equals("/10250/0")) {
                    //Add to lights list if user is owner
                    String owner = observeServlet.getOwners().get(c.getEndpoint());
                    if(owner!=null && owner.equals(user)) {
                        ClientFormat cf = ClientFormat.create(c.getEndpoint());
                        lights.add(cf);
                    }
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Allow external AJAX calls
        resp.setHeader("Access-Control-Allow-Origin","*");
        // /api/lights
        String user = req.getParameter("user");
        String password = req.getParameter("password");
        String pwd = usersServlet.getUsers().get(user);
        if(pwd==null || !pwd.equals(password)) {
            resp.setStatus(401);
            return;
        }
        //Fetch light endpoints
        Collection<Client> clients = server.getClientRegistry().allClients();
        ArrayList<ClientFormat> lights = new ArrayList<>();
        for(Client c : clients) {
            LinkObject[] objs = c.getObjectLinks();
            for(int i = 0 ; i < objs.length ; i++) {
                if(objs[i].getUrl().equals("/10250/0")) {
                    //Add to lights list if user is owner
                    String owner = observeServlet.getOwners().get(c.getEndpoint());
                    if(owner!=null && owner.equals(user)) {
                        ClientFormat cf = ClientFormat.create(c.getEndpoint());
                        lights.add(cf);
                    }
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

}
