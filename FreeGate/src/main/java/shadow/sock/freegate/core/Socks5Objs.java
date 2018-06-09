package shadow.sock.freegate.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;

public class Socks5Objs {
	private final ChannelHandlerContext ctx;
	private final Socks5CommandRequest request;
	private final boolean authRemote;
	
	public Socks5Objs(ChannelHandlerContext ctx, Socks5CommandRequest request, boolean authRemote) {
		super();
		this.ctx = ctx;
		this.request = request;
		this.authRemote = authRemote;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public Socks5CommandRequest getRequest() {
		return request;
	}
	
	public boolean isAuthRemote() {
		return authRemote;
	}
}
