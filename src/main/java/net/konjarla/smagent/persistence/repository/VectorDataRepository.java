package net.konjarla.smagent.persistence.repository;

import net.konjarla.smagent.persistence.model.VectorData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VectorDataRepository extends CrudRepository<VectorData, String> {
    VectorData findByImageKey(String imageKey);

    List<VectorData> findByAlbumKey(String albumKey);

    VectorData findByDocid(String docid);
}
