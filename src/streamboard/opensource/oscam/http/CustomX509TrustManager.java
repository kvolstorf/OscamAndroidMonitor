package streamboard.opensource.oscam.http;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * A custom TrustManager for X509 certificates which allows a more lazy checking if user wants that.
 */
public class CustomX509TrustManager implements X509TrustManager {

	private X509TrustManager _standardTrustManager = null;

	/**
	 * Constructor
	 */
	public CustomX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init(keystore);
		TrustManager[] trustmanagers = factory.getTrustManagers();
		if (trustmanagers.length == 0) {
			throw new NoSuchAlgorithmException("no trust manager found");
		}
		_standardTrustManager = (X509TrustManager) trustmanagers[0];
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String authType)
	 */
	public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
		_standardTrustManager.checkClientTrusted(certificates, authType);
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String authType)
	 */
	public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
		// Check if cert is expired (only if there's no cert chain)
		try {
			if ((certificates != null) && (certificates.length == 1)) certificates[0].checkValidity();
		} catch (Exception e2){
			// todo: show popup or throw error, maybe also save this permanently per profile?
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {}
		for(int i=certificates.length - 1; i > 0; --i) md.update(certificates[i].getEncoded());
		String res = new String(md.digest());
		// todo: check against hash in settings, not against blank string
		if(!res.equals("")){
			try{
				_standardTrustManager.checkServerTrusted(certificates, authType);
			} catch (Exception e){				
				// todo: not valid according to standard trust, show popup so that user may add sha hash to settings to permanently allow certificate			
			}
		}
	}

	/**
	 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
	 */
	public X509Certificate[] getAcceptedIssuers() {
		return _standardTrustManager.getAcceptedIssuers();
	}
}
