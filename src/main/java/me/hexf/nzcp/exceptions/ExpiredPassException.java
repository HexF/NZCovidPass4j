package me.hexf.nzcp.exceptions;

public class ExpiredPassException extends VerificationException{

    public ExpiredPassException() {
        super("The provided pass is expired");
    }
}
