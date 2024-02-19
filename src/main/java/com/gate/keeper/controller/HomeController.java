package com.gate.keeper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.gate.keeper.service.AuthenticationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final AuthenticationService authenticationService;

    @GetMapping({"/home"})
    public String hello(HttpServletResponse response, HttpServletRequest request) {
        authenticationService.createAnonymousSession(request, response);
        return "home-screen";
    }

}
