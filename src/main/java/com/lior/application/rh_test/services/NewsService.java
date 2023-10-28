package com.lior.application.rh_test.services;

import com.lior.application.rh_test.model.Comment;
import com.lior.application.rh_test.model.News;
import com.lior.application.rh_test.repos.CommentsRepository;
import com.lior.application.rh_test.repos.NewsRepository;
import com.lior.application.rh_test.security.UserAccountDetails;
import com.lior.application.rh_test.util.NewsNotFoundException;
import com.lior.application.rh_test.util.NotAuthorizedException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NewsService {
    private final NewsRepository newsRepository;
    private final CommentsRepository commentsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository, CommentsRepository commentsRepository) {
        this.newsRepository = newsRepository;
        this.commentsRepository = commentsRepository;
    }

    public News findOne(int id){
        return newsRepository.findById(id).orElseThrow(NewsNotFoundException::new);
    }


    public Page<News> findAll(int page, int newsPerPage){
        return newsRepository.findAll(PageRequest.of(page, newsPerPage));
    }
    public void save (News news){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccountDetails userAccountDetails = (UserAccountDetails) authentication.getPrincipal();
        news.setInserted_by(userAccountDetails.getUser());
        news.setUpdated_by(userAccountDetails.getUser());
        newsRepository.save(news);
    }

    public void update(int id, News updNews){
        updNews.setId(id);
        updNews.setInserted_by(newsRepository.findById(id)
                .orElseThrow(NewsNotFoundException::new).getInserted_by());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserAccountDetails userAccountDetails = (UserAccountDetails) authentication.getPrincipal();
        updNews.setUpdated_by(userAccountDetails.getUser());
        newsRepository.save(updNews);
    }

    public void delete(int id) throws NotAuthorizedException {
        preventUnauthorizedAccess(newsRepository.findById(id).orElseThrow(NewsNotFoundException::new));
        newsRepository.deleteById(id);
    }

//    public List<Comment> getCommentsByNewsId (int id){
//        Optional<News> news = newsRepository.findById(id);
//        if (news.isPresent()){
//            Hibernate.initialize(news.get().getComments());
//            return news.get().getComments();
//        }else throw new NewsNotFoundException();
//    }

//paginated comments
    public Page<Comment> getCommentsByNewsId (int id, int page, int commsPerPage){
        Optional<News> news = newsRepository.findById(id);
        if (news.isPresent()){
            Hibernate.initialize(news);
            return commentsRepository
                    .findCommentsByCommentednews(news.get(), PageRequest.of(page, commsPerPage));
        }else throw new NewsNotFoundException();
    }

    public List<News> search(String search){
        return newsRepository.findByTextContainingOrTitleContaining(search, search);
    }

    private void preventUnauthorizedAccess(News target) throws NotAuthorizedException {

        //TODO make more scalable solution
        //prevents unauthorized modifications by comparing usernames of creator
        //and active session user (or if active user is Admin)
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<SimpleGrantedAuthority> roles = (List<SimpleGrantedAuthority>) SecurityContextHolder
                .getContext().getAuthentication().getAuthorities();

        if(roles.stream().noneMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"))
                && !name.equals(target.getInserted_by().getUsername())){
            throw new NotAuthorizedException("You can alter and remove only your own news");
        }

    }
}
