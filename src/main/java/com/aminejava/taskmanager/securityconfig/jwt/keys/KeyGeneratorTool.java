package com.aminejava.taskmanager.securityconfig.jwt.keys;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Service
@Data
public class KeyGeneratorTool {

    @Autowired
    ResourceLoader resourceLoad;

    @Value("${server.bitmarck.publickey.path}")
    private String publicKeyPath;

    @Value("${server.bitmarck.privatekey.path}")
    private String privateKeyPath;


    private PublicKey publicKey;

    private PrivateKey privateKey;

    public void createKeys() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom();
        keyPairGenerator.initialize(2048, secureRandom);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        savePrivateKey();
        savePublicKey();
    }

    private void savePublicKey() throws IOException {
        Resource resource = new ClassPathResource(publicKeyPath);
        File publicFile = resource.getFile();

        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        Writer writer = new FileWriter(publicFile);
        writer.write(Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded()));
        writer.close();
    }

    private void savePrivateKey() throws IOException {
        Resource resource = new ClassPathResource(privateKeyPath);
        File privateFile = resource.getFile();
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        Writer writer = new FileWriter(privateFile);
        writer.write(Base64.getEncoder().encodeToString(pkcs8EncodedKeySpec.getEncoded()));
        writer.close();
    }

    public PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Resource resource = new ClassPathResource(privateKeyPath);
        File privateFile = resource.getFile();
        byte[] fileContent = Files.readAllBytes(privateFile.toPath());
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(fileContent));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }

    public PublicKey loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Resource resource = new ClassPathResource(publicKeyPath);
        File publicFile = resource.getFile();
        byte[] fileContent = Files.readAllBytes(publicFile.toPath());
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(fileContent));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }

}
