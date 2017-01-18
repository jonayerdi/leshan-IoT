package org.eclipse.leshan.server.demo.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jon Ayerdi on 15/01/2017.
 */
public class UserappServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ClientServlet.class);

    UsersServlet usersServlet;
    ObserveServlet observeServlet;

    public UserappServlet(UsersServlet us, ObserveServlet os) {
        usersServlet = us;
        observeServlet = os;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

}
