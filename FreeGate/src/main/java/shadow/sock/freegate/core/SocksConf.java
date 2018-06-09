package shadow.sock.freegate.core;

public class SocksConf {
	private final static SocksConf INS = new SocksConf();
	private String name;
	private String pwd;
	private String key;
	private String remoteAddr;
	private int port;
	
	private SocksConf(){
		port = 2080;
		remoteAddr = "127.0.0.1";
	}
	
	public static SocksConf getConf(){
		return INS;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getRemoteAddr() {
		return remoteAddr;
	}
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
