package com.example.core;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONObject;

public class PrecoDaHora {
    private final HttpClient httpClient;
    private final String baseUrl = "https://precodahora.ba.gov.br";
    private String csrfToken = "";
    private String cookies = "";

    public PrecoDaHora() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private String makePostRequest(String endpoint, Map<String, String> params) throws IOException, InterruptedException {
        URI uri = URI.create(baseUrl + endpoint);

        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (body.length() > 0) body.append("&");
            body.append(entry.getKey()).append("=").append(entry.getValue());
        }

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("X-CSRFToken", csrfToken)
                .header("Cookie", cookies)
                .POST(BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public JSONObject post(String endpoint, Map<String, String> params) throws IOException, InterruptedException {
        String response = makePostRequest(endpoint, params);
        return new JSONObject(response);
    }

    public Map<String, String> getCookiesAndCsrfToken() throws IOException, InterruptedException {
        URI uri = URI.create(baseUrl + "/");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        Document document = Jsoup.parse(responseBody);
        Element csrfElement = document.selectFirst("#validate");
        String csrfToken = csrfElement != null ? csrfElement.attr("data-id") : "";
        String cookies = response.headers().firstValue("Set-Cookie").orElse("");

        this.csrfToken = csrfToken;
        this.cookies = cookies;

        Map<String, String> result = new HashMap<>();
        result.put("csrfToken", csrfToken);
        result.put("cookies", cookies);
        return result;
    }

    public void setCookiesAndCsrfToken(Map<String, String> cookiesAndCsrfToken) {
        this.csrfToken = cookiesAndCsrfToken.get("csrfToken");
        this.cookies = cookiesAndCsrfToken.get("cookies");
    }
}
