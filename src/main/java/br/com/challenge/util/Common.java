package br.com.challenge.util;

/**
 * Represents common methods.
 * 
 * @author Publio B. Guirelli
 * @version 1.0 03/2019
 * @since 1.0 03/2019
 */
public class Common {

	/**
	 * Encode a string dealing with special characters.
	 * 
	 * @param originalWord
	 *            The original string.
	 * 
	 * @return The string treated.
	 */
	public static String encode(String originalWord) {
		return originalWord.replace("'", "").replace("\\", "").trim();
	}

}
