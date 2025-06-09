package com.reliaquest.api.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Employee model representing employee data from external API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    private String id;
    private String name;
    private Integer salary;
    private Integer age;
    
    @JsonProperty("title")
    private String title;
    
    /**
     * Create an Employee from EmployeeInput
     * 
     * @param input the input data
     * @return new Employee object
     */
    public static Employee fromInput(EmployeeInput input) {
        return Employee.builder()
                .name(input.getName())
                .salary(input.getSalary())
                .age(input.getAge())
                .title(input.getTitle())
                .build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
