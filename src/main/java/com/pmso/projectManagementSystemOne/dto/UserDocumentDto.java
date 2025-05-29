package com.pmso.projectManagementSystemOne.dto;

import java.util.List;

public class UserDocumentDto {
    private Long docId;
    private String type;
    private List<String> file;

    public UserDocumentDto(Long docId, String type, List<String> file) {
        this.docId = docId;
        this.type = type;
        this.file = file;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFile() {
        return file;
    }

    public void setFile(List<String> file) {
        this.file = file;
    }
}