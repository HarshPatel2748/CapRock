package com.caprock.config;

import com.caprock.security.jwt.JwtAuthEntryPoint;
import com.caprock.security.jwt.JwtFilter;
import com.caprock.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthEntryPoint unauthorizedHandler;

    @Autowired
    private JwtFilter jwtFilter;

    //BCrypt bean -> used everywhere we hash or verify passwords
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //Wires our UserDetailsService + BCrypt together for Spring's auth system
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    //Exposes the AuthenticationManager so AuthController can use it
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }

    //The main security filter chain - defines all the rules
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                //Disable CSRF - not needed for stateless JWT APIs
                .csrf(AbstractHttpConfigurer::disable)

                //Use our custom 401 handler
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler))

                //Stateless - no sessions, no cookies
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //Route rules
                .authorizeHttpRequests(auth -> auth
                        //Public routes
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/payment/webhook").permitAll()
                        //Admin only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        //Everything else requires a valid JWT
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider())

                //Plug in our JWT filter BEFORE Spring's default auth filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
