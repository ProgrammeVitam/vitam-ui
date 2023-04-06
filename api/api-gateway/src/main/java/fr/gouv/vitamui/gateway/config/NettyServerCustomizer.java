package fr.gouv.vitamui.gateway.config;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class NettyServerCustomizer
    implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    private final int maxHttpHeaderSize;

    public NettyServerCustomizer(ServerProperties serverProperties) {
        maxHttpHeaderSize = Math.toIntExact(serverProperties.getMaxHttpHeaderSize().toBytes());
    }

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(
            server -> server.httpRequestDecoder(
                reqDecorator -> reqDecorator
                    .maxInitialLineLength(maxHttpHeaderSize)
                    .maxHeaderSize(maxHttpHeaderSize)
            )
        );
    }
}
