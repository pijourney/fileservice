package com.pijourney.fileservice.config;

import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.PartEventHttpMessageReader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.charset.StandardCharsets;
@Component
public class ConfigureCodecConfig  implements WebFluxConfigurer {
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer serverCodecConfigurer){
        serverCodecConfigurer
                .customCodecs().register(streamPartReader());
    }
    private PartEventHttpMessageReader streamPartReader(){
        var partReader = new PartEventHttpMessageReader();
        partReader.setMaxInMemorySize(16*1024*1024);
        partReader.setHeadersCharset(StandardCharsets.UTF_8);
        return partReader;
    }
}
