//package org.xyp.demo.echo;
//
//import org.apache.catalina.connector.Connector;
//import org.apache.coyote.AbstractProtocol;
//import org.apache.coyote.ProtocolHandler;
//import org.apache.coyote.http2.Http2Protocol;
//import org.springframework.boot.util.LambdaSafe;
//import org.springframework.boot.web.embedded.tomcat.*;
//import org.springframework.boot.web.server.Ssl;
//import org.springframework.util.StringUtils;
//
//import java.util.LinkedHashSet;
//import java.util.Set;
//
//public class CustomizedTomcatServletWebServerFactory extends TomcatServletWebServerFactory {
//
//
//    private Set<TomcatProtocolHandlerCustomizer<?>> tomcatProtocolHandlerCustomizers = new LinkedHashSet<>();
//
//    private Set<TomcatConnectorCustomizer> tomcatConnectorCustomizers = new LinkedHashSet<>();
//    @Override
//    // Needs to be protected so it can be used by subclasses
//    protected void customizeConnector(Connector connector) {
//        int port = Math.max(getPort(), 0);
//        connector.setPort(port);
//        if (StringUtils.hasText(getServerHeader())) {
//            connector.setProperty("server", getServerHeader());
//        }
//        if (connector.getProtocolHandler() instanceof AbstractProtocol) {
//            customizeProtocol((AbstractProtocol<?>) connector.getProtocolHandler());
//        }
//        invokeProtocolHandlerCustomizers(connector.getProtocolHandler());
//        if (getUriEncoding() != null) {
//            connector.setURIEncoding(getUriEncoding().name());
//        }
//        // Don't bind to the socket prematurely if ApplicationContext is slow to start
//        connector.setProperty("bindOnInit", "false");
//        if (getHttp2() != null && getHttp2().isEnabled()) {
//            connector.addUpgradeProtocol(new Http2Protocol());
//        }
//        if (Ssl.isEnabled(getSsl())) {
//            customizeSsl(connector);
//        }
//        TomcatConnectorCustomizer compression = new CompressionConnectorCustomizer(getCompression());
//        compression.customize(connector);
//        for (TomcatConnectorCustomizer customizer : this.tomcatConnectorCustomizers) {
//            customizer.customize(connector);
//        }
//    }
//
//
//    @SuppressWarnings("unchecked")
//    private void invokeProtocolHandlerCustomizers(ProtocolHandler protocolHandler) {
//        LambdaSafe
//                .callbacks(TomcatProtocolHandlerCustomizer.class, this.tomcatProtocolHandlerCustomizers, protocolHandler)
//                .invoke((customizer) -> customizer.customize(protocolHandler));
//    }
//
//    private void customizeProtocol(AbstractProtocol<?> protocol) {
//        if (getAddress() != null) {
//            protocol.setAddress(getAddress());
//        }
//    }
//
//    @Override
//    private void customizeSsl(Connector connector) {
//        new SslConnectorCustomizer(getSsl().getClientAuth(), getSslBundle()).customize(connector);
//    }
//
//}
