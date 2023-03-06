package com.pijourney.fileservice.service;

import com.pijourney.fileservice.handler.protocol.FileMetaDataResponse;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
@Service
public class FileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private FileStorageStrategy fileStorageStrategy;
    public FileService( FileStorageStrategy fileStorageStrategy){
        this.fileStorageStrategy = Validate.notNull(fileStorageStrategy, "fileStorageStrategy can't be null");
    }

    public Mono<FileMetaDataResponse> putFile(String name, MediaType contentType, Long contentLength, Flux<DataBuffer> data) {
        return Mono.fromSupplier(UUID::randomUUID)
                .flatMap(id ->
                        fileStorageStrategy.saveFile(id.toString(), name, contentLength, contentType.getType(),data)
                                .map(eTag ->
                                        // Here you could save  to database or something, all the meta data  about a file.
                                        new FileMetaDataResponse(id.toString(), contentLength, eTag)));
    }


}
