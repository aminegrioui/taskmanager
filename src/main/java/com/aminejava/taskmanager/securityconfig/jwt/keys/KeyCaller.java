package com.aminejava.taskmanager.securityconfig.jwt.keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class KeyCaller {

    @Value("${publickey}")
    private String publicKeyPath;

    @Value("${privatekey}")
    private String privateKeyPath;

    public PrivateKey loadPrivateAuthKeyFromFile() throws Exception {

        try {
//            byte[] fileContent = Files.readAllBytes(Paths.get(new File(privateKeyPath).getPath()));
            List<String> list = Files.readAllLines(Paths.get(new File(privateKeyPath).getPath()));

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(list.get(1)));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, exception.getMessage());
            throw new Exception(exception.getMessage());
        }
    }

    public PublicKey loadPublicAuthKeyFromFile() throws Exception {

        try {
            List<String> list = Files.readAllLines(Paths.get(new File(publicKeyPath+"/").getPath()));
//            byte[] fileContent = Files.readAllBytes(Paths.get(new File(publicKeyPath).getPath()));


            X509EncodedKeySpec ks = new X509EncodedKeySpec(Base64.getDecoder().decode(list.get(1)));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, exception.getMessage());
            throw new Exception(exception.getMessage());
        }
    }


}
