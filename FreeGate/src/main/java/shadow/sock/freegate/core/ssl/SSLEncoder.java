package shadow.sock.freegate.core.ssl;

import shadow.sock.freegate.core.SocksConf;
import shadow.sock.freegate.util.SUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class SSLEncoder extends MessageToByteEncoder<ByteBuf>{
	public final static SSLEncoder SSLE = new SSLEncoder();
	private final byte[] key;
	private SSLEncoder(){
		key = SocksConf.getConf().getKey().getBytes();
	}
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg,
			ByteBuf out) throws Exception {
		/**init**/
		int tk = SUtils.getSecond(System.currentTimeMillis());
		byte []token = SFCrypto.getKey(key, tk);
		out.capacity(4 + msg.readableBytes());
		/**encrypt**/
		out.writeInt(tk);
		while(msg.readableBytes() > 0){
			byte b = msg.readByte();
			out.writeByte((int)SFCrypto.encode(b, token));
		}
	}
}
