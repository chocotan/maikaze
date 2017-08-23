package io.loli.maikaze.kancolle

import org.apache.http.HttpHost
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.utils.URIUtils;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.SessionScope

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by uzuma on 2017/8/20.
 */
@WebServlet(urlPatterns = "/kcsapi/*")
public class KancolleProxyServlet extends ProxyServlet {


    @Autowired
    private LoginContext context;

    protected String getTargetUri(HttpServletRequest servletRequest) {
        def requestUrl = servletRequest.getRequestURI();
        return "http://" + context.$5_world_ip;
    }

    protected void initTarget() throws ServletException {

    }

    protected HttpHost getTargetHost(HttpServletRequest servletRequest) {
        return HttpHost.create("http://" + context.$5_world_ip)
    }
}
