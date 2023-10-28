package com.lior.application.rh_test.controllers;

//TODO Testing and access

import com.lior.application.rh_test.dto.CommentDTO;
import com.lior.application.rh_test.dto.UserDTO;
import com.lior.application.rh_test.model.Comment;
import com.lior.application.rh_test.services.CommentsService;
import com.lior.application.rh_test.util.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("comments/{news_id}")
@AllArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;
    private final ErrorPrinter errorPrinter;
    private final ModelMapper modelMapper;


    /**
     * Method for commenting news
     * @param commentDTO Comment information
     * @param bindingResult Error holder
     * @param id  Id of the news that are being commented
     * @return HttpStatusResponse
     */
    @PostMapping
    public ResponseEntity<HttpStatus> addComment(@RequestBody @Valid CommentDTO commentDTO,
                                                 BindingResult bindingResult,
                                                 @PathVariable(name = "news_id") int id){
        if (bindingResult.hasErrors()) errorPrinter.printFieldErrors(bindingResult);
        commentsService.addComment(toComment(commentDTO), id);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    /**
     * Gets all comments attached to News with given id
     * @param news_id News id
     * @param page Page
     * @param comments_per_page Comments per page
     * @return Page of comments
     */
    @GetMapping
    public Page<CommentDTO> getComments(@PathVariable(name = "news_id") int news_id,
                                        @RequestParam Integer page,
                                        @RequestParam Integer comments_per_page) {
        return commentsService.findAll(page, comments_per_page, news_id).map(this::toDTO);
    }

    /**
     * Comment altering method
     * @param commentDTO Comment body
     * @param bindingResult Field Error holder
     * @param news_id News id
     * @param com_num Comment number in list of comments
     * @return OK or Exception response
     */
    @PatchMapping("/{com_num}")
    public ResponseEntity<HttpStatus> editComment(@RequestBody @Valid CommentDTO commentDTO,
                                                 BindingResult bindingResult,
                                                  @PathVariable(name = "news_id") int news_id,
                                                  @PathVariable int com_num) throws NotAuthorizedException {
        if (bindingResult.hasErrors()) errorPrinter.printFieldErrors(bindingResult);
        commentsService.editComment(toComment(commentDTO), news_id, com_num);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Deleting comment on news by comment number
     * @param news_id News id
     * @param com_num Comment number
     * @return No Content or Exception response
     */
    @DeleteMapping("/{com_num}")
    public ResponseEntity<HttpStatus> removeComment(@PathVariable(name = "news_id") int news_id,
                                                    @PathVariable(name = "com_num")int com_num) throws NotAuthorizedException {
        commentsService.removeComment(news_id, com_num);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    /**
     * Deleting comment on news by comment. Only accessible by admins.
     * @param news_id News id
     * @return No Content or Exception response
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping
    public ResponseEntity<HttpStatus> clearComments(@PathVariable(name = "news_id") int news_id){
        commentsService.clearCommentsByNewsId(news_id);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    //Exception Handlers

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler (ValidationFailureException e){
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler (NewsNotFoundException e){
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler
    private ResponseEntity<ErrorResponse> exceptionHandler (NotAuthorizedException e){
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    //DTO Converters
    private CommentDTO toDTO(Comment comment){
        CommentDTO commentDTO = modelMapper.map(comment, CommentDTO.class);
        commentDTO.setComment_author(modelMapper.map(comment.getInserted_by(), UserDTO.class));
        return commentDTO;
    }

    private Comment toComment(CommentDTO commentDTO){
        return  modelMapper.map(commentDTO, Comment.class);
    }
}
