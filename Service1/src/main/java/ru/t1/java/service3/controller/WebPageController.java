package ru.t1.java.service3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class WebPageController {
    @GetMapping("/css/styles.css")
    public String styles() {
        return "redirect:/static/css/styles.css";
    }
    @GetMapping("/")
    public String home() {
        return "index";
    }
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; //
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; //
    }

    @GetMapping("/mainpage")
    public String showMainPage() {
        return "mainpage";
    }
}
