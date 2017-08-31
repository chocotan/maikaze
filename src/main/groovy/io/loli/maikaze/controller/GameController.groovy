package io.loli.maikaze.controller

import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import java.security.Principal;

/**
 * Created by chocotan on 2017/8/31.
 */
//@Controller
public class GameController {

    @RequestMapping(value = "/kcs/game", method = RequestMethod.GET)
    def login(Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            def tuple = dmmAccountService.login(id)
            def flash = tuple._1()
            model.addAttribute "flash", flash
            "account/game"
        } catch (Exception e) {
            logger.error("登录发生错误了, {}", ExceptionUtils.getStackTrace(e))
            redirectAttributes.addFlashAttribute "error", e.message
            "redirect:account/list"
        }
    }
}
