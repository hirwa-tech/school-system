package com.hirwa.classprogram.security;

import com.hirwa.classprogram.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .maximumSessions(1)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/index", "/about", "/academic", "/admission", "/contact", "/news/**", "/gallery/**", "/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**", "/api/**", "/uploads/**", "/ds/login", "/ds/authenticate").permitAll()
                .requestMatchers("/student/quick-login").permitAll()
                .requestMatchers("/ds/login", "/ds/authenticate").permitAll()
                .requestMatchers("/news/**", "/gallery/**").permitAll()
                .requestMatchers("/news/create", "/news/*/edit", "/news/*/delete", "/gallery/create", "/gallery/*/delete").hasRole("DS")
                .requestMatchers("/classroom/**").hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers("/notes/**").hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers("/homework/**").hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers("/quiz/**").hasAnyRole("TEACHER", "STUDENT")
                .requestMatchers("/dashboard", "/account/**").hasAnyRole("TEACHER", "STUDENT", "DS")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
.logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }
}
