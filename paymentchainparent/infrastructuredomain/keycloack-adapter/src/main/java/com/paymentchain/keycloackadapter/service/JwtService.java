package com.paymentchain.keycloackadapter.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class JwtService {

    @Value("${keycloak.jwk-set-uri}")
    private String jwksUrl;

    @Value("${keycloak.certs-id}")
    private String certsId;

    @Cacheable(value = "jwkCache")
    public Jwk getJwk() throws Exception {
        URL url = new URL(jwksUrl);
        UrlJwkProvider urlJwkProvider = new UrlJwkProvider(url);
        return urlJwkProvider.get(certsId.trim());
    }
}
