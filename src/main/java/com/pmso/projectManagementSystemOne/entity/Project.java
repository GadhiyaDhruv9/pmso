package com.pmso.projectManagementSystemOne.entity;

import com.pmso.projectManagementSystemOne.enums.Status;
import com.pmso.projectManagementSystemOne.utils.CommonUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "projects")
public class Project extends CommonUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_type")
    private String projectType;

    @Column(name = "project_description")
    private String projectDescription;

    @Transient
    private String createdByName;

    @Enumerated(EnumType.STRING)
    private Status projectStatus = Status.Draft;

    // RELATIONS
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectAssignment> assignments = new ArrayList<>();

    // Add direct relationship with ProjectDetails if not managed via ProjectAssignment
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectDetails> projectDetails = new ArrayList<>();

    @PostLoad
    private void loadCreatedByName() {
        this.createdByName = getCreatedBy() != null ? getCreatedBy().getUsername() : null;
    }
}