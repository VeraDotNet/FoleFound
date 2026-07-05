package com.veradotnet.folefound.matching.domain.model;

import com.veradotnet.folefound.declaration.domain.model.Declaration;
import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "matching")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_declaration_id", nullable = false)
    private Declaration lostDeclaration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_declaration_id", nullable = false)
    private Declaration foundDeclaration;

    @CreatedDate
    private LocalDateTime dateCreated;

    //@LastModifiedDate
    //private LocalDateTime lastModified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status;
}
