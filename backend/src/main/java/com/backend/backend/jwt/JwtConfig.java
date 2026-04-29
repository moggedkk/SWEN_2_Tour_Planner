package com.backend.backend.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtConfig {
    private final String secretKey = "7f9a8c2d4e6b1a9f3d2e5c8b0a4f7e1d"; // Should be safely stored, gonna do later
    public String createJWT(String username){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("my-app")
                    .withSubject(username)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error creating JWT", exception);
        }
    }
    public DecodedJWT verifyJWT(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("my-app")
                    .build();

             return verifier.verify(token); // decoded JWT
        } catch (JWTVerificationException exception){
            throw new RuntimeException("Invalid JWT", exception);
        }
    }
}
