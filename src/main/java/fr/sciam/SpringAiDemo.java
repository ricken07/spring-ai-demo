package fr.sciam;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "spring-ai-demo")
public class SpringAiDemo implements AppShellConfigurator {
    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemo.class, args);
    }
}