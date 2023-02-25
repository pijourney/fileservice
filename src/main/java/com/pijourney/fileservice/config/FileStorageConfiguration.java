package com.pijourney.fileservice.config;

import com.pijourney.fileservice.service.AwsFileStorageStrategy;
import com.pijourney.fileservice.service.FileStorageStrategy;
import com.pijourney.fileservice.service.LocalFileStorageStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfiguration {

    @Bean
    @ConditionalOnProperty(name = "storageStrategy", havingValue = "local")
    public FileStorageStrategy localFileStorageStrategy(@Value("${local.basePath}") String basePath) {
        return new LocalFileStorageStrategy(Paths.get(basePath));
    }

    @Bean
    @ConditionalOnProperty(name = "storageStrategy", havingValue = "aws")
    public FileStorageStrategy awsFileStorageStrategy(S3AsyncClient s3AsyncClient) {
        return new AwsFileStorageStrategy(s3AsyncClient);
    }
}