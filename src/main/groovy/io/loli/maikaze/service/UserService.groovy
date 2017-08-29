package io.loli.maikaze.service;

import io.loli.maikaze.domains.User;
import io.loli.maikaze.exception.UserExistsException;
import io.loli.maikaze.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * @author choco
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    @Transactional
    public User registerNewUser(User user) throws UserExistsException {
        if (emailExist(user.getEmail())) {
            throw new UserExistsException("email.exists");
        }

        if (userNameExist(user.getEmail())) {
            throw new UserExistsException("username.exists");
        }
        return userRepository.save(user);
    }

    public User findByEmailOrName(String name) {
        try {
            return userRepository.findByEmailOrUserName(name, name);
        } catch (Exception e) {
            return null;
        }
    }


    private boolean emailExist(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return true;
        }
        return false;
    }

    private boolean userNameExist(String userName) {
        User user = userRepository.findByUserName(userName);
        if (user != null) {
            return true;
        }
        return false;
    }

}