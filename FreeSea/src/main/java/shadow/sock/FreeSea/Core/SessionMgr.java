package shadow.sock.FreeSea.Core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionMgr {
	private static Logger LOG = LoggerFactory.getLogger(SessionMgr.class);
	private static class LazyHolder{
		private static final SessionMgr INSTANCE = new SessionMgr();
	}
	private ConcurrentMap<String, String> connKeys;
	
	private SessionMgr(){
		connKeys = new ConcurrentHashMap<>();
	}
	
	public static SessionMgr getMgr(){
		return LazyHolder.INSTANCE;
	}
	
	public void putConnKey(String k, String v){
		LOG.debug("{}:{}", k, v);
		connKeys.put(k, v);
	}
	
	public String getConnKey(String k){
		String v = connKeys.get(k);
		LOG.debug("{}:{}", k, v);
		return v;
	}
	

}
