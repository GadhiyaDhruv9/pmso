package com.pmso.projectManagementSystemOne.dto;

import java.util.List;

public class UserDocumentDto {
    private Long docId;
    private String documentType;
    private List<String> fileUrls;

    // Full constructor
    public UserDocumentDto(Long docId, String documentType, List<String> fileUrls) {
        this.docId = docId;
        this.documentType = documentType;
        this.fileUrls = fileUrls;
    }

    // Constructor for mapping from Map.Entry
    public UserDocumentDto(String documentType, List<String> fileUrls) {
        this(null, documentType, fileUrls);
    }

    // Default constructor
    public UserDocumentDto() {
    }

    // Getters and setters
    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public List<String> getFileUrls() {
        return fileUrls;
    }

    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }
}