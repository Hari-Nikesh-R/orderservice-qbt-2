package com.example;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailFeignConfiguration {

    @Bean(name = "mailServiceFeign")
    public MailServiceFeign mailServiceFeign() {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(MailServiceFeign.class))
                .logLevel(Logger.Level.FULL)
                .target(MailServiceFeign.class, "http://121.0.0.1:8095");
    }
}
