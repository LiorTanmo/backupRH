package com.lior.application.rh_test.services;

import com.lior.application.rh_test.model.User;
import com.lior.application.rh_test.repos.UsersRepository;
import com.lior.application.rh_test.util.NotAuthorizedException;
import com.lior.application.rh_test.util.UserNotFoundException;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

//TODO
@Service
@Transactional
public class UsersService {
    private final UsersRepository usersRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Optional<User> findUserByUsername(String username){
        return usersRepository.findByUsername(username);
    }

    public User findOne(int id) {
        return usersRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void save(User user) {
       usersRepository.save(user);
    }

    @Transactional
    public void update(int id, User updatedUser) throws NotAuthorizedException {
        preventUnAuthorizedAccess(usersRepository.findById(id).orElseThrow(UserNotFoundException::new));
        updatedUser.setId(id);
        usersRepository.save(updatedUser);
    }

    @Transactional
    public void delete(int id) throws NotAuthorizedException {
        preventUnAuthorizedAccess(usersRepository.findById(id).orElseThrow(UserNotFoundException::new));
        usersRepository.deleteById(id);
    }

    //prevents unauthorized modifications by comparing usernames of creator
    // and active session user (or if active user is Admin)
    @PreRemove
    @PreUpdate
    private void preventUnAuthorizedAccess(User target) throws NotAuthorizedException {

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SimpleGrantedAuthority> roles = (List<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        if(roles.stream().noneMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"))
                && !name.equals(target.getUsername())){
            throw new NotAuthorizedException("Access denied");
        }

    }
}
