package com.rusudinu.backend.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rusudinu.backend.request.Request;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Entity
@RequiredArgsConstructor
@Table(name = "app_users")
@ToString(exclude = "requests")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keycloakId;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private ZonedDateTime createdDate;

    private boolean isDeleted = false;

    @JsonIgnoreProperties("user")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Request> requests;
}
