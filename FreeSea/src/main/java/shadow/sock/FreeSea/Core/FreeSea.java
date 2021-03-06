package shadow.sock.FreeSea.Core;

import java.util.concurrent.Executors;

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

public class FreeSea {
	private static Logger LOG = LoggerFactory.getLogger(FreeSea.class);
	private static class LazyHolder{
		private static final FreeSea INSTANCE = new FreeSea();
	}
	
	private EventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	
	public static FreeSea getServer(){
		return LazyHolder.INSTANCE;
	}
	
	private FreeSea(){
		bossGroup = null;
		workerGroup = null;
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
	
	public void run(){
		int port = Integer.parseInt(System.getProperty("port", "2080"));
		int cpus = Runtime.getRuntime().availableProcessors();
		int works = cpus * 2 + 2;
		LOG.info("Start running listen port:{}, worker {}", port, works);
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup(works, Executors.newCachedThreadPool());
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
		
//	private MultithreadEventLoopGroup getAcceptEventLoopGroup(){
//		if(Epoll.isAvailable()){
//			try{
//				/**use epoll mode**/
//				MultithreadEventLoopGroup meg = new EpollEventLoopGroup(1);
//				LOG.info("Use epoll mode");
//				return meg;
//			}catch(Throwable t){
//				LOG.warn("Epoll exceptions:{}, use Nio", t.getMessage());
//			}
//		}
//		LOG.info("Use Nio mode");
//		return new NioEventLoopGroup(1);
//	}
//	
//	private MultithreadEventLoopGroup getEventLoopGroup(int threads){
//		if(Epoll.isAvailable()){
//			try{
//				/**use epoll mode**/
//				MultithreadEventLoopGroup meg = new EpollEventLoopGroup(threads, Executors.newCachedThreadPool());
//				LOG.info("Use epoll mode");
//				return meg;
//			}catch(Throwable t){
//				LOG.warn("Epoll exceptions:{}, use Nio", t.getMessage());
//			}
//		}
//		LOG.info("Use Nio mode");
//		return new NioEventLoopGroup(threads, Executors.newCachedThreadPool());
//	}
	
	
}
