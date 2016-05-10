package com.urlshortener.entrypoint.output;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestFailed {
    private String message;
}
