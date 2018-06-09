package shadow.sock.FreeSea.Core.ssl;

import java.util.Arrays;

public class SFCrypto {
	public static byte [] getKey(byte[]key, int v){
		byte[] t = new byte[16 + key.length];
		int s = v;
		int o = 0;
		for(; o < key.length; o++){
			t[o] = key[o];
		}
		while(s > 0){
			int r = (s % 26);
			t[o] = (byte) ('A' + r);
			o++;
			s = s/26;
		}
		return Arrays.copyOf(t, o);
	}
	
	public static byte encode(byte s, byte[] k){
		byte e = s;
		for(int i = 0; i < k.length; i++){
			e = (byte)(e ^ k[i]);
		}
		return e;
	}
	
	public static byte decode(byte d, byte[] k){
		byte s = d;
		for(int i = k.length; i >0; i--){
			s = (byte)(s ^ k[i-1]);
		}
		return s;
	}
}
