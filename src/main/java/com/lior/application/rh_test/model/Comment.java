package com.lior.application.rh_test.model;


import com.lior.application.rh_test.util.NotAuthorizedException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Entity
@Table(name = "comments")
@Data
public class Comment {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    @Size(max = 300,message = "Up to 300 characters")
    private String text;

//    @Column
//    @Temporal(TemporalType.DATE)
//    private Date creation_date;
//
//    @Column
//    @Temporal(TemporalType.DATE)
//    private Date last_edit_date;

    @ManyToOne
    @JoinColumn(name = "inserted_by_id", referencedColumnName = "id")
    private User inserted_by;

    @ManyToOne
    @JoinColumn(name = "id_news", referencedColumnName = "id")
    private News commentednews;

}
