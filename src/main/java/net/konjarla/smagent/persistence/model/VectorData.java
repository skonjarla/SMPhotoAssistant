package net.konjarla.smagent.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "photo_info")
public class VectorData implements Serializable {
    @Serial
    private static final long serialVersionUID = 100001L;
    @Id
    String id;
    String imageKey;
    String albumKey;
    String docid;
}
