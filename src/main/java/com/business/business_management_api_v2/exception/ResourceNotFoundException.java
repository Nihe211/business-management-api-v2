package com.business.business_management_api_v2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String mess){
        super(mess);
    }
}
