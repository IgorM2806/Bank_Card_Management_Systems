package org.example.util;

import org.example.service.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class GenerateJwtTokenTest {

    private final JwtUtils jwtUtils;

    public GenerateJwtTokenTest(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }


    public String generateJwtToken(UserDetailsImpl userDetails) throws Exception {
        UsernamePasswordAuthenticationToken token =
                new  UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        return jwtUtils.generateToken(token);
    }
}
