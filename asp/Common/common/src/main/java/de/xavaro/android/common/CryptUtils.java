package de.xavaro.android.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//
// Encryp and decrypt stuff.
//

public class CryptUtils
{
    private static final String LOGTAG = CryptUtils.class.getSimpleName();

    private static byte[] AESencrypt(byte[] key, byte[] decrypted) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec spec = new IvParameterSpec(new byte[ cipher.getBlockSize() ]);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, spec);
        byte[] encrypted = cipher.doFinal(decrypted);

        Log.d(LOGTAG,"encrypt:" + StaticUtils.hexBytesToString(key));
        Log.d(LOGTAG,"encrypt:" + StaticUtils.hexBytesToString(encrypted));

        return encrypted;
    }

    private static byte[] AESdecrypt(byte[] key, byte[] encrypted) throws Exception
    {
        Log.d(LOGTAG,"decrypt:" + StaticUtils.hexBytesToString(key));
        Log.d(LOGTAG,"decrypt:" + StaticUtils.hexBytesToString(encrypted));

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec spec = new IvParameterSpec(new byte[ cipher.getBlockSize() ]);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, spec);
        byte[] decrypted = cipher.doFinal(encrypted);

        return decrypted;
    }

    @Nullable
    private static byte[] AESmakeKey(String uuid)
    {
        return StaticUtils.getUUIDBytes(uuid);

        /*
        try
        {
            byte[] keyStart = passPhrase.getBytes();
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(keyStart);
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            return skey.getEncoded();
        }
        catch (NoSuchAlgorithmException ex)
        {
        }

        return null;
        */
    }

    @Nullable
    public static byte[] AESencrypt(String identity, String message)
    {
        try
        {
            JSONObject ident = IdentityManager.getInstance().getIdentity(identity);
            if (!ident.has("passPhrase")) return null;
            Log.d(LOGTAG,"encrypt:" + ident.getString("passPhrase"));
            byte[] aesKey = AESmakeKey(ident.getString("passPhrase"));
            return AESencrypt(aesKey, message.getBytes());
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static byte[] AESdecrypt(String identity, byte[] message)
    {
        try
        {
            JSONObject ident = IdentityManager.getInstance().getIdentity(identity);
            if (!ident.has("passPhrase")) return null;
            Log.d(LOGTAG,"decrypt:" + ident.getString("passPhrase"));
            byte[] aesKey = AESmakeKey(ident.getString("passPhrase"));
            return AESdecrypt(aesKey, message);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    private static PublicKey RSAgetPublicKeyFromBase64(String publicKey)
    {
        try
        {
            byte[] keyBytes = Base64.decode(publicKey, 0);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(spec);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    private static PrivateKey RSAgetPrivateKeyFromBase64(String privateKey)
    {
        try
        {
            byte[] keyBytes = Base64.decode(privateKey, 0);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            return fact.generatePrivate(keySpec);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static JSONObject RSAgenerateKeys()
    {
        try
        {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            KeyPair kp = kpg.genKeyPair();
            PublicKey publicKey = kp.getPublic();
            PrivateKey privateKey = kp.getPrivate();

            String publicKey64 = Base64.encodeToString(publicKey.getEncoded(), 0).trim();
            String privateKey64 = Base64.encodeToString(privateKey.getEncoded(), 0).trim();

            //Log.d(LOGTAG, "Public: " + publicKey64);
            //Log.d(LOGTAG, "Private: " + privateKey64);

            JSONObject keys = new JSONObject();

            keys.put("public", publicKey64);
            keys.put("private", privateKey64);

            return keys;
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String RSAgetPrivateKey(Context context)
    {
        try
        {
            JSONObject keys = RSAretrieveFromStorage(context);

            return keys.getString("private").trim();
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String RSAgetPublicKey(Context context)
    {
        try
        {
            JSONObject keys = RSAretrieveFromStorage(context);

            return keys.getString("public").trim();
        }
        catch (JSONException ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static JSONObject RSAretrieveFromStorage(Context context)
    {
        String filename = context.getPackageName() + ".rsakeys.json";

        if (! new File(context.getFilesDir(), filename).exists())
        {
            JSONObject keys = RSAgenerateKeys();

            RSAstoreIntoStorage(context, keys);

            return keys;
        }

        try
        {
            FileInputStream inputStream;
            byte[] content = new byte[ 4096 ];

            inputStream = context.openFileInput(filename);
            int xfer = inputStream.read(content);
            inputStream.close();

            return new JSONObject(new String(content,0,xfer));
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    public static void RSAstoreIntoStorage(Context context, JSONObject keys)
    {
        try
        {
            String filename = context.getPackageName() + ".rsakeys.json";
            FileOutputStream outputStream;

            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(keys.toString(2).getBytes());
            outputStream.close();
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }
    }

    @Nullable
    public static String RSAEncrypt(String publicKeyString, String passPhrase)
    {
        try
        {
            PublicKey publicKey = RSAgetPublicKeyFromBase64(publicKeyString);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(passPhrase.getBytes());

            return Base64.encodeToString(encryptedBytes, 0);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }

    @Nullable
    public static String RSADecrypt(String privateKeyString, String passPhrase)
    {
        try
        {
            byte[] encryptedBytes = Base64.decode(passPhrase, 0);
            PrivateKey privateKey = RSAgetPrivateKeyFromBase64(privateKeyString);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);
        }
        catch (Exception ex)
        {
            OopsService.log(LOGTAG, ex);
        }

        return null;
    }
}
