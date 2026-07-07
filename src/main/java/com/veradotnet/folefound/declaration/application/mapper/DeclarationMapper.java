package com.veradotnet.folefound.declaration.application.mapper;

import com.veradotnet.folefound.declaration.application.dto.DeclarationRequestDTO;
import com.veradotnet.folefound.declaration.application.dto.DeclarationResponseDTO;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
//import com.veradotnet.folefound.image.domain.model.Image;
import com.veradotnet.folefound.item.domain.Model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

//DTO composite (1 dto pour créer 2 entités)
@Mapper
public interface DeclarationMapper {
    DeclarationMapper INSTANCE = Mappers.getMapper(DeclarationMapper.class);


     // 1. Transforme le DTO du formulaire React en entité physique Item

    @Mapping(target = "id", ignore = true) // Géré par l'auto-increment PostgreSQL
    @Mapping(target = "name", source = "itemName") // Redirection car les noms de champs diffèrent
    @Mapping(target = "category", ignore = true) // Lié manuellement par ID dans le Service
    @Mapping(target = "itemState", ignore = true) // Calculé logiquement (LOST/FOUND) dans le Service
    Item toItemModel(DeclarationRequestDTO dto);

    //2. Transforme le DTO du formulaire React en entité événementielle Declaration

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true) // Lié manuellement par ID dans le Service
    @Mapping(target = "user", ignore = true)     // Lié manuellement par ID dans le Service
    @Mapping(target = "item", ignore = true)     // Lié manuellement après l'instanciation de l'Item
    @Mapping(target = "status", ignore = true)   // Forcé à EN_COURS par le Service
    Declaration toDeclarationModel(DeclarationRequestDTO dto);

   //3. Transforme la Declaration enregistrée (avec son Item interne) en un seul DTO plat pour React

    @Mapping(target = "itemName", source = "item.name")   // Va chercher le nom caché dans l'Item lié
    @Mapping(target = "color", source = "item.color")     // Va chercher la couleur dans l'Item
    @Mapping(target = "brand", source = "item.brand")     // Va chercher la marque dans l'Item
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "campusId", source = "location.campus.id") // Va chercher l'ID du campus via la relation Location
    @Mapping(target = "campusName", source = "location.campus.name")
    DeclarationResponseDTO toResponseDTO(Declaration declaration);

   /* default List<String> mapImagesToUrls(List<Image> images) {
        if (images == null) return null;
        return images.stream().map(Image::getUrlS3).toList();
    }*/
}
