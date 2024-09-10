package com.example;

import com.example.core.PrecoDaHora;
import com.example.service.PrecoDaHoraService;

public class Main {
    public static void main(String[] args) {
       
        String latitude = "-12.2711";
        String longitude = "-38.9684";

        
        PrecoDaHora precoDaHora = new PrecoDaHora();

       
        PrecoDaHoraService precoDaHoraService = new PrecoDaHoraService(precoDaHora, latitude, longitude);

        
        String termo = "CAFE";

        
        precoDaHoraService.buscarProduto(termo);
    }
}
