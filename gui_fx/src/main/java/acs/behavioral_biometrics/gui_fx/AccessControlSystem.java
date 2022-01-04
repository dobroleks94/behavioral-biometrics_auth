package acs.behavioral_biometrics.gui_fx;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"acs.behavioral_biometrics"})
@EntityScan(basePackages = {"acs.behavioral_biometrics"})
@EnableJpaRepositories(basePackages = {"acs.behavioral_biometrics"})
public class AccessControlSystem {
    public static void main(String[] args) {
        Application.launch(AccessControlSystemFX.class, args);
    }
}
