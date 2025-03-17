package net.konjarla.smagent.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.persistence.model.VectorData;
import net.konjarla.smagent.persistence.repository.VectorDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorDataService {
    private final VectorDataRepository vectorDataRepository;

    public List<VectorData> getAll() {
        return (List<VectorData>) vectorDataRepository.findAll();
    }

    public VectorData getByImageKey(String imageKey) {
        return vectorDataRepository.findByImageKey(imageKey);
    }

    public List<VectorData> getByAlbumKey(String albumKey) {
        return vectorDataRepository.findByAlbumKey(albumKey);
    }

    public boolean isAlbumIndexed(String albumKey) {
        return getByAlbumKey(albumKey) == null;
    }

    public boolean isImageIndexed(String imageKey) {
        return getByImageKey(imageKey) == null;
    }

    public VectorData getByDocid(String docId) {
        return vectorDataRepository.findByDocid(docId);
    }

    public void deleteById(String id) {
        vectorDataRepository.deleteById(id);
    }

    public void save(VectorData vectorData) {
        vectorDataRepository.save(vectorData);
    }
}
