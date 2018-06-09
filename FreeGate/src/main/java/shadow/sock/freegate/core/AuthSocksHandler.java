package shadow.sock.freegate.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shadow.sock.freegate.util.SUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder;

public class AuthSocksHandler extends SimpleChannelInboundHandler<Socks5Message>{
	private static Logger LOG = LoggerFactory.getLogger(AuthSocksHandler.class);
	private Socks5Objs objs;
	public AuthSocksHandler(Socks5Objs objs){
		this.objs = objs;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx){
		/**send init msg**/
		List<Socks5AuthMethod> ams = new ArrayList<>();
		ams.add(Socks5AuthMethod.NO_AUTH);
		ams.add(Socks5AuthMethod.GSSAPI);
		ams.add(Socks5AuthMethod.PASSWORD);
		Socks5Message msg = new DefaultSocks5InitialRequest(ams);
		/**send init msg to remote**/
		ctx.writeAndFlush(msg);
		
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Socks5Message msg)throws Exception {
		/**check msg**/
		if(msg instanceof Socks5InitialResponse){
			/**send auth msg**/
			Socks5Message authMsg = new DefaultSocks5PasswordAuthRequest(SocksConf.getConf().getName(), 
					                                                     SocksConf.getConf().getPwd());
			/**add password decode**/
			ctx.channel().pipeline().replace("socks5c", "socks5c", new Socks5PasswordAuthResponseDecoder());
			ctx.channel().writeAndFlush(authMsg);
		}else if(msg instanceof Socks5PasswordAuthResponse){
			/**auth check**/
			ctx.channel().pipeline().replace("socks5c", "socks5c", new Socks5CommandResponseDecoder());
			
			Socks5PasswordAuthResponse amsg = (Socks5PasswordAuthResponse)msg;
			if(!amsg.status().isSuccess()){
				LOG.warn("Conneting to: {}:{} auth fail",objs.getRequest().dstAddr(), objs.getRequest().dstPort());
				fail(ctx);
			}else{
				Socks5Message cmsg = new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT,
						                                            objs.getRequest().dstAddrType(),
						                                            objs.getRequest().dstAddr(),
						                                            objs.getRequest().dstPort());
				ctx.writeAndFlush(cmsg);
			}
		}else if(msg instanceof Socks5CommandResponse){
			Socks5CommandResponse rmsg = (Socks5CommandResponse)msg;
			if(!rmsg.status().isSuccess()){
				LOG.warn("Conneting to: {}:{} connect fail", objs.getRequest().dstAddr(),objs.getRequest().dstPort());
				fail(ctx);
			}else{
				Object omsg = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS,
                        objs.getRequest().dstAddrType(),
                        objs.getRequest().dstAddr(),
                        objs.getRequest().dstPort());
				ChannelFuture responseFuture = objs.getCtx().channel().writeAndFlush(omsg);
				responseFuture.addListener(new ChannelFutureListener() {
		             @Override
		             public void operationComplete(ChannelFuture channelFuture) {
		            	 SUtils.safeRemove(ctx.pipeline(), AuthSocksHandler.this);
		            	 /**Out:In**/
		                 ctx.pipeline().addLast(new RelayHandler(objs.getCtx().channel()));
		                 /**In:Out**/
		                 objs.getCtx().pipeline().addLast(new RelayHandler(ctx.channel()));
		             }
		         });
			}
		}
		else{
			LOG.warn("Msg type is error, may be status error");
			SUtils.closeOnFlush(ctx.channel());
			Object lmsg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, objs.getRequest().dstAddrType());
			objs.getCtx().channel().writeAndFlush(lmsg);
			SUtils.closeOnFlush(objs.getCtx().channel());
		}
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        throwable.printStackTrace();
        LOG.error("Get exceptions:{}", throwable.getMessage());
        SUtils.closeOnFlush(ctx.channel());
        
        Object lmsg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, objs.getRequest().dstAddrType());
        objs.getCtx().channel().writeAndFlush(lmsg);
		SUtils.closeOnFlush(objs.getCtx().channel());
    }
    
    private void fail(ChannelHandlerContext ctx){
		SUtils.closeOnFlush(ctx.channel());
		Object lmsg = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, 
				                                       objs.getRequest().dstAddrType());
		objs.getCtx().channel().writeAndFlush(lmsg);
		SUtils.closeOnFlush(objs.getCtx().channel());
    }

}
