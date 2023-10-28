package com.lior.application.rh_test.controllers;

import com.lior.application.rh_test.dto.UserCRUDDTO;
import com.lior.application.rh_test.dto.UserDTO;
import com.lior.application.rh_test.dto.UserLoginDTO;
import com.lior.application.rh_test.model.User;
import com.lior.application.rh_test.security.JWTUtil;
import com.lior.application.rh_test.security.UserAccountDetails;
import com.lior.application.rh_test.services.UsersService;
import com.lior.application.rh_test.util.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

//TODO
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UsersService usersService;
    private final ModelMapper modelMapper;
    private final UserValidator userValidator;
    private final ErrorPrinter errorPrinter;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * Method for user authentication.
     * @param loginDTO Credentials
     * @param bindingResult Field error holder
     * @return OK status with Authorisation header, containing JWT or Exception Handler response
     */
    @PostMapping("/login")
    public ResponseEntity<HttpStatus> login(@RequestBody @Valid UserLoginDTO loginDTO,
                                            BindingResult bindingResult){
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),
                        loginDTO.getPassword());

        errorPrinter.printFieldErrors(bindingResult);

        authManager.authenticate(authInputToken);
        String jwt = jwtUtil.generateToken(loginDTO.getUsername());
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + jwt);
        return ResponseEntity.ok().headers(headers).body(HttpStatus.OK);
    }

    /**
     * User info by username
     * @param username Username of the User
     * @return UserDTO
     */
    @GetMapping("/{username}")
    public UserDTO userPage (@PathVariable(name = "username") String username){
        User user = usersService.findUserByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        return toDTO(user);
    }

    /**
     * Current user info
     * @return User DTO
     */
    @GetMapping("/userInfo")
    public UserDTO currentUserInfo (){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccountDetails userAccountDetails = (UserAccountDetails) authentication.getPrincipal();
        User currentUser = userAccountDetails.getUser();
        return toDTO(currentUser);
    }

    /**
     * Method for creating new users. Admin Only
     * @param userCRUDDTO User Information
     * @param bindingResult Field Error holder
     * @return HttpStatus created or error response
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/registration")
    public ResponseEntity<HttpStatus> registration(@RequestBody @Valid UserCRUDDTO userCRUDDTO,
                                             BindingResult bindingResult){
        User user = toUser(userCRUDDTO);
        if (user.getRole() == null){user.setRole("ROLE_SUBSCRIBER");}
        userValidator.validate(user, bindingResult);
        errorPrinter.printFieldErrors(bindingResult);

        usersService.save(user);
        String jwt = jwtUtil.generateToken(userCRUDDTO.getUsername());
        HttpHeaders headers = new HttpHeaders();

        headers.add("Authorization", "Bearer " + jwt);
        return ResponseEntity.ok().headers(headers).body(HttpStatus.CREATED);
    }

    //probably ID should be replaced by username
    /**
     * Edit user info. Changing roles is available only for admins.
     * Non-admin user role in input will be replaced by their current role.
     * @param userCRUDDTO New user info
     * @param bindingResult Field error holder
     * @param id ID of user to be changed
     * @return HttpStatus OK or error response
     */
    @PatchMapping("/{id}")
    public ResponseEntity<HttpStatus> edit(@RequestBody @Valid UserCRUDDTO userCRUDDTO,
                                           BindingResult bindingResult,
                                           @PathVariable(name = "id") int id) throws NotAuthorizedException {
        User user = modelMapper.map(userCRUDDTO, User.class);
        if (user.getRole() == null){user.setRole("ROLE_SUBSCRIBER");}

        //Prevents non-admin users from changing roles, probably should be separate DTO
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccountDetails userAccountDetails = (UserAccountDetails) authentication.getPrincipal();
        User currentUser = userAccountDetails.getUser();
        if (!Objects.equals(currentUser.getRole(), "ROLE_ADMIN")){
            user.setRole(usersService.findOne(id).getRole());
        }

        userValidator.validate(user, bindingResult);
        errorPrinter.printFieldErrors(bindingResult);

        usersService.update(id, user);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Deleting user by ID
     * @param id Target User id
     * @return HttpStatus No_Content or Error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser (@PathVariable(name = "id") int id) throws NotAuthorizedException {
        usersService.delete(id);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler (Exception e){
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    private User toUser(UserCRUDDTO userCRUDDTO){
        User user = modelMapper.map(userCRUDDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;
    }

    private UserDTO toDTO (User user){
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return userDTO;
    }
}
