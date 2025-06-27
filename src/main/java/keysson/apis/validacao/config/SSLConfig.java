package keysson.apis.validacao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SSLConfig {

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.ssl.key-store:}")
    private String keyStore;

    @Value("${server.ssl.key-store-password:}")
    private String keyStorePassword;

    @Value("${server.ssl.key-alias:}")
    private String keyAlias;

    @Bean
    @Profile("!test") // Não aplicar em testes
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        
        if (sslEnabled) {
            tomcat.addConnectorCustomizers(connector -> {
                connector.setScheme("https");
                connector.setSecure(true);
                connector.setProperty("SSLEnabled", "true");
                connector.setProperty("keystoreFile", keyStore);
                connector.setProperty("keystorePass", keyStorePassword);
                connector.setProperty("keyAlias", keyAlias);
                connector.setProperty("keystoreType", "PKCS12");
            });
        }
        
        return tomcat;
    }
} 