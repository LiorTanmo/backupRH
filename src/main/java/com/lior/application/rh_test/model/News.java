package com.lior.application.rh_test.model;

import com.lior.application.rh_test.util.NotAuthorizedException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Entity
@Table
@Data
public class    News {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    @Size(max = 150, message = "Up to 150 characters")
    private String title;

    @Column
    @Size(max = 2000,message = "Up to 2000 characters")
    private String text;

    @OneToMany(mappedBy = "commentednews")
    private List<Comment> comments;

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
    @JoinColumn(name = "updated_by_id", referencedColumnName = "id")
    private User updated_by;
}
