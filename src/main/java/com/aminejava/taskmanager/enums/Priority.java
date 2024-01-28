package com.aminejava.taskmanager.enums;

public enum Priority {
    HIGH(0),
    MEDIUM(1),
    LOW(2);

    private int priorityNumber;

     Priority(int priorityNumber){
        this.priorityNumber=priorityNumber;
    }

    public int getPriorityNumber() {
        return priorityNumber;
    }
}
