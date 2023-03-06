package com.pijourney.fileservice.config;

import com.pijourney.fileservice.service.FileStorageStrategy;
import com.pijourney.fileservice.service.LocalFileStorageStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
@ConditionalOnProperty(name = "storageStrategy", havingValue = "local")
public class FileStorageConfiguration {

    @Bean
    public FileStorageStrategy localFileStorageStrategy(@Value("${local.base-path}") String basePath) {
        return new LocalFileStorageStrategy(Paths.get(basePath));
    }
}