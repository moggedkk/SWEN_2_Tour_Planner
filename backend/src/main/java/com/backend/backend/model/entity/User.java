package com.backend.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    private String username;
    private String email;
    private String hash;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Session> session_id;
}
