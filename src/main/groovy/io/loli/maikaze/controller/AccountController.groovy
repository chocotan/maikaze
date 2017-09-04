package io.loli.maikaze.controller

import io.loli.maikaze.domains.DmmAccount
import io.loli.maikaze.kancolle.KancolleProperties
import io.loli.maikaze.kancolle.DmmLoginUserHelper
import io.loli.maikaze.service.DmmAccountService
import javaslang.Tuple3
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

/**
 * Created by chocotan on 2017/8/29.
 */
@Controller
@RequestMapping("/account")
@PreAuthorize("hasRole('USER')")
class AccountController {

    static final Logger logger = LoggerFactory.getLogger(AccountController)
    @Autowired
    DmmAccountService dmmAccountService;


    @RequestMapping(value = "add", method = RequestMethod.GET)
    def add(@ModelAttribute DmmAccount dmmAccount) {
        "account/add"
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    def login(Long id,
              @RequestParam(defaultValue = "game") String type, Model model, Principal principal, HttpServletRequest request) {
        try {
            if (!dmmAccountService.check(id, principal.principal.id)) {
                throw new IllegalArgumentException("非法的请求")
            }
            def tuple = dmmLogin(id, request)
            def flash = tuple._1()
            model.addAttribute "flash", flash
            model.addAttribute "type", type
            model.addAttribute "id", id
            "account/${type.toLowerCase()}"
        } catch (Exception e) {
            logger.error("登录发生错误了, {}", ExceptionUtils.getStackTrace(e))
            model.addAttribute "error", e.message
            list(model, principal)
        }
    }


    @RequestMapping(value = "game", method = RequestMethod.GET)
    def game(Long id,
             @RequestParam(defaultValue = "game") String type, Model model, Principal principal, HttpSession session) {
        try {
            if (!dmmAccountService.check(id, principal.principal.id)) {
                throw new IllegalArgumentException("非法的请求")
            }
            model.addAttribute "flash", dmmAccountService.findById(id).lastFlashUrl
            model.addAttribute "type", type
            model.addAttribute "id", id
            "account/${type.toLowerCase()}"
        } catch (Exception e) {
            logger.error("登录发生错误了, {}", ExceptionUtils.getStackTrace(e))
            model.addAttribute "error", e.message
            list(model, principal)
        }
    }

    @Autowired
    KancolleProperties kcp;

    @Autowired
    HttpSession session;

    @Autowired
    DmmLoginUserHelper loginContextCache

    def dmmLogin(Long id, HttpServletRequest request) {
        // 登录指定ip的舰娘账号，并将登录结果放在session中
        def account = dmmAccountService.findById(id)
        def lctx = loginContextCache.get(id, session);
        session.setAttribute("lctx_$id", lctx)
        lctx.user_agent = request.getHeader "User-Agent"
        def pwd = account.password ?: session.getAttribute(account.username);
        if(!pwd){
            throw new IllegalArgumentException("你需要重新登录dmm账号")
        }
        lctx.reset(account.username,pwd)
        def flashUrl = lctx.startLogin();
        account.token = lctx.$6_api_token
        account.startTime = lctx.$6_api_starttime
        account.lastLogin = new Date()
        account.serverIp = lctx.$5_world_ip
        def finalFlashUrl = flashUrl.replace("http://" + lctx.$5_world_ip, "")
        lctx.finalFlashUrl = finalFlashUrl
        account.lastFlashUrl = finalFlashUrl
        dmmAccountService.save(account)
        Tuple3.of finalFlashUrl, lctx.$6_api_token, lctx.$6_api_starttime
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
    def updateSubmit(
            @ModelAttribute DmmAccount dmmAccount, Principal principal) {
        dmmAccount.user = principal.principal.user;
        if (dmmAccount.type) {
            session.setAttribute(dmmAccount.username, dmmAccount.password)
            dmmAccount.password = null
        }

        dmmAccountService.save(dmmAccount)
        if (dmmAccount.type) {
            "redirect:/account/login?id=${dmmAccount.id}&type=${dmmAccount.type}"
        } else {
            "redirect:list"
        }
    }
}
