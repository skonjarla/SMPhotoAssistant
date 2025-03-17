package net.konjarla.smagent.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.persistence.model.PhotoCaptionVectorData;
import net.konjarla.smagent.persistence.repository.PhotoCaptionVectorDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoCaptionVectorDataService {
    private final PhotoCaptionVectorDataRepository photoCaptionVectorDataRepository;

    public PhotoCaptionVectorData getById(String id) {
        Optional<PhotoCaptionVectorData> photoCaptionVectorData = photoCaptionVectorDataRepository.findById(id);
        return photoCaptionVectorData.orElse(null);
    }

    public List<PhotoCaptionVectorData> findByAlbumName(String albumName) {
        return photoCaptionVectorDataRepository.findByContentIgnoreCase(albumName);
    }
}
