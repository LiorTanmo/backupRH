package com.lior.application.rh_test.controllers;

import com.lior.application.rh_test.dto.CommentDTO;
import com.lior.application.rh_test.dto.NewsDTO;
import com.lior.application.rh_test.dto.UserDTO;
import com.lior.application.rh_test.model.News;
import com.lior.application.rh_test.services.NewsService;
import com.lior.application.rh_test.util.ErrorPrinter;
import com.lior.application.rh_test.util.ErrorResponse;
import com.lior.application.rh_test.util.NewsNotFoundException;
import com.lior.application.rh_test.util.NotAuthorizedException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//TODO
@RestController
@RequestMapping("/news")
@AllArgsConstructor
@Slf4j
public class NewsController {

    private final NewsService newsService;
    private final ModelMapper modelMapper;
    private  final ErrorPrinter errorPrinter;

    /**
     * Method for posting news
     * @param newsDTO News body
     * @param bindingResult Error holder
     * @return Response status Created or Exception
     */
    @PostMapping("/post")
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid NewsDTO newsDTO,
                                             BindingResult bindingResult){
        errorPrinter.printFieldErrors(bindingResult);
        newsService.save(toNews(newsDTO));
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    /**
     * Method for editing news
     * @param newsDTO New news body
     * @param bindingResult Error holder
     * @param id Id of news to be patched
     * @return Status OK or Exception status
     */
    @PatchMapping("/{id}")
    public ResponseEntity<HttpStatus> edit(@RequestBody @Valid NewsDTO newsDTO,
                                           BindingResult bindingResult,
                                           @PathVariable(name = "id") int id){
        errorPrinter.printFieldErrors(bindingResult);
        newsService.update(id, toNews(newsDTO));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Get all news by pages
     * @param page Page number
     * @param news_per_page News per page (required)
     * @return Page of news with null comments (required)
     */
    @GetMapping()
    public Page<NewsDTO> getNews(@RequestParam Integer page,
                                 @RequestParam Integer news_per_page){
        return newsService.findAll(page,news_per_page).map(this::toDTO);
    }

    /**
     * Get single news post with paginated comments
     * @param id News Id
     * @param page Comment page number (reqired)
     * @param comms_per_page How many comments per page (required)
     * @return News with comments
     */
    @GetMapping("/{id}")
    public NewsDTO getNewsById(@PathVariable("id") int id,
                               @RequestParam Integer page,
                               @RequestParam Integer comms_per_page){
        return toDTO(newsService.findOne(id), id, page, comms_per_page);
    }

    /**
     * News search. Looks for searched string in title and text. Case-sensitive.
     * @param query Search string
     * @return List of news corresponding to search request.
     */
    @GetMapping("/search")
    public List<NewsDTO> newsSearch(@RequestParam String query){
        return newsService.search(query).stream().map((this::toDTO)).toList();
    }

    /**
     * Method for deleting news. Corresponding comments are cascade-deleted.
     * @param id News id
     * @return HttpStatus NO_CONTENT
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable(name = "id") int id) throws NotAuthorizedException {
        newsService.delete(id);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
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


    //DTO converters
    private NewsDTO toDTO (News news){
       return modelMapper.map(news, NewsDTO.class);
    }

    private NewsDTO toDTO(News news, int id, int page, int comms_per_page){
        NewsDTO NewsDTO =  modelMapper.map(news, NewsDTO.class);
        NewsDTO.setComments(newsService
                .getCommentsByNewsId(id, page, comms_per_page)
                .map((comm) -> {
                    CommentDTO comDTO = modelMapper.map(comm, CommentDTO.class);
                    comDTO.setComment_author(modelMapper.map(comm.getInserted_by(), UserDTO.class));
                    return comDTO;
                }));
        return NewsDTO;
    }

    private News toNews(NewsDTO newsDTO) {
        return modelMapper.map(newsDTO, News.class);
    }


}
