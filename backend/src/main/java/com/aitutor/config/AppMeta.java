package com.aitutor.config;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_metadata")
@Getter @Setter @NoArgsConstructor
public class AppMeta {
    @Id
    @Column(name = "meta_key", length = 80, nullable = false)
    private String key;

    @Column(name = "meta_value", length = 500, nullable = false)
    private String value;

    public AppMeta(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
