package org.eclipse.leshan.server.demo.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.leshan.server.demo.serializers.UserDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by Jon Ayerdi on 15/01/2017.
 */
public class UsersServlet extends HttpServlet {

    public static final String USERS_FILE = "users.txt";

    private static final Logger LOG = LoggerFactory.getLogger(UsersServlet.class);
    private Map<String,String> users;

    private final Gson gson;

    public UsersServlet() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(HashMap.class, new UserDeserializer());
        gson = gsonBuilder.create();
        loadUsers(USERS_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        changeUsers(req.getParameter("d"));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        changeUsers(req.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public void changeUsers(String json) {
        writeUsers(USERS_FILE,json);
        loadUsers(USERS_FILE);
    }

    public void writeUsers(String file, String json) {
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(json.getBytes());
            f.close();
        } catch (Exception e) {
            LOG.warn("Exception writing users",e);
        }
    }

    public void loadUsers(String file) {
        try {
            File f = new File(file);
            f.createNewFile();
            Scanner in = new Scanner(f);
            String json = "";
            while(in.hasNext())
                json += in.nextLine();
            in.close();
            users = gson.fromJson(json,HashMap.class);
        } catch (Exception e) {
            LOG.warn("Exception loading users",e);
        }
    }

    public Map<String,String> getUsers() {
        return users;
    }

}
