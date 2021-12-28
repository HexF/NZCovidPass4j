package me.hexf.nzcp.exceptions;

public class SignatureMismatchException extends VerificationException {
    public SignatureMismatchException() {
        super("The provided key doesn't match the signature");
    }
}
