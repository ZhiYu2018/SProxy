package shadow.sock.freegate.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

public final class SSHeartbeat extends ChannelInboundHandlerAdapter{
	 private static final ByteBuf HS = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("HEARTBEAT", CharsetUtil.ISO_8859_1)); 
	 @Override
     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
         if (evt instanceof IdleStateEvent) {
              ctx.writeAndFlush(HS.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);  //3
         } else {
             super.userEventTriggered(ctx, evt);  //4
         }
	 }
}
