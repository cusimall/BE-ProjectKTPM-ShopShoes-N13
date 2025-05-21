package com.microservice.cartservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceCommunicationException extends RuntimeException {
    
    private String serviceName;
    private int statusCode;
    
    public ServiceCommunicationException(String message) {
        super(message);
    }
    
    public ServiceCommunicationException(String serviceName, String message) {
        super("Error communicating with " + serviceName + " service: " + message);
        this.serviceName = serviceName;
    }
    
    public ServiceCommunicationException(String serviceName, int statusCode, String message) {
        super("Error communicating with " + serviceName + " service: " + 
              "Status " + statusCode + " - " + message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

} 