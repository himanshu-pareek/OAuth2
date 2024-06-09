package dev.javarush.oauth2.authorizationserver.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Repository
public class KeyPairRepository {
    Map<String, KeyPair> realmToKeyPair;

    private final JdbcClient jdbcClient;

    public KeyPairRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
        this.realmToKeyPair = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        this.jdbcClient.sql("SELECT realm_id, private_key, public_key FROM realm_keys")
                .query(this::handleRow);
    }

    private void handleRow(ResultSet row) throws SQLException {
        String realmId = row.getString("realm_id");
        String privateKey = row.getString("private_key");
        String publicKey = row.getString("public_key");
        this.addRealmWithKeys(realmId, privateKey, publicKey);
    }

    private void insertKeyPair (String realmId, String privateKey, String publicKey) {
        this.jdbcClient.sql("INSERT INTO realm_keys (realm_id, private_key, public_key) VALUES (:realmId, :privateKey, :publicKey)")
                .param("realmId", realmId)
                .param("privateKey", privateKey)
                .param("publicKey", publicKey)
                .update();
    }

    public void generateKeysForRealm (String realmId) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            String[] keys = getKeyStrings(keyPair);
            this.insertKeyPair(realmId, keys[0], keys[1]);
            this.realmToKeyPair.put(realmId, keyPair);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public PrivateKey getPrivateKey (String realmId) {
        return this.realmToKeyPair.get(realmId).getPrivate();
    }

    public String getPrivateKeyString (String realmId) {
        KeyPair keyPair = realmToKeyPair.get(realmId);
        if (keyPair == null) {
            return null;
        }
        return Base64.getUrlEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public String getPublicKeyString (String realmId) {
        KeyPair keyPair = realmToKeyPair.get(realmId);
        if (keyPair == null) {
            return null;
        }
        return Base64.getUrlEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public void addRealmWithKeys (String realmId, String privateKey, String publicKey) {
        KeyPair keyPair = createKeyPair(privateKey, publicKey);
        addRealmWithKeyPair(realmId, keyPair);
    }

    public void addRealmWithKeyPair (String realmId, KeyPair keyPair) {
        this.realmToKeyPair.put(realmId, keyPair);
    }

    private PrivateKey createPrivateKeyFromString (String privateKeyString) {
        byte[] encodedPrivateKey = Base64.getUrlDecoder().decode(privateKeyString);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey createPublicKeyFromString (String publicKeyString) {
        byte[] encodedPublicKey = Base64.getUrlDecoder().decode(publicKeyString);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedPublicKey);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyPair createKeyPair (String privateKeyString, String publicKeyString) {
        PrivateKey privateKey = createPrivateKeyFromString(privateKeyString);
        PublicKey publicKey = createPublicKeyFromString(publicKeyString);
        return new KeyPair(publicKey, privateKey);
    }

    private String[] getKeyStrings (KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        return new String[] {
                keyToString(privateKey),
                keyToString(publicKey)
        };
    }

    private String keyToString (Key key) {
        return Base64.getUrlEncoder().encodeToString(key.getEncoded());
    }
}
