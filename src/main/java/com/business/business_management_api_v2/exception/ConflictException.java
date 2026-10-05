package com.business.business_management_api_v2.exception;

public class ConflictException extends RuntimeException{
    public ConflictException(String mess){
        super(mess);
    }
}
