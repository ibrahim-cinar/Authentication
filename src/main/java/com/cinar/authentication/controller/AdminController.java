package com.cinar.authentication.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/admin")
public class AdminController
{
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }
}
