package com.lior.application.rh_test.repos;

import com.lior.application.rh_test.model.Comment;
import com.lior.application.rh_test.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Integer> {
    Page<Comment> findCommentsByCommentednews(News news, PageRequest page);
}
