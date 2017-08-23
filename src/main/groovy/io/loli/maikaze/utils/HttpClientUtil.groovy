package io.loli.maikaze.utils

import com.alibaba.fastjson.JSON
import okhttp3.*
import org.springframework.web.util.UriComponentsBuilder

/**
 * Created by uzuma on 2017/8/20.
 */
public class HttpClientUtil {
    CookieManager cookieManager = new CookieManager();
    {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    CookieJar cookieJar = new JavaNetCookieJar(cookieManager)

    HttpClientUtil() {
        client = new OkHttpClient().newBuilder().cookieJar(cookieJar).build()
    }

    HttpClientUtil(Proxy proxy) {
        client = new OkHttpClient().newBuilder().cookieJar(cookieJar).proxy(proxy).build()
    }
    private OkHttpClient client

    String get(String url, Map<String, String> queryParams, Map<String, String> header) {
        def headers = Headers.of(Optional.ofNullable(header).orElse(new HashMap<>()));
        def componentsBuilder = UriComponentsBuilder.fromHttpUrl(url);
        Optional.ofNullable(queryParams).orElse(new HashMap<>())
                .forEach({ a, b -> componentsBuilder.queryParam(a, b) })
        def request = new Request.Builder()
                .get()
                .url(componentsBuilder.toUriString()).headers(headers)
                .build();
        Response response = client.newCall(request).execute();
        response.body().string();
    }

    String post(String url, Map<String, String> postParams, Map<String, String> header) {
        def headers = Headers.of(Optional.ofNullable(header).orElse(new HashMap<>()));
        def builder = new FormBody.Builder()
        postParams.each { entry -> builder.add(entry.key, entry.value.toString()) }
        def formBody = builder.build();
        def request = new Request.Builder()
                .post(formBody)
                .url(url).headers(headers)
                .build();

        def response = client.newCall(request).execute();
        return response.body().string();
    }

    String postBody(String url, Map<String, String> postParams, Map<String, String> header) {
        def headers = Headers.of(Optional.ofNullable(header).orElse(new HashMap<>()));
        def request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(postParams)))
                .url(url).headers(headers)
                .build();

        def response = client.newCall(request).execute();
        return response.body().string();
    }

}
