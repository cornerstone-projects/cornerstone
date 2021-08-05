package io.example.showcase;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.core.DefaultApplication;

@SpringBootApplication
@EnableJpaRepositories
//required here since annotated somewhere else, JpaRepositoriesAutoConfiguration is skipped
@EntityScan
//required here since annotated somewhere else, see JpaBaseConfiguration::getPackagesToScan
public class MainApplication extends DefaultApplication {

	public static void main(String[] args) throws Exception {
		start(args);
	}

}
