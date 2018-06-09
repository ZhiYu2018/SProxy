package shadow.sock.freegate.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreeServer {
	private static Logger LOG = LoggerFactory.getLogger(FreeServer.class);
	private String userKey = "ABCDEFGHIJKLMNOP";
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private static class LazyHolder{
		private static final FreeServer INSTANCE = new FreeServer();
	}
	
	private FreeServer(){
		bossGroup = null;
		workerGroup = null;
		SocksConf.getConf().setName("ZhiYu");
		SocksConf.getConf().setPwd("ZhiYu2018");
		SocksConf.getConf().setKey(userKey);
	}
	
	public static FreeServer getServer(){
		return LazyHolder.INSTANCE;
	}
	
	public void run(){
		int port = Integer.parseInt(System.getProperty("port", "1080"));
		int cpus = Runtime.getRuntime().availableProcessors();
		int works = cpus * 2 + 2;
		LOG.info("Start running listen port:{}, worker {}", port, works);
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup(works);
		try{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			       .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
			       .handler(new LoggingHandler(LogLevel.DEBUG))
                   .childHandler(new SocksServerInitializer());
            b.bind(port).sync().channel().closeFuture().sync();
		}catch(Throwable t){
			t.printStackTrace();
			LOG.error("Get exceptions:{}", t.getMessage());
		}finally{
			stop();
		}
	}
	
	public void stop(){
		if(bossGroup == null){
			bossGroup.shutdownGracefully();
			bossGroup = null;
		}
		if(workerGroup != null){
			workerGroup.shutdownGracefully();
			workerGroup = null;
		}
	}

}
