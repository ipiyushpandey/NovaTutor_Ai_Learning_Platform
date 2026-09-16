package com.aitutor.security;

import com.aitutor.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates the existing NovaTutor JWT once per request and establishes the
 * authenticated user for Spring Security. The filter also prevents a client
 * from swapping another user's numeric id into the user-owned URL patterns.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Pattern USER_PATH = Pattern.compile(
            "^/api/(?:users|chat/conversations|progress|analytics|intelligence|adaptive|mentor/state|learning-recommendations)/(?<id>\\d+)(?:/|$)"
    );

    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isBlank()) {
                try {
                    String email = jwt.email(token);
                    var user = users.findByEmail(email).orElse(null);
                    if (user == null) {
                        unauthorized(response, "Account not found");
                        return;
                    }
                    if (user.isBlocked() || !user.isActive()) {
                        unauthorized(response, "Account is inactive or blocked");
                        return;
                    }
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" +
                            String.valueOf(user.getRole() == null ? "STUDENT" : user.getRole()).toUpperCase()));
                    var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authentication.setDetails(user.getId());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    Matcher matcher = USER_PATH.matcher(request.getRequestURI());
                    if (matcher.find()) {
                        Long requestedId = Long.valueOf(matcher.group("id"));
                        boolean admin = authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_OWNER".equals(a.getAuthority()));
                        if (!admin && !requestedId.equals(user.getId())) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You can only access your own account data");
                            return;
                        }
                    }
                } catch (RuntimeException ex) {
                    unauthorized(response, "Invalid or expired session");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
