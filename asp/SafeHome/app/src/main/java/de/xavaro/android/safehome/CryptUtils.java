package de.xavaro.android.safehome;

import android.util.Base64;
import android.util.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//
// Encryp and decrypt stuff.
//

public class CryptUtils
{
    private static final String LOGTAG = CryptUtils.class.getSimpleName();

    public static byte[] encrypt(byte[] key, byte[] decrypted) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(decrypted);
        return encrypted;
    }

    public static byte[] decrypt(byte[] key, byte[] encrypted) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static void MakeKey(String strkey)
    {
        try
        {
            byte[] keyStart = strkey.getBytes();
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(keyStart);
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            byte[] key = skey.getEncoded();
        }
        catch (NoSuchAlgorithmException ex)
        {
        }
    }

    KeyPairGenerator kpg;
    KeyPair kp;
    PublicKey publicKey;
    PrivateKey privateKey;
    byte [] encryptedBytes,decryptedBytes;
    Cipher cipher,cipher1;

    public static void generateRSAKeys()
    {
        try
        {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            PublicKey publicKey = kp.getPublic();
            PrivateKey privateKey = kp.getPrivate();

            Log.d(LOGTAG, "Public: " + Base64.encodeToString(publicKey.getEncoded(), 0));
            Log.d(LOGTAG, "Private: " + Base64.encodeToString(privateKey.getEncoded(), 0));
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
        }
    }

    public byte[] RSAEncrypt(final String plain)
    {
        try
        {

            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedBytes = cipher.doFinal(plain.getBytes());

            return encryptedBytes;
        }
        catch (Exception ex)
        {

        }

        return null;
    }

    public byte[] RSADecrypt(final byte[] encryptedBytes)
    {

        try
        {
            cipher1 = Cipher.getInstance("RSA");
            cipher1.init(Cipher.DECRYPT_MODE, privateKey);
            decryptedBytes = cipher1.doFinal(encryptedBytes);

            return decryptedBytes;
        }
        catch (Exception ex)
        {

        }

        return null;
    }
}
