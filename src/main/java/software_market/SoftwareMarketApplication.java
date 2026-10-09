package software_market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SoftwareMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoftwareMarketApplication.class, args);
	}

}
