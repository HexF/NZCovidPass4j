package me.hexf.nzcp.exceptions;

public class MalformedPayloadException extends DecodingException{
    public MalformedPayloadException(String message){
        super(message);
    }
}
