package com.shudas.rewind.webapp;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simply log the inbound and outbound request
 */
@Slf4j
@Singleton
public class LoggingFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String msg = String.format("%s %s [%s] \"%s %s %s\" %d %d",
                request.getRemoteAddr(),
                request.getRemoteUser(),
                getCurrentTime(),
                request.getMethod(),
                getFullURL(request),
                request.getProtocol(),
                response.getStatus(),
                response.getBufferSize());
        log.info(msg);

        filterChain.doFilter(servletRequest, servletResponse);

        msg = String.format("%s %s [%s] \"%s %s %s\" %d %d",
                request.getRemoteAddr(),
                request.getRemoteUser(),
                getCurrentTime(),
                request.getMethod(),
                getFullURL(request),
                request.getProtocol(),
                response.getStatus(),
                response.getBufferSize());
        log.info(msg);
    }

    private String getCurrentTime() {
        String format = "dd/MMM/yyyy:HH:mm:ss %Z";
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat(format);
        return dateFormatGmt.format(new Date());
    }

    private static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    public void destroy() {}
}