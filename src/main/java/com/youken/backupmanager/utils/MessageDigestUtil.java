package com.youken.backupmanager.utils;

import com.youken.backupmanager.io.MappedBiggerFileReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author 杨剑
 * @date 2019/12/25
 */
@Slf4j
public class MessageDigestUtil {

	private static final String ALGORITHM = "MD5";

	private static MessageDigest messageDigest;

	static {
		try {
			messageDigest = MessageDigest.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private MessageDigestUtil() {
	}

	public static String md5(@NonNull String filePath) {
		try (MappedBiggerFileReader reader = new MappedBiggerFileReader(filePath)) {
			int length;
			while ((length = reader.read()) != -1) {
				messageDigest.update(reader.getBuffer(), 0, length);
			}
			String md5 = new String(Hex.encodeHex(messageDigest.digest()));
			log.debug("md5: {}, filePath: {}", md5, filePath);
			return md5;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
