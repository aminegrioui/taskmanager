package com.aminejava.taskmanager;


import java.util.HashSet;
import java.util.Objects;

class TaskManagerApplicationTests {


    public static void main(String[] args) {
//        Employee employee = new Employee();
//        employee.id = 1;
//        employee.setName("name1");
//
//        Employee employee2 = new Employee();
//        employee2.id = 2;
//        employee2.setName("name2");
//
//        Employee employee3 = new Employee();
//        employee3.id = 3;
//        employee3.setName("name3");
//
//
//        HashSet<Employee> employees = new HashSet<>();
//        employees.add(employee);
//        employees.add(employee2);
//        employees.add(employee3);
//        employees.add(employee);
//        System.out.println(employees.size());
//        employees.forEach(employee1 -> employee1.getName());

        String val = "sds";
        String val2 = "sds6";

        if(val!=null && val2.equals(val)){
            System.out.println("sdgs");
        }
        else{
            System.out.println("test");
        }

    }
}

class Employee {

    public int id;
    private String name;
    public String department;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        Employee employee = (Employee) o;

        return (Objects.equals(name, employee.name));
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 32;
        result = PRIME * getName().length() * result;
        return result;
    }
}
