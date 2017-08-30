package io.loli.maikaze.config;

import io.loli.maikaze.domains.User;
import io.loli.maikaze.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class RepositoryUserDetailsService implements UserDetailsService {

    private UserRepository repository;

    @Autowired
    public RepositoryUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email);
        if (user == null) {
            user = repository.findByUserName(email);
        }

        if (user == null) {
            throw new UsernameNotFoundException("No user found with username: " + email);
        }

        MaikazeUserDetails.Builder builder = MaikazeUserDetails.getBuilder()
                .id(user.getId())
                .password(user.getPassword())
                .role(user.getRole());


        MaikazeUserDetails principal = builder
                .username(user.getUserName())
                .email(user.getEmail())
                .user(user)
                .build();

        return principal;
    }
}