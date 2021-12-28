package me.hexf.nzcp.exceptions;

import java.net.URI;

public class KeyFormatException extends VerificationException{

    public KeyFormatException(URI keyLocator) {
        super(String.format("Key \"%s\" was not a valid JSON Web Key", keyLocator));
    }
}
