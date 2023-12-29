package com.paymentchain.keycloackadapter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakRestService {

    private final RestTemplate restTemplate;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.authorization-grant-type}")
    private String grantType;

    @Value("${keycloak.authorization-grant-type-refresh}")
    private String grantTypeRefresh;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.scope}")
    private String scope;

    @Autowired
    public KeycloakRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String login(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username",username);
        map.add("password",password);
        map.add("client_id",clientId);
        map.add("grant_type",grantType);
        map.add("client_secret",clientSecret);
        map.add("scope",scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, new HttpHeaders());
        return restTemplate.postForObject(keycloakTokenUri, request, String.class);
    }

    /**
     *  a successful user token will generate http code 200, other than that will create an exception
     *
     * @param token
     * @return
     * @throws Exception
     */
    public String checkValidity(String token) throws Exception {
        return getUserInfo(token);
    }

    /**
     *  logging out and disabling active token from keycloak
     *
     * @param refreshToken
     */
    public void logout(String refreshToken) throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id",clientId);
        map.add("client_secret",clientSecret);
        map.add("refresh_token",refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        restTemplate.postForObject(keycloakLogout, request, String.class);
    }

    public List<String> getRoles(String token) throws Exception {
        String response = getUserInfo(token);

        // get roles
        Map map = new ObjectMapper().readValue(response, HashMap.class);
        return (List<String>) map.get("roles");
    }

    private String getUserInfo(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        return restTemplate.postForObject(keycloakUserInfo, request, String.class);
    }

    /**
     *  logging out and disabling active token from keycloak
     *
     * @param refreshToken
     */
    public String refresh(String refreshToken) throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id",clientId);
        map.add("grant_type",grantTypeRefresh);
        map.add("refresh_token",refreshToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        return restTemplate.postForObject(keycloakTokenUri, request, String.class);
    }

}
