package com.gringotts.transaction.transaction_service.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtAuthenticationHelper jwtAuthenticationHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestHeader = request.getHeader("Authorization");

        //Nearer xxx.yyy.zzz
        String userName = null;
        String token = null;
        if(requestHeader!=null && requestHeader.startsWith("Bearer ")){
            token = requestHeader.substring(7);
            userName = jwtAuthenticationHelper.getUserNameFromToken(token);

            if(userName!=null && SecurityContextHolder.getContext().getAuthentication()==null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                if(jwtAuthenticationHelper.isValidToken(token,userDetails)){
                    UsernamePasswordAuthenticationToken auth=
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

                    auth.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            }
        }
        filterChain.doFilter(request,response);
    }
}
