package keysson.apis.validacao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class HttpToHttpsRedirectConfig {

    @Value("${server.port:8089}")
    private int serverPort;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        
        if (sslEnabled) {
            // Adiciona redirecionamento HTTP para HTTPS
            tomcat.addConnectorCustomizers(connector -> {
                connector.setPort(serverPort);
                connector.setRedirectPort(serverPort);
                connector.setProperty("redirectPort", String.valueOf(serverPort));
            });
        }
        
        return tomcat;
    }
} 