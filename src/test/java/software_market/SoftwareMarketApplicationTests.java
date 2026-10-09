package software_market;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(properties = "websocket.server-endpoint-exporter=false")
class SoftwareMarketApplicationTests {

	@Test
	void contextLoads() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		String password = "admin123";

		String encode = encoder.encode(password);

		System.out.println("明文密码: " + password);
		System.out.println("加密结果: " + encode);

		System.out.println(
				encoder.matches(password, encode)
		);
	}

}
