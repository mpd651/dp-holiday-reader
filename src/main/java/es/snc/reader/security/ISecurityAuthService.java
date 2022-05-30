package es.snc.reader.security;

public interface ISecurityAuthService {

	/**
	 * Calls Security API to get token.
	 * <p>
	 * /authenticate
	 *
	 * @return null if not found or failed
	 */
	String authenticate();
	
}
