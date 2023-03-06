package com.pijourney.fileservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pijourney.fileservice.FileserviceApplication;
import com.pijourney.fileservice.handler.protocol.CreateFileMetaData;
import com.pijourney.fileservice.handler.protocol.FileMetaDataResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePartEvent;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes ={FileserviceApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileHandlerTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateFile() throws JsonProcessingException {
        // Create a temporary file with random data
        byte[] fileData = new byte[1024 * 1024 * 10]; // 10 MB
        var createFileMetaData = new CreateFileMetaData("Testfile.txt", (long) fileData.length);
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        var dataBuffer = Flux.just(bufferFactory.wrap(fileData));
        var metaDataJson = objectMapper.writeValueAsString(createFileMetaData);
        // Prepare the request body using PartEvent
        Consumer<HttpHeaders> headersConsumer = headers -> {
            headers.add("File-Meta-Data", metaDataJson);
        };
        var partEventFlux = FilePartEvent.create("file",createFileMetaData.name(),
                MediaType.TEXT_PLAIN, dataBuffer, headersConsumer);

        // Send a POST request to create the file
        webTestClient.post().uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromProducer(partEventFlux, FilePartEvent.class))
                .header("File-Meta-Data", String.valueOf(createFileMetaData))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FileMetaDataResponse.class)
                .value(fileMetaDataResponse -> {
                    assertThat(fileMetaDataResponse.id()).isNotNull();
                    assertThat(fileMetaDataResponse.contentLength()).isEqualTo(fileData.length);
                    assertThat(fileMetaDataResponse.eTag()).isNotNull();
                });
    }
}