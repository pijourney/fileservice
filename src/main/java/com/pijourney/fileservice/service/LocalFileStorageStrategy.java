package com.pijourney.fileservice.service;

import jakarta.xml.bind.DatatypeConverter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LocalFileStorageStrategy implements FileStorageStrategy {
    private final Path basePath;


    public LocalFileStorageStrategy(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public Mono<String> saveFile(String path, String name, long contentLength, String mimeType, Flux<DataBuffer> data) {
        Path filePath = basePath.resolve(path+"-"+name+"."+mimeType);
        // Create the directory if it does not exist
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                return Mono.error(e);
            }
        }
        //Should never happen cause uuid in filename.
        if (Files.exists(filePath)) {
            // handle file already exists case optional to remove if removed will overwrite existing file.
            return Mono.error(new FileAlreadyExistsException(filePath.toString()));
        }
        // Open the output file for writing
        try {
            OutputStream outputStream = Files.newOutputStream(filePath);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // Write the file data to disk and compute the ETag hash in a streaming fashion
            return DataBufferUtils.write(data, outputStream)
                    .doOnNext(dataBuffer ->{
                        messageDigest.update(dataBuffer.asByteBuffer());
                        DataBufferUtils.release(dataBuffer);
                    })
                    .then(Mono.fromCallable(() -> {
                        byte[] hashBytes = messageDigest.digest();
                        String hashString = DatatypeConverter.printHexBinary(hashBytes);
                        return "\"" + hashString + "\"";
                    }));
        } catch (IOException | NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }
}