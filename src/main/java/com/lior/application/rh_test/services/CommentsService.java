package com.lior.application.rh_test.services;

import com.lior.application.rh_test.model.Comment;
import com.lior.application.rh_test.model.News;
import com.lior.application.rh_test.repos.CommentsRepository;
import com.lior.application.rh_test.repos.NewsRepository;
import com.lior.application.rh_test.security.UserAccountDetails;
import com.lior.application.rh_test.util.NewsNotFoundException;
import com.lior.application.rh_test.util.NotAuthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@AllArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private  final NewsRepository newsRepository;


    public Page<Comment> findAll(int news_id, int page, int comms_per_page){
        return commentsRepository.findCommentsByCommentednews(
                newsRepository.findById(news_id).orElseThrow(NewsNotFoundException::new),
                PageRequest.of(page,comms_per_page));
    }
    public void removeComment(int comm_id){
        commentsRepository.deleteById(comm_id);
    }

    public void clearCommentsByNewsId(int id){

    }


    public void removeComment(int news_id, int com_num) throws NotAuthorizedException {
        News news = newsRepository.findById(news_id).orElseThrow(NewsNotFoundException::new);
        preventUnauthorizedAccess(news.getComments().get(com_num));
        commentsRepository.delete(news.getComments().remove(com_num));
        newsRepository.save(news);
    }

    public void editComment(Comment updCom, int news_id, int com_num) throws NotAuthorizedException {
        News news = newsRepository.findById(news_id).orElseThrow(NewsNotFoundException::new);
        preventUnauthorizedAccess(news.getComments().get(com_num));
        updCom.setId(news.getComments().get(com_num).getId());
        updCom.setCommentednews(news);
        commentsRepository.save(updCom);
    }

    public void addComment (Comment comment, int id){
        News commentedNews = newsRepository.findById(id).orElseThrow(NewsNotFoundException::new);
        comment.setCommentednews(commentedNews);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccountDetails userAccountDetails = (UserAccountDetails) authentication.getPrincipal();
        comment.setInserted_by(userAccountDetails.getUser());

        commentsRepository.save(comment);
    }


    //prevents unauthorized modifications by comparing usernames of creator
    //and active session user (or if active user is Admin)
    private void preventUnauthorizedAccess(Comment target) throws NotAuthorizedException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SimpleGrantedAuthority> roles = (List<SimpleGrantedAuthority>) SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities();
        //checks if active user is Admin, comment author, or commented News Author
        if (roles.stream().noneMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"))
                && !(name.equals(target.getInserted_by().getUsername())
                || name.equals(target.getCommentednews().getInserted_by().getUsername()))) {
            throw new NotAuthorizedException("You can alter and remove only your own comments or comments on your news");
        }
    }
}
