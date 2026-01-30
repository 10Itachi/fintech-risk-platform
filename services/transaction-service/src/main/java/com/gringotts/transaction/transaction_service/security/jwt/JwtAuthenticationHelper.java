package com.gringotts.transaction.transaction_service.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationHelper {
private final String SECRET_KEY="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
private static final long EXPIRATION_TIME=1000*60*60;

    public String generateToken(CustomerUserDetails userDetails) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetails.getUserId());
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }


    public Claims getClaimsFromTokens(String token){
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token).getBody();
            return  claims;
    }

    public String getUserNameFromToken(String token){
            String name = getClaimsFromTokens(token).getSubject();
            return name;
    }

    public Long getUserIdFromToken(String token){
       return getClaimsFromTokens(token).get("userId",Long.class);
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        return getUserNameFromToken(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public Boolean isTokenExpired(String token){
        Claims claims = getClaimsFromTokens(token);
        Date  expDate = claims.getExpiration();
        return expDate.before(new Date());
    }

    public long getExpirationInSecond() {
        return new Date(System.currentTimeMillis()+EXPIRATION_TIME).getTime() ;
    }
}
