/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.loli.maikaze.kancolle

import com.google.common.io.ByteStreams
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.apache.http.*
import org.apache.http.client.RedirectStrategy
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.*
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.message.BasicHttpRequest
import org.apache.http.protocol.HttpContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.context.environment.EnvironmentChangeEvent
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.Host
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.util.UriComponentsBuilder

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.stream.Collectors

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*

@Component
public class KcsHostRoutingFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(KcsHostRoutingFilter.class);
    private final Timer connectionManagerTimer = new Timer(
            "CustomHostRoutingFilter.connectionManagerTimer", true);

    boolean sslHostnameValidationEnabled;
    boolean forceOriginalQueryStringEncoding;

    ProxyRequestHelper helper;
    Host hostProperties;
    PoolingHttpClientConnectionManager connectionManager;
    CloseableHttpClient httpClient;

    Proxy proxy = null;

    KancolleProperties kancolleProperties;


    @Autowired
    HttpSession session;

    @Autowired
    DmmLoginUserHelper userHelper;

    KcsHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties properties,
                         KancolleProperties kancolleProperties) {
        this.helper = helper;
        this.hostProperties = properties.getHost();
        this.sslHostnameValidationEnabled = properties.isSslHostnameValidationEnabled();
        this.forceOriginalQueryStringEncoding = properties
                .isForceOriginalQueryStringEncoding();
        this.kancolleProperties = kancolleProperties

        if (kancolleProperties.proxy)
            proxy = new Proxy(Proxy.Type.valueOf(kancolleProperties.proxyType), new InetSocketAddress(kancolleProperties.proxyIp, kancolleProperties.proxyPort))
    }


    @PostConstruct
    private void initialize() {
        this.httpClient = newClient();
        this.connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (KcsHostRoutingFilter.this.connectionManager == null) {
                    return;
                }
                KcsHostRoutingFilter.this.connectionManager.closeExpiredConnections();
            }
        }, 30000, 5000);
    }

    @PreDestroy
    public void stop() {
        this.connectionManagerTimer.cancel();
    }

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SIMPLE_HOST_ROUTING_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext.getCurrentContext().getRouteHost() != null && RequestContext.getCurrentContext().sendZuulResponse() && (RequestContext.getCurrentContext().get("CACHE_HIT") != Boolean.TRUE);
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        MultiValueMap<String, String> headers = this.helper
                .buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper
                .buildZuulRequestQueryParams(request);
        String verb = getVerb(request);
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        String uri = this.helper.buildZuulRequestURI(request);
        this.helper.addIgnoredHeaders();

        preProcessHeader(headers, request);
        uri = preProcessUri(headers, request, uri);
        log.debug("routing request to {}", uri);
        try {
            CloseableHttpResponse response = forward(this.httpClient, verb, uri, request,
                    headers, params, requestEntity);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 199 && statusCode < 400) {
                context.set("ROUTE_RESULT", Boolean.TRUE);
            }
            log.info("Request to {} status {}", uri, statusCode);
            setResponse(response);
        } catch (Exception ex) {
            log.error("Request failed {}", uri);
            throw new ZuulRuntimeException(ex);
        }
        return null;
    }


    protected String preProcessUri(MultiValueMap<String, String> headers, HttpServletRequest request, String originUri) {
        if (originUri.contains("/kcs/resources/image/world/") && originUri.endsWith(".png")) {
            def worldIp = userHelper.getServer(request)
            String ip = worldIp
            def newUrl = Arrays.stream(ip.split("\\.")).mapToInt({ Integer.parseInt(it) })
                    .mapToObj({ String.format("%03d", it) })
                    .collect(Collectors.joining("_"))
            def nu = originUri.replaceAll("/kcs/resources/image/world/.+", "/kcs/resources/image/world/" + newUrl + "_t.png")
            return nu;
        }
        return originUri
    }


    protected void preProcessHeader(MultiValueMap<String, String> headers, HttpServletRequest request) {
        def worldIp = userHelper.getServer(request)

        headers.set('X-Requested-With', request.getHeader("X-requested-With") ?: "ShockwaveFlash/26.0.0.151");
        def referer = request.getHeader("Referer")
        if (referer && request.getRequestURI().contains("/kcsapi/")) {
            headers.set("Origin", "http://$worldIp/");
            def replacedRef = UriComponentsBuilder.fromHttpUrl(referer).host(worldIp).port(80).scheme("http").build().toUriString()
            headers.set("Referer", replacedRef.replace(":80/", "/"))
        } else {
            headers.set("Referer", "http://www.dmm.com/netgame/social/-/gadgets/=/app_id=854854/")
        }
        headers.set("User-Agent", RequestContext.getCurrentContext().get("UA"))
        headers.remove("Cookie")
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
                Socket createSocket(HttpContext context) throws IOException {
                    if (proxy) {
                        return new Socket(proxy)
                    }
                    return new Socket();
                }
            });
            if (sslHostnameValidationEnabled) {
                registryBuilder.register(HTTPS_SCHEME,
                        new SSLConnectionSocketFactory(sslContext));
            } else {
                registryBuilder.register(HTTPS_SCHEME, new SSLConnectionSocketFactory(
                        sslContext, NoopHostnameVerifier.INSTANCE));
            }
            final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

            connectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null,
                    hostProperties.getTimeToLive(), hostProperties.getTimeUnit());
            connectionManager
                    .setMaxTotal(hostProperties.getMaxTotalConnections());
            connectionManager.setDefaultMaxPerRoute(
                    hostProperties.getMaxPerRouteConnections());
            return connectionManager;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected CloseableHttpClient newClient() {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(this.hostProperties.getSocketTimeoutMillis())
                .setConnectTimeout(this.hostProperties.getConnectTimeoutMillis())
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (!this.sslHostnameValidationEnabled) {
            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        return httpClientBuilder.setConnectionManager(newConnectionManager())
                .disableContentCompression()
                .useSystemProperties().setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new KancolleHttpRequestRetryHandler(5, false))
                .build();
    }

    private CloseableHttpResponse forward(CloseableHttpClient httpclient, String verb,
                                          String uri, HttpServletRequest request, MultiValueMap<String, String> headers,
                                          MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
                requestEntity);
        URL host = RequestContext.getCurrentContext().getRouteHost();
        HttpHost httpHost = getHttpHost(host);
        uri = StringUtils.cleanPath((host.getPath() + uri).replaceAll("/{2,}", "/"));
        ContentType contentType = null;
        if (request.getContentType() != null) {
            contentType = ContentType.parse(request.getContentType());
        }
        ByteArrayEntity entity = new ByteArrayEntity(ByteStreams.toByteArray(requestEntity), contentType);
        HttpRequest httpRequest = buildHttpRequest(verb, uri, entity, headers, params, request);
        try {
            log.debug(httpHost.getHostName() + " " + httpHost.getPort() + " "
                    + httpHost.getSchemeName());
            CloseableHttpResponse zuulResponse = forwardRequest(httpclient, httpHost,
                    httpRequest);
            this.helper.appendDebug(info, zuulResponse.getStatusLine().getStatusCode(),
                    revertHeaders(zuulResponse.getAllHeaders()));
            return zuulResponse;
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            // httpclient.getConnectionManager().shutdown();
        }
    }

    protected HttpRequest buildHttpRequest(String verb, String uri,
                                           HttpEntity entity, MultiValueMap<String, String> headers,
                                           MultiValueMap<String, String> params, HttpServletRequest request) {
        HttpRequest httpRequest;
        String uriWithQueryString = uri + (this.forceOriginalQueryStringEncoding
                ? getEncodedQueryString(request) : this.helper.getQueryString(params));

        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uriWithQueryString);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uriWithQueryString);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uriWithQueryString);
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            case "DELETE":
                BasicHttpEntityEnclosingRequest entityRequest = new BasicHttpEntityEnclosingRequest(
                        verb, uriWithQueryString);
                httpRequest = entityRequest;
                entityRequest.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uriWithQueryString);
                log.debug(uriWithQueryString);
        }

        httpRequest.setHeaders(convertHeaders(headers));
        return httpRequest;
    }

    private String getEncodedQueryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return (query != null) ? "?" + query : "";
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    private CloseableHttpResponse forwardRequest(CloseableHttpClient httpclient,
                                                 HttpHost httpHost, HttpRequest httpRequest) throws IOException {
        return httpclient.execute(httpHost, httpRequest);
    }

    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
                host.getProtocol());
        return httpHost;
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }

    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }

    private void setResponse(HttpResponse response) throws IOException {
        RequestContext.getCurrentContext().set("zuulResponse", response);
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

}
