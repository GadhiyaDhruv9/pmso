package com.pmso.projectManagementSystemOne.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Column;

@Controller
@ResponseBody
@CrossOrigin
public class HelloController {

    @GetMapping("/api/hello")
    public String SayHello() {
        return "Hello, how are you?";
    }
}
