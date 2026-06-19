package com.veradotnet.folefound.location.domain.model;

import com.veradotnet.folefound.campus.domain.model.Campus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Boolean isActive = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    private LocalDateTime lastModified;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    private Campus campus;
}
