package me.hexf.nzcp.v1;

import me.hexf.nzcp.Verifier;
import me.hexf.nzcp.exceptions.DocumentResolvingException;
import me.hexf.nzcp.exceptions.VerificationException;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class PublicCovidPass extends CovidPass {
    @Override
    public boolean isValid(Verifier verifier) {
        try {
            return verifier.verify(this).isTrustedIssuer();
        } catch (DocumentResolvingException | VerificationException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | URISyntaxException e) {
            return false;
        }
    }
}
