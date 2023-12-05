package org.mifos.pheeConnectorCrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("org.mifos.pheeConnectorCrm")
public class PheeConnectorCrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(PheeConnectorCrmApplication.class, args);
	}

}
