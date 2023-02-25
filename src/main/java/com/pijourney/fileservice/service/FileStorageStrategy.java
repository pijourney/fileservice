package com.pijourney.fileservice.service;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileStorageStrategy {
    public Mono<String> saveFile(String id,
                              String name,
                              long contentLength,
                              String mimeType,
                              Flux<DataBuffer> data);

}
