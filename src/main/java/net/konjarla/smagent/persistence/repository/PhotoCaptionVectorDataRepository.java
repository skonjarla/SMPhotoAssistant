package net.konjarla.smagent.persistence.repository;

import net.konjarla.smagent.persistence.model.PhotoCaptionVectorData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PhotoCaptionVectorDataRepository extends CrudRepository<PhotoCaptionVectorData, String> {
    List<PhotoCaptionVectorData> findByContentIgnoreCase(String albumName);
}
