package com.veradotnet.folefound.item.domain.Model;

import com.veradotnet.folefound.category.domain.model.Category;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
//import com.veradotnet.folefound.image.domain.model.Image;
import com.veradotnet.folefound.item.application.enums.ItemState;
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
import java.util.List;

@Entity
@Table(name = "item")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String color;

    @NotBlank
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemState itemState;

    @CreatedDate
    private LocalDateTime dateCreated;

    @LastModifiedDate
    private LocalDateTime lastModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<Declaration> declarations;

    /*@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "item_id") // Relation 1..2 gérée au niveau applicatif
    private List<Image> images;*/
}
