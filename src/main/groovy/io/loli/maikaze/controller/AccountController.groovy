package io.loli.maikaze.controller

import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.repository.DmmAccountRepository
import io.loli.maikaze.service.DmmAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import java.security.Principal

/**
 * Created by chocotan on 2017/8/29.
 */
@Controller
@RequestMapping("/account")
class AccountController {

    @Autowired
    DmmAccountService dmmAccountService;

    @RequestMapping(value = "add", method = RequestMethod.GET)
    def add() {
        "account/add"
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    def list(Model model, Principal principal) {
        model.addAttribute ("list", dmmAccountService.findByUser(principal.principal.user));
        "account/list";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    def addSubmit(String username, String password, Principal principal) {
        dmmAccountService.save(new DmmAccount(username: username, password: password, user: principal.principal.user))
        "redirect:list"
    }
}
