package me.hexf.nzcp;

import com.google.common.io.BaseEncoding;
import me.hexf.nzcp.exceptions.DecodingException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public abstract class CovidPass {
    protected LocalDateTime notValidBefore;
    protected LocalDateTime notValidAfter;
    protected UUID id;
    protected URI issuer;
    protected String keyId;
    protected CredentialSubject credentialSubject;

    public LocalDateTime getNotValidBefore() {
        return notValidBefore;
    }

    public LocalDateTime getNotValidAfter() {
        return notValidAfter;
    }

    public UUID getId() {
        return id;
    }

    public URI getIssuer() {
        return issuer;
    }

    public String getKeyId() {
        return keyId;
    }

    public CredentialSubject getCredentialSubject() {
        return credentialSubject;
    }


    public abstract boolean isValid(Verifier verifier);

    public static CovidPass createFromQRCodeString(String payload) throws DecodingException {
        if(!payload.startsWith("NZCP:/"))
            throw new DecodingException("QR Code doesn't start with \"NZCP:/\"");
        String[] dataBlocks = payload.split("/");
        if(dataBlocks.length != 3)
            throw new DecodingException("Expected 2 \"/\" characters");

        // just a sanity check
        assert dataBlocks[0].equals("NZCP:");

        if(dataBlocks[1].equals("1")){
            byte[] payloadBytes = BaseEncoding.base32().decode(dataBlocks[2]);
            return me.hexf.nzcp.v1.CovidPass.createFromByteArray(payloadBytes);
        }else {
            throw new DecodingException("Unknown major protocol version \"" + dataBlocks[0] + "\"");
        }

    }

    public boolean isExpired(LocalDateTime when) {
        return when.isBefore(notValidBefore) || when.isAfter(notValidAfter);
    }

    public boolean isExpired(){
        return isExpired(LocalDateTime.now());
    }
}
