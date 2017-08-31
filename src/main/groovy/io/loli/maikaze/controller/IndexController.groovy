package io.loli.maikaze.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasRole('USER')")
public class IndexController {

    @RequestMapping("/")

    public String index() {
        return "redirect:/account/list";
    }
}