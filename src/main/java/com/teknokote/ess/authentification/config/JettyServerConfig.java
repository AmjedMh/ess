package com.teknokote.ess.authentification.config;

import org.apache.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "ess", name = "mode", havingValue = "PROD")
public class JettyServerConfig {

    @Value("${server.https.port}")
    int httpsPort;

    @Value("${server.port}")
    int httpPort;

    @Bean
    public JettyServletWebServerFactory jettyEmbeddedServletContainerFactory() {
        JettyServletWebServerFactory jettyContainer = new JettyServletWebServerFactory();

        jettyContainer.addServerCustomizers((Server server) -> {
             //HTTP
            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.setPort(httpPort);

             //HTTPS
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath("keystore.p12");
            sslContextFactory.setKeyStorePassword("12345678");
            sslContextFactory.setKeyStoreType("PKCS12");
            sslContextFactory.setCertAlias("https");

            HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.addCustomizer(new SecureRequestCustomizer());

            ServerConnector httpsConnector = new ServerConnector(
                    server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.toString()),
                    new HttpConnectionFactory(httpsConfig)
            );
            httpsConnector.setPort(httpsPort);

            //Create a Connector array and set it to the server
            Connector[] connectors = { httpConnector, httpsConnector };
            server.setConnectors(connectors);
        });

        return jettyContainer;
    }
}

