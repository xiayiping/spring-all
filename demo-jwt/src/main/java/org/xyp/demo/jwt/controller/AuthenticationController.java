package org.xyp.demo.jwt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyp.demo.jwt.pojo.AuthenticationRequest;
import org.xyp.demo.jwt.pojo.AuthenticationResponse;
import org.xyp.demo.jwt.pojo.RegisterRequest;
import org.xyp.demo.jwt.security.AuthenticationService;

@RestController
@RequestMapping("/api/vi/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public AuthenticationResponse register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }


    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }
}
