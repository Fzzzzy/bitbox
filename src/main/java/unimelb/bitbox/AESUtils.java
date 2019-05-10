package unimelb.bitbox;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
 
public class AESUtils {
	
	/**
	 * <p>Discription:[encryption]</p>
	 * @param content JSON.toJSONString(Map<String, String> map) transfered json string
	 * @param key 
	 * @return String ciphertext
	 */
	public static String ecodes(String content, String key) {
		if (content == null || content.length() < 1) {
			return null;
		}
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(key.getBytes());
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] byteRresult = cipher.doFinal(byteContent);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteRresult.length; i++) {
				String hex = Integer.toHexString(byteRresult[i] & 0xFF);
				if (hex.length() == 1) {
					hex = '0' + hex;
				}
				sb.append(hex.toUpperCase());
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
 
	/**
	 * <p>Discription:[decryption]</p>
	 * @param content ciphertext
	 * @param key 
	 * @return String plaintext
	 */
	public static String dcodes(String content, String key) {
		if (content == null || content.length() < 1) {
			return null;
		}
		if (content.trim().length() < 19) {
			return content;
		}
		byte[] byteRresult = new byte[content.length() / 2];
		for (int i = 0; i < content.length() / 2; i++) {
			int high = Integer.parseInt(content.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(content.substring(i * 2 + 1, i * 2 + 2), 16);
			byteRresult[i] = (byte) (high * 16 + low);
		}
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom random=SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(key.getBytes());
			kgen.init(128, random);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] result = cipher.doFinal(byteRresult);
			return new String(result);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
//			
			String ciphertext = AESUtils.ecodes("{\"id\":1759,\"reservationId\":1511867174251,\"visitingPurpose\":1,\"officeBuildingId\":1,\"officeBuildingName\":\"1\",\"cityId\":1,\"cityName\":\"1\",\"reservationStatus\":1,\"visitorName\":\"1 \",\"visitorAreaCode\":\"1\",\"visitorPhone\":\"1\",\"visitorEmail\":null,\"remark\":null,\"visitingTime\":1,\"visitorNum\":1,\"visitingUnit\":null,\"messageNum\":null,\"photoUrl\":null,\"receptionUserName\":\"1\",\"receptionUserDomainAccount\":\"1\",\"visitingInvitationCode\":null,\"createUser\":\"1\",\"createTime\":1,\"createUserDomainAccount\":null,\"updateTime\":null,\"signTime\":null,\"alreadySignedInNum\":null,\"alreadySignedOutNum\":null,\"accompanyPersons\":null,\"visitingReason\":null,\"field1\":null,\"field2\":null,\"field3\":null,\"field4\":null,\"field5\":null,\"approveStatus\":1,\"levelOneName\":null,\"levelTwoName\":null,\"createPlatform\":1}",
					"AAAAB3NzaC1yc2EAAAADAQABAAABAQCymM7yjAoXWqlMoNvrAYU2PjOWaLDDFYZt51f1VjaWq4oelhYwH02SLeZ6rBUSAbyjxdFCsJ9Tzc/VEbZCtv8eRXWJWPvNrraSlA1c1u8zqI06XboLi6UoUGJ4lVI0+Y/3ljkyiQc+/se/B8ywD5+IOZ6a9sdY+I4P+BP6i74W+zvEp3czmdxpRVIq0bv0u7jWTBhcnYyohgxMQgObdLS2zBwju+nVh+y2zzPBMIx1FRy1rrMocsifExkII1Ll6xNllfQsRpCvr/q2tQjs9FgV7WqDQt8r+uh/aN/GvZA+6yy1CYofIocLWreIOaVTrEjJcVDnli9XwXIyVky1NmeN");
			System.out.println("ciphertext : " + ciphertext);
			
			String plaintext = AESUtils.dcodes(ciphertext, "AAAAB3NzaC1yc2EAAAADAQABAAABAQCymM7yjAoXWqlMoNvrAYU2PjOWaLDDFYZt51f1VjaWq4oelhYwH02SLeZ6rBUSAbyjxdFCsJ9Tzc/VEbZCtv8eRXWJWPvNrraSlA1c1u8zqI06XboLi6UoUGJ4lVI0+Y/3ljkyiQc+/se/B8ywD5+IOZ6a9sdY+I4P+BP6i74W+zvEp3czmdxpRVIq0bv0u7jWTBhcnYyohgxMQgObdLS2zBwju+nVh+y2zzPBMIx1FRy1rrMocsifExkII1Ll6xNllfQsRpCvr/q2tQjs9FgV7WqDQt8r+uh/aN/GvZA+6yy1CYofIocLWreIOaVTrEjJcVDnli9XwXIyVky1NmeN");
			System.out.println("plaintext : " + plaintext);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
