package com.cinar.authentication.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/test")
public class TestController {

    @PreAuthorize("hasRole('USER')")
    @RequestMapping("/test")
    public String test(){
        return "test";
    }
}
