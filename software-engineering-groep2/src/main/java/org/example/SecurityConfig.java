package org.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Security configuration class that defines HTTP security rules,
 * user authentication details, and password encoding mechanisms.
 * <p>
 * This setup supports in-memory authentication with custom user details and
 * assigns different access levels to "USER" and "ADMIN" roles.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configures the security filter chain, defining access control rules for endpoints,
     * form login behavior, and logout functionality.
     *
     * @param http The {@link HttpSecurity} object provided by Spring Security.
     * @return A configured {@link SecurityFilterChain} bean.
     * @throws Exception If any configuration error occurs.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**").permitAll()
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/request").hasRole("USER")
                        .requestMatchers("/manage", "/stock").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    /**
     * Defines the password encoder used to encrypt user passwords.
     *
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides a {@link UserDetailsService} with two predefined users: "user" and "admin".
     * Each user is represented as a {@link CustomUserDetails} instance containing
     * username, password, roles, full name, and location.
     * <p>
     * @param encoder The password encoder to use for hashing user passwords.
     * @return A {@link UserDetailsService} that returns user details based on the username.
     */
    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        Map<String, CustomUserDetails> userMap = new HashMap<>();
        userMap.put("user", new CustomUserDetails(
                "user", encoder.encode("user123"),
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "Donald Duck", "Amsterdam"
        ));
        userMap.put("admin", new CustomUserDetails(
                "admin", encoder.encode("admin123"),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                "Dagobert Duck", "Utrecht"
        ));

        return username -> {
            CustomUserDetails user = userMap.get(username);
            if (user == null) throw new UsernameNotFoundException("User not found: " + username);
            return user;
        };
    }
}
