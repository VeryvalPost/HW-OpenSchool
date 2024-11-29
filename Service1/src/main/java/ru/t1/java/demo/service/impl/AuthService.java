package ru.t1.java.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.User;
import ru.t1.java.demo.util.JwtUtils;

@Service
public class AuthService {
    @Autowired
    private final AuthenticationManager authenticationManager;
    @Autowired
    private final JwtUtils jwtUtils;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils
                      ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        }

    public String authentication (User user){

        String pass = user.getPassword();
        System.out.println(pass);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        return jwt;
    }
}
