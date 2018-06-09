package shadow.sock.freegate.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.freegate.util.SUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;

@ChannelHandler.Sharable
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage>{
	private static Logger LOG = LoggerFactory.getLogger(SocksServerHandler.class);
	private static SocksServerHandler INSTANCE = null;
	
	public static SocksServerHandler getInstance(){
		if(INSTANCE == null){
			synchronized(SocksServerHandler.class){
				if(INSTANCE == null){
					INSTANCE = new SocksServerHandler();
				}
			}
		}
		
		return INSTANCE;
	}
	private SocksServerHandler(){
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksMessage msg)
			throws Exception {
		if(msg.version() == SocksVersion.SOCKS4a){
			socks4Read0(ctx, msg);
		}else if(msg.version() == SocksVersion.SOCKS5){
			socks5Read0(ctx, msg);
		}else{
			LOG.warn("{}:Unknow message:{}", ctx.channel().remoteAddress().toString());
			ctx.close();
		}	
	}
	
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        //throwable.printStackTrace();
        LOG.error("Get exceptions:{}", throwable.getMessage());
        SUtils.closeOnFlush(ctx.channel());
    }
	
	private void socks4Read0(ChannelHandlerContext ctx, SocksMessage msg){
		Socks4CommandRequest s4c = (Socks4CommandRequest) msg;
		 if(s4c.type() == Socks4CommandType.CONNECT){
			 LOG.info("socks4 cmd:[{}]", s4c.type().toString());
			 ctx.pipeline().addLast(new SocksServerConnectHandler());
			 ctx.pipeline().remove(this);
			 ctx.fireChannelRead(msg);
		 }else{
			 LOG.warn("Cmd:[{}], failed",s4c.type().toString());
			 ctx.close();
		 }
	}
	
	private void socks5Read0(ChannelHandlerContext ctx, SocksMessage msg){
		if(msg instanceof Socks5InitialRequest){
			ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
			ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
		}else if(msg instanceof Socks5PasswordAuthRequest){
			/**Get user name and pwd from msg**/
			ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
			ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
		}else if(msg instanceof Socks5CommandRequest){
			Socks5CommandRequest s5c = (Socks5CommandRequest) msg;
			if(s5c.type() == Socks5CommandType.CONNECT){
				ctx.pipeline().addLast(new SocksServerConnectHandler());
				ctx.pipeline().remove(this);
				ctx.fireChannelRead(msg);
			}else{
				LOG.warn("{}:socks5: error status",ctx.channel().remoteAddress().toString());
				ctx.close();
			}
		}else{
			LOG.warn("{}:socks5: error msg",ctx.channel().remoteAddress().toString());
			ctx.close();
		}	
	}
}
