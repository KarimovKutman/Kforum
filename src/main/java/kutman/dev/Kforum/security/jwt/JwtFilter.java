package kutman.dev.Kforum.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.repo.UserRepository;
import kutman.dev.Kforum.service.jwt.JwtToolsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtToolsService jwtToolsService;
    private final UserRepository userRepository;

    public JwtFilter(
            JwtToolsService jwtToolsService,
            UserRepository userRepository
    ) {
        this.jwtToolsService = jwtToolsService;
        this.userRepository = userRepository;
    }

    // помню ты мне говорил " мы блокируем юзера и ставим isBlocked = true но это не значит что его жвт тоже заблокан и ты сказал перенаправить меня в жвт фильтер что бы я что то там исправил здесь. мне интересно что ты тут исправил и как работало до и после
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtToolsService.validate(token)) {
                String email = jwtToolsService.extractEmail(token);

                if (email != null) {
                    User user = userRepository.findByEmailIgnoreCase(email)
                            .orElse(null);

                    if (user == null) {
                        SecurityContextHolder.clearContext();
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    if (user.isBlocked()) {
                        SecurityContextHolder.clearContext();
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Account blocked");
                        return;
                    }

                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}