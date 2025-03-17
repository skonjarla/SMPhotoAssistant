package net.konjarla.smagent.persistence.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "photo_caption_vector_store")
public class PhotoCaptionVectorData implements Serializable {
    @Serial
    private static final long serialVersionUID = 100002L;
    @Id
    public String id;
    @Column(columnDefinition = "text")
    public String content;
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode metadata;
}
