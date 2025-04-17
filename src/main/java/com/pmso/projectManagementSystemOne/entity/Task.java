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
@Table(name = "tasks")
public class Task extends CommonUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;
    private String taskName;
    private String taskPriority;
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    private Status taskStatus = Status.Draft;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private UserEntity assignedTo;

}