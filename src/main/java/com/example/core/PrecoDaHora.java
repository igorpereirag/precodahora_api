package com.example.core;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrecoDaHora {
    private final CloseableHttpClient httpClient;
    private final String baseUrl = "https://precodahora.ba.gov.br";
    private String csrfToken = "";
    private String cookies = "";

    public PrecoDaHora() {
        this.httpClient = HttpClients.createDefault();
    }

    private String makePostRequest(String endpoint, Map<String, String> params) throws IOException {
        HttpPost postRequest = new HttpPost(baseUrl + endpoint);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        postRequest.setConfig(requestConfig);

        postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        postRequest.setHeader("X-CSRFToken", csrfToken);
        postRequest.setHeader("Cookie", cookies);

        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (body.length() > 0) body.append("&");
            body.append(entry.getKey()).append("=").append(entry.getValue());
        }
        postRequest.setEntity(new StringEntity(body.toString()));

        try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        }
    }

    public JSONObject post(String endpoint, Map<String, String> params) throws IOException {
        String response = makePostRequest(endpoint, params);
        return new JSONObject(response);
    }

    public Map<String, String> getCookiesAndCsrfToken() throws IOException {
        HttpGet getRequest = new HttpGet(baseUrl + "/");

        getRequest.setHeader("User-Agent", "Mozilla/5.0");

        try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
            String responseBody = EntityUtils.toString(response.getEntity());

            Document document = Jsoup.parse(responseBody);
            Element csrfElement = document.selectFirst("#validate");
            String csrfToken = csrfElement != null ? csrfElement.attr("data-id") : "";
            String cookies = response.getFirstHeader("Set-Cookie") != null ? response.getFirstHeader("Set-Cookie").getValue() : "";

            this.csrfToken = csrfToken;
            this.cookies = cookies;

            Map<String, String> result = new HashMap<>();
            result.put("csrfToken", csrfToken);
            result.put("cookies", cookies);
            return result;
        }
    }

    public void setCookiesAndCsrfToken(Map<String, String> cookiesAndCsrfToken) {
        this.csrfToken = cookiesAndCsrfToken.get("csrfToken");
        this.cookies = cookiesAndCsrfToken.get("cookies");
    }
}
