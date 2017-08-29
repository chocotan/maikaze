package io.loli.maikaze.kancolle

import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.protocol.HttpContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties
import org.springframework.cloud.netflix.zuul.filters.rout.SimpleHostRoutingFilter
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap

import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.stream.Collectors

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.HTTPS_SCHEME
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.HTTP_SCHEME
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER;

/**
 * Created by uzuma on 2017/8/26.
 */
@Component
public class KcsProxyHostRoutingFilter extends SimpleHostRoutingFilter {

    private KancolleProperties kancolleProperties;

    @Override
    public int filterOrder() {
        return SIMPLE_HOST_ROUTING_FILTER_ORDER;
    }

    KcsProxyHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties properties,
                              KancolleProperties kancolleProperties) {
        super(helper, properties)
        this.kancolleProperties = kancolleProperties

    }


    @Autowired
    private HttpSession session;

    @Override
    protected void preProcessHeader(MultiValueMap<String, String> headers, HttpServletRequest request) {
        def loginContext = session.getAttribute("lctx")
        String ip = loginContext.$5_world_ip
        headers.set("Origin", "http://$ip/");
        headers.set('X-Requested-With', 'ShockwaveFlash/18.0.0.232')
        def referer = request.getHeader("Referer") ? request.getHeader("Referer") : "http://www.dmm.com/netgame/social/-/gadgets/=/app_id=854854/"
        headers.set("Referer", referer.replace(request.getRemoteHost(), ip).replace("https", "http"))
        headers.remove("Cookie")
    }

    protected String preProcessUri(MultiValueMap<String, String> headers, HttpServletRequest request, String originUri) {
        if (originUri.contains("kcs/resources/image/world/") && originUri.endsWith(".png")) {
            def host = request.getRemoteHost();
            def toReplace = Arrays.stream(host.split("\\.")).mapToInt({ Integer.parseInt(it) })
                    .mapToObj({ String.format("%03d", it) })
                    .collect(Collectors.joining("_"))

            def loginContext = session.getAttribute("lctx")
            String ip = loginContext.$5_world_ip
            def newUrl = Arrays.stream(ip.split("\\.")).mapToInt({ Integer.parseInt(it) })
                    .mapToObj({ String.format("%03d", it) })
                    .collect(Collectors.joining("_"))
            return originUri.replace(toReplace, newUrl);
        }


        return super.preProcessUri(headers, request, originUri);
    }


    protected PoolingHttpClientConnectionManager newConnectionManager() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, (X509TrustManager[]) [new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates,
                                               String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates,
                                               String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }], new SecureRandom());

            RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
                    .<ConnectionSocketFactory> create()
                    .register(HTTP_SCHEME, new PlainConnectionSocketFactory() {
                @Override
                public Socket createSocket(HttpContext context) throws IOException {
                    if (kancolleProperties.proxy) {
                        def proxy = new Proxy(Proxy.Type.valueOf(kancolleProperties.proxyType), new InetSocketAddress(kancolleProperties.proxyIp, kancolleProperties.proxyPort))

                        return new Socket(proxy)
                    }
                    return new Socket();
                }
            });
            if (super.sslHostnameValidationEnabled) {
                registryBuilder.register(HTTPS_SCHEME,
                        new SSLConnectionSocketFactory(sslContext));
            } else {
                registryBuilder.register(HTTPS_SCHEME, new SSLConnectionSocketFactory(
                        sslContext, NoopHostnameVerifier.INSTANCE));
            }
            final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

            super.connectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null,
                    hostProperties.getTimeToLive(), super.hostProperties.getTimeUnit());
            super.connectionManager
                    .setMaxTotal(super.hostProperties.getMaxTotalConnections());
            super.connectionManager.setDefaultMaxPerRoute(
                    super.hostProperties.getMaxPerRouteConnections());
            return super.connectionManager;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}