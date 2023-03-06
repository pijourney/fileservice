package com.pijourney.fileservice.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class FileRouter {
    private final FileHandler fileHandler;

    public FileRouter(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }
    @Bean
    @Autowired
    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions
                .route(RequestPredicates.POST("/files"), fileHandler::handleCreateFile);
    }
}