package com.gate.keeper.service;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CookieService {

    public String getCookieValue(HttpServletRequest req, String cookieName) {
        if (req.getCookies() == null) {
            return null;
        }
        return Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public String createCookie(String name, String token, Integer expireInSeconds, boolean httpOnly) {
        List<String> headerValues = new ArrayList<>();
        headerValues.add(name + "=" + token);
        headerValues.add("SameSite=Strict");
        headerValues.add("Path=/");
        if (httpOnly) {
            headerValues.add("HttpOnly");
        }

        if (expireInSeconds != null) {
            headerValues.add("Max-Age=" + expireInSeconds);
        }

        return String.join("; ", headerValues);
    }

    public String createCookie(String name, String token, boolean httpOnly) {
        return createCookie(name, token, null, httpOnly);
    }

    public void addCookie(String cookie, HttpServletResponse response) {
        response.addHeader("Set-Cookie", cookie);
    }

}