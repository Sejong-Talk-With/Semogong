package Talk_with.semogong;

import Talk_with.semogong.configuration.SessionListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpSessionListener;

@SpringBootApplication
public class SemogongApplication {

	public static void main(String[] args) {
		SpringApplication.run(SemogongApplication.class, args);
	}

	@Bean
	public HttpSessionListener httpSessionListener(){
		return new SessionListener();
	}

}
