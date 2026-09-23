package smartdesk.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class SmartDeskBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartDeskBookingApplication.class, args);
		System.out.println("SB running");
	}

}
