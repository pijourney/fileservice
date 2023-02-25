package com.pijourney.fileservice.handler.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileMetaDataResponse(@JsonProperty("id") String id,
                                   @JsonProperty("contentLength") Long contentLength,
                                   @JsonProperty("ETag") String eTag
) {
}
