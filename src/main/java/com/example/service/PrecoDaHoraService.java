package com.example.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.core.PrecoDaHora;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class PrecoDaHoraService {
    private final PrecoDaHora precoDaHora;
    private final String latitude;
    private final String longitude;

    private static final String API_SUGESTAO = "/sugestao/";
    private static final String API_PRODUTOS = "/produtos/";
    private static final String PARAM_ITEM = "item";
    private static final String PARAM_GTIN = "gtin";
    private static final String PARAM_HORAS = "horas";
    private static final String PARAM_LATITUDE = "latitude";
    private static final String PARAM_LONGITUDE = "longitude";
    private static final String PARAM_RAIO = "raio";
    private static final String PARAM_PRECOMAX = "precomax";
    private static final String PARAM_PRECOMIN = "precomin";
    private static final String PARAM_ORDENAR = "ordenar";
    private static final String PARAM_PAGINA = "pagina";

    public PrecoDaHoraService(PrecoDaHora precoDaHora, String latitude, String longitude) {
        this.precoDaHora = precoDaHora;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void buscarProduto(String termo) {
        try {
            Map<String, String> cookiesAndCsrfToken = precoDaHora.getCookiesAndCsrfToken();
            precoDaHora.setCookiesAndCsrfToken(cookiesAndCsrfToken);

            String codigoProduto = obterCodigoProduto(termo);

            if (codigoProduto != null) {
                JSONObject produtoResponse = buscarProdutos(codigoProduto);
                imprimirProdutos(produtoResponse);
            } else {
                System.out.println("Nenhum código de produto encontrado para o termo: " + termo);
            }
        } catch (IOException e) {
            System.err.println("Erro ao fazer a requisição para a API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String obterCodigoProduto(String termo) throws IOException {
        Map<String, String> parametrosSugestao = new HashMap<>();
        parametrosSugestao.put(PARAM_ITEM, termo);

        JSONObject sugestaoResponse = precoDaHora.post(API_SUGESTAO, parametrosSugestao);

        if (sugestaoResponse.has("resultado")) {
            JSONArray resultados = sugestaoResponse.getJSONArray("resultado");
            if (resultados.length() > 0) {
                JSONObject primeiroResultado = resultados.getJSONObject(0);
                return Optional.ofNullable(primeiroResultado.opt("gtin"))
                               .map(g -> g.toString())
                               .orElse(null);
            }
        }
        return null;
    }

    private JSONObject buscarProdutos(String codigoProduto) throws IOException {
        Map<String, String> parametrosProduto = new HashMap<>();
        parametrosProduto.put(PARAM_GTIN, codigoProduto);
        parametrosProduto.put(PARAM_HORAS, "72");
        parametrosProduto.put(PARAM_LATITUDE, latitude);
        parametrosProduto.put(PARAM_LONGITUDE, longitude);
        parametrosProduto.put(PARAM_RAIO, "15");
        parametrosProduto.put(PARAM_PRECOMAX, "0");
        parametrosProduto.put(PARAM_PRECOMIN, "0");
        parametrosProduto.put(PARAM_ORDENAR, "preco.asc");
        parametrosProduto.put(PARAM_PAGINA, "1");

        return precoDaHora.post(API_PRODUTOS, parametrosProduto);
    }

    private void imprimirProdutos(JSONObject produtoResponse) {
    try {
        if (produtoResponse.has("resultado") && produtoResponse.getJSONArray("resultado").length() > 0) {
            JSONArray resultados = produtoResponse.getJSONArray("resultado");
            IntStream.range(0, resultados.length())
                .mapToObj(resultados::getJSONObject)
                .forEach(resultado -> {               
                    System.out.println(resultado.toString(2)); 
                });
        } else {
            System.out.println("Nenhum produto encontrado.");
        }
    } catch (JSONException e) {
        System.err.println("Erro ao processar o JSON: " + e.getMessage());
    } catch (Exception e) {
        System.err.println("Erro inesperado: " + e.getMessage());
    }
}

}
