package me.hexf.nzcp;

import com.google.iot.cbor.*;
import foundation.identity.did.DIDDocument;
import foundation.identity.did.VerificationMethod;
import me.hexf.nzcp.exceptions.*;
import me.hexf.nzcp.resolvers.IResolver;
import me.hexf.nzcp.util.JsonWebKeyUtils;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.*;

public class Verifier {

    private final IResolver resolver;
    private final boolean raiseExceptionOnUntrustedIssuer;
    private final boolean raiseExceptionOnExpiredPass;
    private final Collection<URI> trustedIssuers;

    private Verifier(IResolver resolver, boolean raiseExceptionOnUntrustedIssuer, boolean raiseExceptionOnExpiredPass, Collection<URI> trustedIssuers) {
        this.resolver = resolver;
        this.raiseExceptionOnUntrustedIssuer = raiseExceptionOnUntrustedIssuer;
        this.raiseExceptionOnExpiredPass = raiseExceptionOnExpiredPass;
        this.trustedIssuers = trustedIssuers;
    }


    public static class Builder {
        private IResolver resolver;
        private boolean raiseExceptionOnUntrustedIssuer = false;
        private boolean raiseExceptionOnExpiredPass = false;
        private final Collection<URI> trustedIssuers = new ArrayList<>();

        public Builder addTrustedIssuer(URI issuerDid){
            trustedIssuers.add(issuerDid);
            return this;
        }

        public Builder addStandardizedTrustedIssuers(){
            trustedIssuers.add(URI.create("did:web:nzcp.identity.health.nz"));
            return this;
        }

        public Builder addExampleTrustedIssuer(){
            trustedIssuers.add(URI.create("did:web:nzcp.covid19.health.nz"));
            return this;
        }

        public Builder setRaiseExceptionOnUntrustedIssuer(boolean value){
            this.raiseExceptionOnUntrustedIssuer = value;
            return this;
        }

        public Builder setResolver(IResolver resolver) {
            this.resolver = resolver;
            return this;
        }

        public Builder setRaiseExceptionOnExpiredPass(boolean raiseExceptionOnExpiredPass) {
            this.raiseExceptionOnExpiredPass = raiseExceptionOnExpiredPass;
            return this;
        }

        public Verifier build(){
            return new Verifier(
                    resolver,
                    raiseExceptionOnUntrustedIssuer,
                    raiseExceptionOnExpiredPass,
                    trustedIssuers
            );
        }


    }

    public boolean isIssuerTrusted(URI issuerDid){
        return trustedIssuers.contains(issuerDid);
    }

    public VerificationResult verify(CovidPass pass) throws DocumentResolvingException, VerificationException, NoSuchAlgorithmException, SignatureException, URISyntaxException, InvalidKeyException {
        if(pass instanceof me.hexf.nzcp.v1.CovidPass)
            return verify((me.hexf.nzcp.v1.CovidPass) pass);
        else {
            throw new VerificationException("Provided pass type is unknown and cannot be verified");
        }
    }

    public VerificationResult verify(me.hexf.nzcp.v1.CovidPass pass) throws DocumentResolvingException, VerificationException, InvalidKeyException, NoSuchAlgorithmException, SignatureException, URISyntaxException {
        CborArray sigStructure = CborArray.create(Arrays.asList(
                CborTextString.create("Signature1"),
                CborByteString.create(pass.getProtectedHeaders().toCborByteArray()),
                CborByteString.create(new byte[0]),
                CborByteString.create(pass.getProtectedBody().toCborByteArray())
            ));

        byte[] sigMessage = sigStructure.toCborByteArray();

        PublicKey publicKey;

        URI keyLocator = new URI(pass.getIssuer() + "#" + pass.getKeyId());

        DIDDocument certificateDocument = resolver.resolveDidDocument(pass.getIssuer());
        List<VerificationMethod> verificationMethodList = certificateDocument.getAssertionMethodVerificationMethods();
        Optional<VerificationMethod> verificationMethod = verificationMethodList.stream()
                .filter(method -> method.getId().equals(keyLocator))
                .findFirst();

        if(verificationMethod.isEmpty())
            throw new KeyNotFoundException(keyLocator);
        try {
            Map<String, Object> jwk = verificationMethod.get().getPublicKeyJwk();
            publicKey = JsonWebKeyUtils.getECPublicKey(jwk);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidParameterSpecException e) {
            throw new KeyFormatException(keyLocator);
        }

        Signature ecdsa = Signature.getInstance("SHA256withECDSA");

        // transform signature from plain in to ASN.1 DER encoding.
        byte[] signature = pass.getSignature();
        byte[] r = new BigInteger(1,Arrays.copyOfRange(signature,0,32)).toByteArray();
        byte[] s = new BigInteger(1,Arrays.copyOfRange(signature,32,64)).toByteArray();
        byte[] der = new byte[6+r.length+s.length];
        der[0] = 0x30; // Tag of signature object
        der[1] = (byte)(der.length-2); // Length of signature object
        int o = 2;
        der[o++] = 0x02; // Tag of ASN1 Integer
        der[o++] = (byte)r.length; // Length of first signature part
        System.arraycopy (r,0, der,o, r.length);
        o += r.length;
        der[o++] = 0x02; // Tag of ASN1 Integer
        der[o++] = (byte)s.length; // Length of second signature part
        System.arraycopy (s,0, der,o, s.length);

        ecdsa.initVerify(publicKey);
        ecdsa.update(sigMessage);

        boolean matchesSignature = ecdsa.verify(der);

        if(!matchesSignature)
            throw new SignatureMismatchException();

        if(raiseExceptionOnUntrustedIssuer && !isIssuerTrusted(pass.getIssuer()))
            throw new UntrustedIssuerException(pass.getIssuer());

        if(raiseExceptionOnExpiredPass && pass.isExpired())
            throw new ExpiredPassException();

        return new VerificationResult(pass);

    }

    public class VerificationResult {
        private final CovidPass pass;

        private VerificationResult(CovidPass pass) {
            this.pass = pass;
        }

        public boolean isTrustedIssuer() {
            return isIssuerTrusted(pass.getIssuer());
        }

        public CovidPass getPass() {
            return pass;
        }

        public Verifier getVerifier() {
            return Verifier.this;
        }

        public boolean isExpired(){
            return pass.isExpired();
        }
    }
}
