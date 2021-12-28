package me.hexf.nzcp.exceptions;

import java.net.URI;

public class DocumentNotFoundException extends DocumentResolvingException{
    public DocumentNotFoundException(URI documentUrl){
        super(String.format("Failed to resolve document \"%s\"", documentUrl.toString()));

    }
}
