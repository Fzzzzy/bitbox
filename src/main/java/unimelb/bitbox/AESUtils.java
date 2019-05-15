package unimelb.bitbox;

import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AESUtils {

	private static final String IV_STRING = "16-Bytes--String";

	public static final String DEFAULT_KEY = "1bd83b249a414036";

    //generate random key
    public static String generateKey() {
        String key = UUID.randomUUID().toString();
        key = key.replace("-", "").substring(0, 16);// 
        return key;
    }

    public static String encryptData(String key, String content) {
        byte[] encryptedBytes = new byte[0];
        try {
            byte[] byteContent = content.getBytes("UTF-8");
            byte[] enCodeFormat = key.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            encryptedBytes = cipher.doFinal(byteContent);
            return new String(Base64.getEncoder().encode(encryptedBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptData(String key, String content) {
        try {
            // base64 decode
            byte[] encryptedBytes = Base64.getDecoder().decode(content);
            byte[] enCodeFormat = key.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(enCodeFormat, "AES");
            byte[] initParam = IV_STRING.getBytes();
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initParam);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] result = cipher.doFinal(encryptedBytes);
            return new String(result, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args) {
		String cipherText = AESUtils.encryptData("F431E6FF9051DA07", "LIST_PEERS_REQUEST");
		String plainText = AESUtils.decryptData("F431E6FF9051DA07", cipherText);
		
		System.out.println("aes encrypted: " + cipherText);
		System.out.println("aes decrypted: " + plainText);
	}
    
}
