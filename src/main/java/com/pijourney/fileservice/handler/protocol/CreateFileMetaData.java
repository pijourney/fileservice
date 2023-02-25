package com.pijourney.fileservice.handler.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateFileMetaData(
        @JsonProperty("Name") String name,
        @JsonProperty("contentLength") Long contentLength
        ) {}
