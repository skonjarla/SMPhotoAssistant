package net.konjarla.smagent.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.smagent.service.Indexer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
@RequestMapping("/photo")
@Slf4j
public class IndexController {
    Indexer indexer;

    @GetMapping(value = "/index/{albumKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMessage> index(@PathVariable String albumKey) {
        try {
            indexer.indexSmugMugAlbum(albumKey);
            return ResponseEntity.ok().body(new ResponseMessage(HttpStatus.OK.toString(), "success"));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseMessage(HttpStatus.BAD_REQUEST.toString(), e.getMessage()));
        }
    }

    @GetMapping(value = "/indexalbums", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ResponseMessage> indexAlbums() {
        try {
            indexer.loadAlbumDataToVectorStore();
            return ResponseEntity.ok().body(new ResponseMessage(HttpStatus.OK.toString(), "success"));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(new ResponseMessage(HttpStatus.BAD_REQUEST.toString(), e.getMessage()));
        }
    }

    public record ResponseMessage(String status, String message) {
    }
}
