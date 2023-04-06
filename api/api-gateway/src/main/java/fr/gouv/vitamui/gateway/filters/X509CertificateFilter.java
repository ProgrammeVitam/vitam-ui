package fr.gouv.vitamui.gateway.filters;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

@Order(1)
@Component
public class X509CertificateFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(X509CertificateFilter.class);

    @Value("${authn.client-certificate-header-name}")
    private String clientCertificateHeaderName;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        SslInfo sslInfo = req.getSslInfo();
        if (sslInfo != null) {
            X509Certificate[] certs = sslInfo.getPeerCertificates();
            if (certs != null && certs.length > 0) {
                ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(httpHeaders -> {
                        X509Certificate certificate = certs[0];
                        try {
                            certificate.checkValidity();
                            String encodedCert = new String(Base64.encodeBase64(certificate.getEncoded()));
                            httpHeaders.set(clientCertificateHeaderName, encodedCert);
                        } catch (CertificateEncodingException | CertificateExpiredException
                            | CertificateNotYetValidException e) {
                            logger.error("Certificate is invalid : {}", certificate, e);
                        }

                    }).build();
                return chain.filter(exchange.mutate().request(request).build());
            }

        }
        return chain.filter(exchange);
    }
}
