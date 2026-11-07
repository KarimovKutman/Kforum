package kutman.dev.Kforum.security;

import kutman.dev.Kforum.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        RequestMatcher apiMatcher = request -> request.getServletPath().startsWith("/api/");

        // ignoringRequestMatchers("/api/**", "/K-messenger/**") отключает CSRF только для указанных путей (для REST API и WebSockets/K-messenger), оставляя защиту включенной для остальных URL а disable полностью отключает проверку
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/K-messenger/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers("/K-messenger/**").permitAll()
                        // это доп проверка на админа типа? и как именно оно проверяет? через что?
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                        // что за пост и гет и что будет если их не добавить
                        .requestMatchers(HttpMethod.GET, "/topics/new").authenticated()
                        .requestMatchers(HttpMethod.GET, "/", "/categories", "/categories/**", "/topics/**", "/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/topics/**", "/comments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                //что за формы? откуда они? они нужжны типа для дджавы да? я думал фронт сам всё сделает.  на что отвечает каждый из этих параметров
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                // что за лог аут и за что отвечают каждые из этих методов
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // как этот метод работает
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiMatcher
                        )
                        // а это что за метод
                        .defaultAccessDeniedHandlerFor(
                                (request, response, exception) -> response.sendError(HttpStatus.FORBIDDEN.value()),
                                apiMatcher
                        )
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // я конечно понял что оно кодирует пороль но я бы теперь хотел узнать как он работает НА САМОМ ДЕЛЕ тоесть я уже вкурсе что он нужен что бы пороль кодировать но я хочу узнать его работу. и как именно он защищает от взлома?
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
