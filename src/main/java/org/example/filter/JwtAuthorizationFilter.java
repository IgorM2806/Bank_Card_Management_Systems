package org.example.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!dev")
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static  final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Value("${jwt.secretKey}")
    private String secretKey;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String header = req.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (Object roleObj : claims.get("authorities", List.class)) {
                String roleStr = (String) roleObj;
                try {
                    RoleEnum role = RoleEnum.valueOf(roleStr);
                    authorities.add(new SimpleGrantedAuthority(role.name()));
                }catch (IllegalArgumentException e) {
                    logger.warn("Неправильная роль в токене: {}", roleStr);
                }

            }
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username,
                    null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (MalformedJwtException | ExpiredJwtException ex) {
            logger.warn("Invalid or expired JWT token.", ex);
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }
        chain.doFilter(req, res);
    }
}
