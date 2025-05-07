package com.pmso.projectManagementSystemOne.dto;

public class UserDocumentDto {
    private Long docId;
    private String type;
    private String file;

    public UserDocumentDto() {}

    public UserDocumentDto(Long docId, String type, String file) {
        this.docId = docId;
        this.type = type;
        this.file = file;
    }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }
}