package com.aminejava.taskmanager.system.services;

import com.aminejava.taskmanager.controller.tool.AppTool;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.securityconfig.jwt.keys.KeyGeneratorTool;
import com.aminejava.taskmanager.system.dto.EmailResponse;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EncryptionService {

    private final KeyGeneratorTool keyGeneratorTool;
    private final AppTool appTool;

    public EncryptionService(KeyGeneratorTool keyGeneratorTool, AppTool appTool) {
        this.keyGeneratorTool = keyGeneratorTool;

        this.appTool = appTool;
    }

    @Bean
    private PrivateKey getPrivateKeyDecrypt() throws NoSuchAlgorithmException, IOException {
        if (keyGeneratorTool.getPrivateKey() == null) {
            keyGeneratorTool.createKeys();
            return keyGeneratorTool.getPrivateKey();
        }
        return keyGeneratorTool.getPrivateKey();
    }

    @Bean
    private PublicKey getPublicKeyDecrypt() throws NoSuchAlgorithmException, IOException {
        if (keyGeneratorTool.getPublicKey() == null) {
            keyGeneratorTool.createKeys();
            return keyGeneratorTool.getPublicKey();
        }
        return keyGeneratorTool.getPublicKey();
    }

    public String encryptUserData(String data) {

        byte[] dataToBytes = data.getBytes();
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyDecrypt());
            byte[] encryptedKey = cipher.doFinal(dataToBytes);
            return Base64.getUrlEncoder().encodeToString(encryptedKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException exception) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, exception.getLocalizedMessage());
            throw new GlobalException(exception.getMessage());
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }
    }

    /**
     * Decrypt AES properties (as Json) of Bitmarck using our sign PrivateKey
     * Bitmarck has utilized our signed public key, transmitting it through the initial API call to their system
     */
    public EmailResponse decryptUserData(String encryptedData) {

        byte[] dataBytes = Base64.getUrlDecoder().decode(encryptedData);
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyDecrypt());
            byte[] decryptKey = cipher.doFinal(dataBytes);
            String decryptData = new String(decryptKey);
            JSONObject jsonObject=new JSONObject(decryptData);

            String email =jsonObject.getString("email");
            String username = jsonObject.getString("username");
            String timeOfCreateEncryptedData = jsonObject.getString("now");
            timeOfCreateEncryptedData=timeOfCreateEncryptedData.substring(0,timeOfCreateEncryptedData.indexOf('.'));
            ZonedDateTime timeCreation = appTool.convertStringToZonedDateTime2(timeOfCreateEncryptedData);
            ZonedDateTime timeCreationPlusDay = timeCreation.plusMinutes(5);
            EmailResponse emailResponse = new EmailResponse();
            emailResponse.setEmail(email);
            emailResponse.setUsername(username);
            if (appTool.nowTime().isBefore(timeCreationPlusDay)) {
                emailResponse.setActive(true);
                emailResponse.setMessage("valid email ");
            } else {
                emailResponse.setActive(false);
                emailResponse.setMessage("The url is over 24 Day, you get soon a new path, please click on the url ");
            }
            return emailResponse;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException exception) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, exception.getLocalizedMessage());
            throw new GlobalException(exception.getMessage());
        } catch (IOException e) {
            throw new GlobalException(e.getMessage());
        }
    }

}
