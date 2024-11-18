package com.website_parser.parser.model;


import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@Builder
@Data
@Entity
@NoArgsConstructor
@Table(name = "type")
public class Type {

    @Id
    @Column(name = "idtype")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name")
    private String typeName;

}
