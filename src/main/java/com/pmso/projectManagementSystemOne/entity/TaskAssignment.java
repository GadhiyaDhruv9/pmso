package com.pmso.projectManagementSystemOne.entity;

import com.pmso.projectManagementSystemOne.utils.CommonUtil;
import jakarta.persistence.*;

@Entity
@Table(name = "task_assignments")
public class TaskAssignment extends CommonUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public TaskAssignment() {
    }

    public TaskAssignment(Task task, UserEntity user) {
        this.task = task;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}