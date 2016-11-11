package com.shudas.rewind.webapp;

import com.google.inject.servlet.GuiceFilter;
import com.shudas.rewind.commons.Properties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Slf4j
public class ServerMain {
    public static void main(String[] args) throws Exception {
        log.info("Starting Server");
        Server server = new Server(Properties.PROP_SERVER_PORT);
        ServletContextHandler root = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        root.addEventListener(new WebServerContextListener());
        root.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        server.start();
        server.join();
    }
}