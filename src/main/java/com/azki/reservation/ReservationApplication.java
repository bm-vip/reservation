package com.azki.reservation;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.Objects;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class ReservationApplication {
    private final Environment environment;
    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @SneakyThrows
    @PostConstruct
    private void postConstruct() {
        String protocol = "https";
        if (Objects.isNull(environment.getProperty("server.ssl.key-store"))) {
            protocol = "http";
        }
        String port = environment.getProperty("server.port");
        String appName = environment.getProperty("spring.application.name");
        String url = protocol.concat("://localhost:").concat(port == null ? "null": port).concat("/swagger");

        log.info("""                       
                        ----------------------------------------------------------
                        \t\
                        Application '{}' is running! Access URLs:
                        \tLocal: \t\t{}
                        \t\
                        External: \t{}://{}:{}
                        \t\
                        Profile(s): {}
                        ----------------------------------------------------------""",
                appName, url, protocol,
                InetAddress.getLocalHost().getHostAddress(), port, environment.getActiveProfiles());
    }
}
