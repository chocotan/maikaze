package io.loli.maikaze.controller

import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.service.DmmAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

import java.security.Principal

/**
 * Created by chocotan on 2017/8/29.
 */
@Controller
@RequestMapping("/account")
@PreAuthorize("hasRole('USER')")
class AccountController {

    @Autowired
    DmmAccountService dmmAccountService;


    @RequestMapping(value = "add", method = RequestMethod.GET)
    def add(@ModelAttribute DmmAccount dmmAccount) {
        "account/add"
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    @ResponseBody
    def login(Long id, Model model) {
        def tuple = dmmAccountService.login(id)
        def flash = tuple._1()
        model.addAttribute "flash", flash
        return flash
    }

    @RequestMapping(value = "edit", method = RequestMethod.GET)
    def edit(Long id, Model model) {
        model.addAttribute "dmmAccount", dmmAccountService.findById(id)
        "account/add"
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    def delete(Long id, Model model) {
        dmmAccountService.deleteById(id)
        "redirect:list"
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    def list(Model model, Principal principal) {
        model.addAttribute("list", dmmAccountService.findByUser(principal.principal.user));
        "account/list";
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    def updateSubmit(@ModelAttribute DmmAccount dmmAccount, Principal principal) {
        dmmAccount.user = principal.principal.user;
        dmmAccountService.save(dmmAccount)
        "redirect:list"
    }
}
