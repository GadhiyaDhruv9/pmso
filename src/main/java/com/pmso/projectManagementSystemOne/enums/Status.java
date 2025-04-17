package com.pmso.projectManagementSystemOne.enums;

public enum Status {
    Draft(0),
    Pending(1),
    In_Progress(2),
    Completed(3);

    private final int order;

    Status(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public boolean canTransitionTo(Status newStatus) {
        if(this == newStatus) {
            return true;
        }
        return newStatus.order > this.order;
    }
}
