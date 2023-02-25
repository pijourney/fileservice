package com.pijourney.fileservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pijourney.fileservice.error.ContentTypeMissingException;
import com.pijourney.fileservice.handler.protocol.CreateFileMetaData;
import com.pijourney.fileservice.service.FileService;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePartEvent;
import org.springframework.http.codec.multipart.PartEvent;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

public class Filehandler {
    private final  ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    private final FileService fileService;

    public Filehandler(ObjectMapper objectMapper, FileService fileService){
        this.objectMapper = Validate.notNull(objectMapper, "ObjectMapper can't be null");
        this.fileService = Validate.notNull(fileService,"Fileservice can't be null");
    }

    public Mono<ServerResponse> handleCreateFile(ServerRequest request) {
        var reqHeaders = request.headers();
        return request.bodyToFlux(PartEvent.class)
                .windowUntil(PartEvent::isLast)
                .concatMap(partWindow -> partWindow
                        .switchOnFirst((signal, partEvents) -> {
                            if (signal.hasValue()) {
                                PartEvent event = signal.get();
                                if(event instanceof FilePartEvent fileEvent) {
                                    var fileMetaData = fileEvent.headers().containsKey("File-Meta-Data") ?
                                            fileEvent.headers().getFirst("File-Meta-Data") :
                                            reqHeaders.header("File-Meta-Data").get(0);
                                    CreateFileMetaData createFileRequest = null;
                                    try {
                                        createFileRequest = unpackHeader(fileMetaData);
                                    }catch(JsonProcessingException e){
                                        Mono.error(e);
                                    }
                                    if(fileEvent.headers().getContentType() == null){
                                        return Mono.error(new ContentTypeMissingException());
                                    }
                                    var contentType = fileEvent.headers().getContentType();
                                    LOGGER.info("Accepted request with contentType={} createFileRequest={}", contentType, createFileRequest);
                                    return  fileService.putFile(
                                                    createFileRequest.name(),
                                                    contentType,
                                                    createFileRequest.contentLength(),
                                                    partEvents.map(PartEvent::content)
                                            ).doFinally(signalType -> ReferenceCountUtil.release(partEvents.map(PartEvent::content)));
                                }
                                else{
                                    ReferenceCountUtil.release(event.content());
                                    return Mono.error(new RuntimeException("Unexpected event: " + event));

                                }
                            } else {
                                return Mono.error(new RuntimeException("Unexpected signal: " + signal.getType()));
                            }
                        })
                ).single().flatMap(fd -> ServerResponse.created(request.uriBuilder().path("/{id}").build(fd.id()))
                        .body(fromValue(fd)))
                .onErrorResume(ContentTypeMissingException.class, e -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(ErrorResponse.builder(e,
                                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                        "Content type must be specified in every part in a multipart upload").build())))
                .onErrorResume(JsonProcessingException.class, e -> ServerResponse.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(ErrorResponse.builder(e,
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                "File-Meta-Data header is malformed or missing!").build())));
    }

    private CreateFileMetaData unpackHeader(String fileMetaData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CreateFileMetaData createFileRequest = objectMapper.readValue(fileMetaData, CreateFileMetaData.class);
        return createFileRequest;
    }
}
