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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrecoDaHoraService {
    private static final Logger logger = LoggerFactory.getLogger(PrecoDaHoraService.class);

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
    private static final String PARAM_PROCESSO = "processo";
    private static final String PARAM_TOTAL_REGISTROS = "totalRegistros";
    private static final String PARAM_TOTAL_PAGINAS = "totalPaginas";
    private static final String PARAM_PAGEVIEW = "pageview";

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
                logger.info("Nenhum código de produto encontrado para o termo: {}", termo);
            }
        } catch (IOException e) {
            logger.error("Erro ao fazer a requisição para a API: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("Requisição interrompida: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    private String obterCodigoProduto(String termo) throws IOException, InterruptedException {
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

    private JSONObject buscarProdutos(String codigoProduto) throws IOException, InterruptedException {
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
        parametrosProduto.put(PARAM_PROCESSO, "carregar");
        parametrosProduto.put(PARAM_TOTAL_REGISTROS, "0");
        parametrosProduto.put(PARAM_TOTAL_PAGINAS, "0");
        parametrosProduto.put(PARAM_PAGEVIEW, "lista");

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
                logger.info("Nenhum produto encontrado.");
            }
        } catch (JSONException e) {
            logger.error("Erro ao processar o JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Erro inesperado: {}", e.getMessage(), e);
        }
    }
}
