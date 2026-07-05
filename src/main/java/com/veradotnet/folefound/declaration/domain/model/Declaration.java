package com.veradotnet.folefound.declaration.domain.model;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.item.domain.Model.Item;
import com.veradotnet.folefound.location.domain.model.Location;
import com.veradotnet.folefound.users.domain.model.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "declaration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public  class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeclarationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeclarationStatus status;

    @Column(unique = true)
    private String qrCode; // Sera null si type == LOST

    @Column(nullable = false)
    private LocalDateTime dateEvent;

    @CreatedDate
    private LocalDateTime dateCreated;

    @LastModifiedDate
    private LocalDateTime lastModified;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}