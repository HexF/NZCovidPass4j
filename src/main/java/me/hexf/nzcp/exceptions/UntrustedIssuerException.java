package me.hexf.nzcp.exceptions;

import java.net.URI;

public class UntrustedIssuerException extends VerificationException{

    public UntrustedIssuerException(URI issuer) {
        super(String.format("Issuer \"%s\" is untrusted", issuer));
    }
}
