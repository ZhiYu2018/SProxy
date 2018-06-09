package shadow.sock.FreeSea;

import javax.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import shadow.sock.FreeSea.Core.FreeSea;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Application 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
             System.out.println("Let's inspect the beans provided by Spring Boot:");
             FreeSea.getServer().run();
        };
    }
    
    @PreDestroy 
    public void  dostory(){ 
    	FreeSea.getServer().stop();
    }
    
}
