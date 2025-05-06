package com.pmso.projectManagementSystemOne.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "document_master")
@Getter
@Setter
public class DocumentMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_name", nullable = false, unique = true)
    private String documentName;

    @Column(name = "document_code", nullable = false, unique = true)
    private String documentCode;

    @Column(name = "is_mandatory")
    private boolean isMandatory = false;

    @Column(name = "is_allowed_multiple")
    private boolean isAllowedMultiple = false;

    @Column(name = "max_size")
    private Double maxSize;

    @Column(name = "allowed_extensions")
    private String allowedExtensions = "jpg,jpeg,png,pdf";
}