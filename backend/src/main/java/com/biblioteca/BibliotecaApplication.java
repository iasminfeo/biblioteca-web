package com.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BibliotecaApplication {

    public static void main(String[] args) {
        // Roda sempre em UTC (igual ao container Docker). No fuso America/Sao_Paulo,
        // datas antigas (ex.: 1839-06-21) voltavam do banco com 1 dia a menos.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BibliotecaApplication.class, args);
    }
}
