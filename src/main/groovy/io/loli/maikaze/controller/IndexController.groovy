package io.loli.maikaze.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")

    @PreAuthorize("hasRole('USER')")
    public String index() {
        return "index";
    }
}