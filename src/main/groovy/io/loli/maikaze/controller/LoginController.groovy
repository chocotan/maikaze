package io.loli.maikaze.controller

import io.loli.maikaze.domains.Role
import io.loli.maikaze.domains.User
import io.loli.maikaze.dto.RegisterDto
import io.loli.maikaze.exception.UserExistsException
import io.loli.maikaze.service.UserService
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.support.RedirectAttributes

import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author choco
 */
@Controller
public class LoginController {
    @Autowired
    private UserService userService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/signin")
    public String signin(Model model) {
        return "signin";
    }


    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String signup(RegisterDto registerDto) {
        return "signup";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String signupSubmit(@Valid RegisterDto registerDto, BindingResult bindingResult,
                               RedirectAttributes redirectAttrs, Model model) {
        if (bindingResult.hasErrors()) {
            return signup(registerDto);
        }
        User registered = new User();
        registered.setRole(Role.ROLE_USER);
        registered.setEmail(registerDto.getEmail());
        registered.setUserName(registerDto.getUserName());
        registered.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        try {
            userService.registerNewUser(registered);
        } catch (UserExistsException e) {
            bindingResult.rejectValue("email", e.getMessage());
            return signup(registerDto);
        }
        redirectAttrs.addAttribute("messages", "signup.success");
        return "redirect:signin";
    }
}
