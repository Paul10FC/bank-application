package com.paymentchain.keycloackadapter.web.controller;

import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.paymentchain.keycloackadapter.exception.BusinessRuleException;
import com.paymentchain.keycloackadapter.service.JwtService;
import com.paymentchain.keycloackadapter.service.KeycloakRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
public class IndexController {
    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private final KeycloakRestService restService;
    private final JwtService jwtService;

    @Autowired
    public IndexController(KeycloakRestService restService, JwtService jwtService) {
        this.restService = restService;
        this.jwtService = jwtService;
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@RequestHeader("Authorization") String authHeader) throws BusinessRuleException {
        try {
            DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());

            // check JWT is valid
            Jwk jwk = jwtService.getJwk();
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);

            // check JWT roles is correct
            List<String> roles = (List) jwt.getClaim("realm_access").asMap().get("roles");

            // check JWT is still active
            Date expiryDate = jwt.getExpiresAt();
            if (expiryDate.before(new Date())) throw new Exception("token expired");

            // all validation passed
            HashMap<String, Integer> hashMap = new HashMap<>();
            roles.forEach(role -> hashMap.put(role, role.length()));

            return ResponseEntity.ok(hashMap);
        } catch (Exception e){
            logger.error("exception: {} ", e.getMessage());
            throw new BusinessRuleException("01", e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/valid")
    public ResponseEntity<?> valid(@RequestHeader("Authorization") String authHeader) throws BusinessRuleException {
        try {
            restService.checkValidity(authHeader);
            return ResponseEntity.ok(new HashMap<String, String>(){{
                put("is valid", "true");
            }});
        } catch (Exception e){
            logger.error("token is not valid, exception : {}", e.getMessage());
            throw new BusinessRuleException("is_valid", "false", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(String username, String password){
        String login = restService.login(username, password);
        return ResponseEntity.ok(login);
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@RequestParam(value = "refresh_token", name = "refresh_token") String refresToken) throws BusinessRuleException{
        try {
            restService.logout(refresToken);
            return ResponseEntity.ok(new HashMap<String, String>(){{
                put("logout", "true");
            }});
        } catch (Exception e){
            logger.error("unable to logout, exception: {} ", e.getMessage());
            throw new BusinessRuleException("logout", "False", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) throws BusinessRuleException {
        try {
            return ResponseEntity.ok(restService.refresh(refreshToken));
        } catch (Exception e){
            logger.error("unable to refresh, exception: {}", e.getMessage());
            throw new BusinessRuleException("refresh", "False", HttpStatus.FORBIDDEN);
        }
    }
}
