package me.hexf.nzcp.util;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Map;

public class JsonWebKeyUtils {
    public static Map<String, String> EC_CURVES = Map.of(
            "P-256", "secp256r1"
    );

    public static ECPublicKey getECPublicKey(Map<String, Object> jwk) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidParameterSpecException {
        String keyType = (String) jwk.get("kty");
        String curve = (String) jwk.get("crv");

        assert keyType.equals("EC");

        byte[] xBytes = Base64.decodeBase64((String) jwk.get("x"));
        byte[] yBytes = Base64.decodeBase64((String) jwk.get("y"));

        BigInteger x = new BigInteger(xBytes);
        BigInteger y = new BigInteger(yBytes);

        ECPoint point = new ECPoint(x, y);

        AlgorithmParameters params = AlgorithmParameters.getInstance(keyType);
        params.init(new ECGenParameterSpec(EC_CURVES.get(curve)));

        ECPublicKeySpec spec = new ECPublicKeySpec(point, params.getParameterSpec(ECParameterSpec.class));

        KeyFactory factory = KeyFactory.getInstance(keyType);
        PublicKey key = factory.generatePublic(spec);

        return (ECPublicKey) key;

    }
}
