package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for employee creation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeInput {
    
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    @NotNull(message = "Salary must be provided")
    @Positive(message = "Salary must be positive")
    private Integer salary;
    
    @NotNull(message = "Age must be provided")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must not exceed 100")
    private Integer age;
    
    @NotBlank(message = "Job title cannot be blank")
    @JsonProperty("title")
    private String title;
}
