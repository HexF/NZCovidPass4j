package me.hexf.nzcp.exceptions;

import java.net.URI;

public class KeyNotFoundException extends VerificationException {
    public KeyNotFoundException(URI keyLocator) {
        super(String.format("Unable to locate key \"%s\"", keyLocator));
    }
}
