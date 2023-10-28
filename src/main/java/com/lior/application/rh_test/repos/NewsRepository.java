package com.lior.application.rh_test.repos;

import com.lior.application.rh_test.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
     List<News> findByTextContainingOrTitleContaining(String text, String title);
}
