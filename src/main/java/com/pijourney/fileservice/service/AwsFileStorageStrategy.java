package com.pijourney.fileservice.service;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.time.Duration;
public class AwsFileStorageStrategy implements FileStorageStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final S3AsyncClient s3;
    @Value("bucket")
    private String bucket;

    public AwsFileStorageStrategy(S3AsyncClient s3) {
        this.s3 = Validate.notNull(s3, "s3 cant be null!");
    }

    @Override
    public Mono<String> saveFile(String path,
                              String name,
                              long contentLength,
                              String mimeType,
                              Flux<DataBuffer> data) {
        Validate.notEmpty(name, "name can't be null or empty");
        Validate.notEmpty(mimeType, "mimeType can't be null or empty");
        Validate.notNull(data, "data can't be null or empty");
        var start = System.nanoTime();

        // Transform Flux<DataBuffer> into AsyncRequestBody fromPublisher,
        // which provides a publisher of byte buffers to upload the file to AWS
        var body = AsyncRequestBody.fromPublisher(data.map(b->{
            var bytes = b.toByteBuffer();
            DataBufferUtils.release(b);
            return bytes;
        }));

        // Use the S3AsyncClient to put the object in S3 bucket, and get the ETag of the object as a Mono<String>
        return Mono.fromCompletionStage(s3.putObject(builder -> builder
                        .key(path)
                        .bucket(bucket)
                        .contentType(mimeType)
                        .contentLength(contentLength)
                        .build(), body))
                .map(response -> response.eTag())
                .doOnSuccess(tag ->LOGGER.info("Uploaded file with path ={} to AWS in duration={}", path,  Duration.ofNanos(System.nanoTime() - start).toMillis()));
    }
}
