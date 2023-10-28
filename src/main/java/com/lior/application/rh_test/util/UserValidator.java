package com.lior.application.rh_test.util;

import com.lior.application.rh_test.model.User;
import com.lior.application.rh_test.services.UsersService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private final UsersService usersService;

    public UserValidator(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        if (usersService.findUserByUsername(user.getUsername()).isPresent()){
            errors.rejectValue("username", "400", "Username already taken");
        }
//проверка на валидность роли на основе ENUM
        boolean validRole = false;
        for(Roles role : Roles.values()){
            if (role.name().equals(user.getRole())){
                validRole = true;
                break;
            }
        }
        if (!validRole){
            errors.rejectValue("role", "400",
                    "Use a valid role (ROLE_ADMIN, ROLE_JOURNALIST, ROLE_SUBSCRIBER)");
        }

    }
}
