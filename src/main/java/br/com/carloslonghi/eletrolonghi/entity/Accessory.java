package br.com.carloslonghi.eletrolonghi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "accessories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Accessory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String name;

}
