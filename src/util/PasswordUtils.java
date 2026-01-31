package util;

import org.mindrot.jbcrypt.BCrypt;


//Mã hóa mật khẩu sử dụng BCrypt

public final class PasswordUtils {
	private static final int BCRYPT_LOG_ROUNDS = 12; 

	private PasswordUtils() {
	}


//Mã hóa mật khẩu thô bằng BCrypt để lưu vào cột password_hash.

	public static String hash(String rawPassword) {
		if (rawPassword == null || rawPassword.isBlank()) {
			throw new IllegalArgumentException("Mật khẩu là bắt buộc");
		}
		return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
	}


//Xác minh mật khẩu thô so với hash được lưu trữ (hoặc fallback plaintext cũ).
	public static boolean matches(String rawPassword, String storedHashOrPlaintext) {
		if (rawPassword == null || storedHashOrPlaintext == null || storedHashOrPlaintext.isBlank()) {
			return false;
		}

		boolean looksLikeBCrypt = storedHashOrPlaintext.startsWith("$2a$")
				|| storedHashOrPlaintext.startsWith("$2b$")
				|| storedHashOrPlaintext.startsWith("$2y$");

		if (looksLikeBCrypt) {
			try {
				return BCrypt.checkpw(rawPassword, storedHashOrPlaintext);
			} catch (Exception ex) {
				return false; // định dạng hash bị hỏng
			}
		}

		// Chế độ cũ: chấp nhận text thô nếu DB vẫn lưu trữ nó.
		return rawPassword.equals(storedHashOrPlaintext);
	}

}