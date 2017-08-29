package io.loli.maikaze.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Created by uzuma on 2017/8/29.
 */
public class DmmLoginAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    @Override
    publ ic boolean supports(Class<?> aClass) {
        return false;
    }
}
