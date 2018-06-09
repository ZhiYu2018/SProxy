package shadow.sock.freegate.core.ssl;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import shadow.sock.freegate.util.SUtils;

public class SSCrypto {
	private final static int BUF_SIZE = 128;
	private final SecretKeySpec key;
	private final IvParameterSpec iv;
	private Cipher cp;
	public SSCrypto(String key){
		this.key = new SecretKeySpec(getUTF8Bytes(key),"AES");
		this.iv = new IvParameterSpec(getUTF8Bytes(key));
		try{
			cp = Cipher.getInstance("AES/CBC/PKCS5Padding");
		}catch(Throwable t){
			cp = null;
		}
	}
	
	public void init(boolean isEncrypt){
		try{
			if(isEncrypt){
				cp.init(Cipher.ENCRYPT_MODE, this.key, iv);
			}else{
				cp.init(Cipher.DECRYPT_MODE, this.key, iv);
			}
		}catch(Throwable t){
			
		}
	}
	
	public byte[] encrypt(byte []input){
		try{
			byte[] buf = cp.doFinal(input);
			return buf;
		}catch(Throwable t){
			t.printStackTrace();
			return null;
		}
	}
	
	public int encrypt(InputStream is, OutputStream os){
		CipherOutputStream cos = null;
		try{
			cos = new CipherOutputStream(os, cp); 
			byte[]buf = new byte[BUF_SIZE];
			int len = 0;
			while(true){
				int r = is.read(buf);
				if(r == -1){
					break;
				}
				len = len + r;
				cos.write(buf, 0, len);
			}
			SUtils.safeClose(cos);
			if(len == 0){
				return -1;
			}
			return 0;
		}catch(Throwable t){
			SUtils.safeClose(cos);
			return -1;
		}
	}
	
	public byte[] decrypt(byte []input){
		try{
			byte[] buf = cp.doFinal(input);
			return buf;
		}catch(Throwable t){
			return null;
		}
	}
	
	/****/
	public int decrypt(InputStream is, OutputStream os){
		CipherInputStream cis = null;
		try{
			cis = new CipherInputStream(is, cp);
			byte[]buf = new byte[BUF_SIZE];
			int len = 0;
			while(true){
				len = cis.read(buf);
				if(len == -1){
					break;
				}
				os.write(buf, 0, len);
			}
			SUtils.safeClose(cis);
			return 0;
		}catch(Throwable t){
			SUtils.safeClose(cis);
			return -1;
		}
	}
	
	public int getOutSize(int len){
		if(cp == null){
			return -1;
		}
		
		return cp.getOutputSize(len);
	}
	
	private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
	}

}
