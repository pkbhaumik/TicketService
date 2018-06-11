package pb.ticketing.service;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import pb.ticketing.service.controller.TicketServiceController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootConfiguration
@EnableSwagger2
public class Configuration {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();
	}

	@Bean
	public TicketServiceController ticketServiceController() {
		return new TicketServiceController();
	}
}
