package io.loli.maikaze.kancolle;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest
import java.util.stream.Collectors;

/**
 * Created by uzuma on 2017/8/20.
 */
@WebServlet("/kcs/*")
public class KancolleImgProxyServlet extends KancolleProxyServlet {

    @Autowired
    LoginContext context;

    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        String requestURI = servletRequest.getRequestURI();
        if (requestURI.endsWith(".png")) {
            StringBuilder uri = new StringBuilder(500);
            uri.append(getTargetUri(servletRequest));
            // Handle the path given to the servlet
            if (servletRequest.getPathInfo() != null) {
                uri.append("/");
                context.$5_world.split("\\.").toList().stream()
                        .mapToInt({ Integer.parseInt(it) })
                        .mapToObj({ String.format("%03d", it) })
                        .collect(Collectors.joining("_"))
                uri.append(encodeUriQuery(servletRequest.getPathInfo()));
            }
            return uri.toString();
        } else {
            return super.rewriteUrlFromRequest(servletRequest);
        }
    }

}
