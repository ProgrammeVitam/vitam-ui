package fr.gouv.vitamui.customersadmin;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.customersadmin.services.CustomerMgtSrvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CustomersAdminApplication {
    protected static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomersAdminApplication.class);
    @Autowired
    private ConfigurableApplicationContext context;


    @Autowired
    private CustomerMgtSrvc customerMgtSrvc;

    public static void main(String[] args) {
        SpringApplication.run(CustomersAdminApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            LOGGER.info("Let's start creating customers:");
            customerMgtSrvc.createCustomersWithUsers();
            LOGGER.info("Application terminated:");
            System.exit(SpringApplication.exit(context));
        };
    }
}
