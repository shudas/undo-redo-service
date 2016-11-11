package com.shudas.rewind.webapp;

import com.google.inject.Module;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;

import javax.servlet.ServletContext;
import java.util.List;

import static java.util.Arrays.asList;

public class WebServerContextListener extends GuiceResteasyBootstrapServletContextListener {
    @Override
    protected List<Module> getModules(ServletContext context)
    {
        return asList(new WebServerModule());
    }
}
