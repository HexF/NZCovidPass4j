package me.hexf.nzcp.v1;

import com.google.iot.cbor.*;
import me.hexf.nzcp.CredentialSubject;
import me.hexf.nzcp.Verifier;
import me.hexf.nzcp.exceptions.DecodingException;
import me.hexf.nzcp.exceptions.MalformedPayloadException;
import me.hexf.nzcp.util.CborUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;

public abstract class CovidPass extends me.hexf.nzcp.CovidPass {
    private byte[] signature;
    private CborObject protectedHeaders;
    private CborObject protectedBody;

    public byte[] getSignature() {
        return signature;
    }

    public CborObject getProtectedHeaders() {
        return protectedHeaders;
    }

    public CborObject getProtectedBody() {
        return protectedBody;
    }

    public static CovidPass createFromByteArray(byte[] payload) throws  DecodingException {
        return createFromByteArray(payload, 0, payload.length);
    }

    public static CovidPass createFromByteArray(byte[] payload, int offset, int length) throws DecodingException {
        CborArray dataArray;
        try {
            dataArray = (CborArray) CborArray.createFromCborByteArray(payload, offset, length);
        } catch (CborParseException e) {
            throw new MalformedPayloadException("CBOR parsing failed");
        }

        if(dataArray.getTag() != 18)
            throw new MalformedPayloadException("Invalid signing tag");

        List<CborObject> objects = dataArray.listValue();

        if(objects.size() != 4)
            throw new MalformedPayloadException("Expected 4 objects");

        CborByteString protectedHeaderByteString = (CborByteString) objects.get(0);
        byte[] protectedHeaderBytes = protectedHeaderByteString.byteArrayValue();
        CborMap protectedHeaders;
        try {
            protectedHeaders = CborMap.createFromCborByteArray(protectedHeaderBytes);
        } catch (CborParseException e) {
            throw new MalformedPayloadException("Malformed protected headers");
        }

        // ignore unprotected header for now - no extensions yet :/

        CborByteString bodyByteString = (CborByteString) objects.get(2);
        byte[] bodyBytes = bodyByteString.byteArrayValue();
        CborMap body;
        try {
            body = CborMap.createFromCborByteArray(bodyBytes);
        } catch (CborParseException e) {
            throw new MalformedPayloadException("Malformed body payload");
        }


        CborByteString signatureByteString = (CborByteString) objects.get(3);
        byte[] signatureBytes = signatureByteString.byteArrayValue();

        CovidPass pass = new PublicCovidPass();

        pass.populateFromCbor(protectedHeaders, body, signatureBytes);

        return pass;
    }

    private void populateFromCbor(CborMap header, CborMap body, byte[] signature) throws MalformedPayloadException {
        if(signature.length != 64)
            throw new MalformedPayloadException("Invalid signature length");
        this.signature = signature;

        this.protectedHeaders = header;
        this.protectedBody = body;

        this.keyId = CborUtils.getMapBytesAsString(header, 4);
        this.id = CborUtils.getMapBytesAsUuid(body, 7);

        String issuer = ((CborTextString) CborUtils.getMapObject(body, 1)).stringValue();
        try {
            this.issuer = new URI(issuer);
        } catch (URISyntaxException e) {
            throw new MalformedPayloadException("Invalid issuer");
        }

        this.notValidBefore = LocalDateTime.ofEpochSecond(CborUtils.getMapNumber(body,5), 0, ZoneOffset.UTC);
        this.notValidAfter = LocalDateTime.ofEpochSecond(CborUtils.getMapNumber(body, 4), 0, ZoneOffset.UTC);

        CborMap vc = (CborMap) body.get("vc");
        CborMap cs = (CborMap) vc.get("credentialSubject");

        LocalDate dob = LocalDate.from(
                DateTimeFormatter.ISO_DATE.parse(
                    ((CborTextString) cs.get("dob")).stringValue()
            )
        );

        this.credentialSubject = new CredentialSubject(
                ((CborTextString) cs.get("givenName")).stringValue(),
                ((CborTextString) cs.get("familyName")).stringValue(),
                dob
        );

    }

}
