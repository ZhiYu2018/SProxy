package shadow.sock.freegate.core.ssl;

public class SSCryptoFactory {
	private static final ThreadLocal<SSCrypto> _crypto = new ThreadLocal<>();
	
	public static SSCrypto get(){
		SSCrypto sscrypto = _crypto.get();
		if(null == sscrypto){
			sscrypto = new SSCrypto("ABCDEFGHIJKLMNOP");
			_crypto.set(sscrypto);
		}
		return sscrypto;
	}
}
