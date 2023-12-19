package com.paymentchain.product.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration {
    private static final String[] NO_AUTH_LIST = {
            "/v3/api-docs", //
            "/configuration/ui", //
            "/swagger-resources", //
            "/webjars/**", //
            "/login",
            "h2-console/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.csrf().disable()
                .authorizeHttpRequests((auth) -> auth
                        // Any URL contained in the list will be accessible to public
                        .antMatchers(NO_AUTH_LIST).permitAll()
                        // Any url containing the word 'Product', must be authenticated to perform a post request
                        .antMatchers(HttpMethod.POST, "/*product*/**").authenticated()
                        // Only admin role have access to Getters
                        .antMatchers(HttpMethod.GET, "/*product*/**").hasRole("ADMIN")
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());
        return httpSecurity.build();
    }

    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration cc = new CorsConfiguration();

        // Allowed headers in a cors request
        cc.setAllowedHeaders(Arrays.asList(
                "Origin, Accept",
                "X-Requested-With",
                "Content-Type",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "Authorization"
        ));

        // Expose headers
        cc.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // List of origins
        cc.setAllowedOrigins(List.of("/*"));
        cc.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "OPTIONS",
                "PUT",
                "PATCH"
        ));

        // Pattern of origins
        cc.addAllowedOriginPattern("*");
        cc.setMaxAge(Duration.ZERO);
        cc.setAllowCredentials(Boolean.TRUE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cc);
        return source;
    }
}
