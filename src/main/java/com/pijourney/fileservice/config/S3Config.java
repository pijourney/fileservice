package com.pijourney.fileservice.config;

import com.pijourney.fileservice.service.AwsFileStorageStrategy;
import com.pijourney.fileservice.service.FileStorageStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "storageStrategy", havingValue = "aws")
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3AsyncClient s3AsyncClient(@Value("aws.bucket") String bucket,
                                       @Value("aws.access-key") String accessKey,
                                       @Value("secret-key") String secretKey,
                                       @Value("region") String region,
                                       @Value("multipart-min-part-size") String maxSize,
                                       @Value("endpoint") String url){
        return S3AsyncClient.builder()
                .httpClient(NettyNioAsyncHttpClient.builder().writeTimeout(Duration.ZERO).maxConcurrency(64).build())
                .region(Region.of(region))
                .endpointOverride(URI.create(url))
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .chunkedEncodingEnabled(true)
                        .pathStyleAccessEnabled(true).build())
                .credentialsProvider(() ->AwsBasicCredentials.create(accessKey, secretKey)).build();
    }
    @Bean
    public FileStorageStrategy awsFileStorageStrategy(S3AsyncClient s3AsyncClient) {
        return new AwsFileStorageStrategy(s3AsyncClient);
    }

}
