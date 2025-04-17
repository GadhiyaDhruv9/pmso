package com.pmso.projectManagementSystemOne.entity;

import com.pmso.projectManagementSystemOne.utils.CommonUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project_details")
public class ProjectDetails extends CommonUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_details_id")
    private Long projectDetailsId;

    @Column(name = "detail_description")
    private String detailDescription;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}