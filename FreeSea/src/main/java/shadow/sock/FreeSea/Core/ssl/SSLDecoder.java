package shadow.sock.FreeSea.Core.ssl;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class SSLDecoder extends ByteToMessageDecoder{
	private static Logger LOG = LoggerFactory.getLogger(SSLDecoder.class);
	private final byte []key;
	public SSLDecoder(){
		key = "ABCDEFGHIJKLMNOP".getBytes();
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		/**decrypt**/
		int len = in.readInt();
		if(len != in.readableBytes()){
			LOG.error("{} : {} != {}", ctx.channel().remoteAddress().toString(), len, 
					  in.readableBytes());
		}
		
		int tk = in.readInt();
		byte[]token = SFCrypto.getKey(key, tk);
		ByteBuf ob = ctx.alloc().buffer(in.readableBytes());
		while(in.readableBytes() > 0){
			byte b = in.readByte();
			ob.writeByte((int)SFCrypto.decode(b, token));
		}
		out.add(ob);
	}
}
