package com.lior.application.rh_test.services;

import com.lior.application.rh_test.model.User;
import com.lior.application.rh_test.repos.UsersRepository;
import com.lior.application.rh_test.security.UserAccountDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserInfoService implements UserDetailsService {

    private final UsersRepository usersRepository;
    public UserInfoService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = usersRepository.findByUsername(username);
        if (user.isEmpty())
            throw new UsernameNotFoundException("User " + username +" not found");
        return new UserAccountDetails(user.get());
    }
}
