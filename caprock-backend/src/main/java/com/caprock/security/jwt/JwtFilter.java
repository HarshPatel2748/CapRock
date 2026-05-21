package com.caprock.security.jwt;

import com.caprock.security.services.UserDetailsImpl;
import com.caprock.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //Step 1 -> extract token from the Authorization header
        String token = parseToken(request);

        //Step 2 -> if token exists and is valid, authenticate the request
        if(token != null && jwtUtil.validateToken(token)){
            String email = jwtUtil.getEmailFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            UserDetails userDetails;

            // Admin has no DB record — build UserDetails directly from token
            if ("ADMIN".equals(role)) {
                userDetails = new UserDetailsImpl(0L, email, "", "ROLE_ADMIN");
            } else {
                userDetails = userDetailsService.loadUserByUsername(email);
            }

            //Step 3 -> create an auth object and set it in Spring's security context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        //Step 4 -> always continue the filter chain
        filterChain.doFilter(request, response);
    }

    //Pulls the token out of "Authorization: Bearer <token>"
    private String parseToken(HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");
        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }
}
