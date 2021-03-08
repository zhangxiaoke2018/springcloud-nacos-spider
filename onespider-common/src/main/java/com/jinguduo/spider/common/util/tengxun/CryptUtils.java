package com.jinguduo.spider.common.util.tengxun;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Random;

@SuppressWarnings("all")
public class CryptUtils {

    public static final int BUFFER_BLOCK_SIZE = 4069;
    public static final int CONFUSE_DATA_SIZE = 16;

    public static byte[] des3EncodeECB(byte[] key, byte[] data) throws Exception {
        Key deskey = SecretKeyFactory.getInstance("desede").generateSecret(new DESedeKeySpec(key));
        Cipher cipher = Cipher.getInstance("desede/ECB/PKCS5Padding");
        cipher.init(1, deskey);
        return cipher.doFinal(data);
    }

    public static String ees3EncodeECB2Str(byte[] key, byte[] data) {
        byte[] by = new byte[0];
        String ret = "";
        try {
            return Base64.encodeToString(des3EncodeECB(key, data), 2);
        } catch (Exception e) {
            return ret;
        }
    }

    public static byte[] ees3DecodeECB(byte[] key, byte[] data) throws Exception {
        Key deskey = SecretKeyFactory.getInstance("desede").generateSecret(new DESedeKeySpec(key));
        Cipher cipher = Cipher.getInstance("desede/ECB/PKCS5Padding");
        cipher.init(2, deskey);
        return cipher.doFinal(data);
    }

    public static String ees3DecodeECB2Str(byte[] data) {
        byte[] key = {98, 97, 98, 102, 55, 51, 97, 57, 52, 49, 53, 54, 100, 49, 100, 57, 51, 49, 101, 56, 55, 102, 56, 55};
        byte[] by = new byte[0];
        String ret = "";
        try {
            String ret2 = new String(ees3DecodeECB(key, Base64.decode(data, 0)));
            return ret2;
        } catch (Exception e) {
            return ret;
        }
    }

    public static byte[] encrypt(byte[] value) {
        byte[] encryptValue = new byte[(value.length + 16)];
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 16; i++) {
            encryptValue[i] = (byte) random.nextInt(255);
        }
        System.arraycopy(value, 0, encryptValue, 16, value.length);
        return encryptValue;
    }

    public static byte[] decrypt(byte[] value) {
        byte[] decryptValue = new byte[(value.length - 16)];
        System.arraycopy(value, 16, decryptValue, 0, value.length - 16);
        return decryptValue;
    }


    public static void fillConfuseData(OutputStream os) throws IOException {
        Random random = new Random(System.currentTimeMillis());
        byte[] buffer = new byte[16];
        for (int i = 0; i < 16; i++) {
            buffer[i] = (byte) random.nextInt(255);
        }
        os.write(buffer);
    }

    public static int encrypt(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4069];
        int read = is.read(buffer);
        if (read != -1) {
            fillConfuseData(os);
        }
        int total = 0;
        while (read != -1) {
            os.write(buffer, 0, read);
            os.flush();
            total += read;
            read = is.read(buffer);
        }
        return total;
    }

    public static void decrypt(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4069];
        is.read(buffer, 0, 16);
        int read = is.read(buffer);
        while (read != -1) {
            os.write(buffer, 0, read);
            os.flush();
            read = is.read(buffer);
        }
    }
}
