package ar.com.platea.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {

	private final CatalogSeeder catalogSeeder;

	public DataSeeder(CatalogSeeder catalogSeeder) {
		this.catalogSeeder = catalogSeeder;
	}

	@Override
	public void run(ApplicationArguments args) {
		catalogSeeder.seed();
	}
}
