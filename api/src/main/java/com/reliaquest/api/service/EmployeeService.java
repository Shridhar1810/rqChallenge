package com.reliaquest.api.service;

import com.reliaquest.api.client.MockEmployeeClient;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for employee operations
 * Uses Java 8 features like lambdas, streams, and method references
 */
@Service
public class EmployeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final MockEmployeeClient mockEmployeeClient;
    
    @Autowired
    public EmployeeService(MockEmployeeClient mockEmployeeClient) {
        this.mockEmployeeClient = mockEmployeeClient;
    }
    
    /**
     * Get all employees
     * 
     * @return list of all employees
     */
    public List<Employee> getAllEmployees() {
        logger.info("Retrieving all employees");
        return mockEmployeeClient.getAllEmployees();
    }
    
    /**
     * Get employees by name search
     * 
     * @param searchString string to search in employee names
     * @return list of employees matching the search criteria
     */
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        logger.info("Searching employees with name containing: {}", searchString);
        if (searchString == null || searchString.trim().isEmpty()) {
            return getAllEmployees();
        }
        
        String searchLowerCase = searchString.toLowerCase();
        return getAllEmployees()
                .stream()
                .filter(employee -> employee.getName() != null &&
                                   employee.getName().toLowerCase().contains(searchLowerCase))
                .collect(Collectors.toList());
    }
    
    /**
     * Get employee by ID
     * 
     * @param id employee ID
     * @return the employee with the given ID
     * @throws EmployeeNotFoundException if employee is not found
     */
    public Employee getEmployeeById(String id) {
        logger.info("Retrieving employee with ID: {}", id);
        try {
            return mockEmployeeClient.getEmployeeById(id);
        } catch (Exception e) {
            logger.error("Employee not found with ID: {}", id);
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        }
    }
    
    /**
     * Get highest salary of all employees
     * 
     * @return the highest salary value
     */
    public Integer getHighestSalaryOfEmployees() {
        logger.info("Retrieving highest salary of employees");
        
        return getAllEmployees().stream()
                .map(Employee::getSalary)
                .filter(salary -> salary != null && salary > 0)
                .max(Integer::compare)
                .orElse(0);
    }
    
    /**
     * Get names of top 10 highest earning employees
     * 
     * @return list of employee names
     */
    public List<String> getTopTenHighestEarningEmployeeNames() {
        logger.info("Retrieving top 10 highest earning employee names");
        
        return getAllEmployees().stream()
                .sorted(Comparator.comparing(Employee::getSalary, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new employee
     * 
     * @param employeeInput employee input data
     * @return the created employee
     */
    public Employee createEmployee(EmployeeInput employeeInput) {
        logger.info("Creating new employee: {}", employeeInput);
        return mockEmployeeClient.createEmployee(employeeInput);
    }
    
    /**
     * Delete employee by ID
     * 
     * @param id employee ID
     * @return deleted employee ID
     * @throws EmployeeNotFoundException if employee is not found
     */
    public String deleteEmployeeById(String id) {
        logger.info("Deleting employee with ID: {}", id);
        
        // First check if employee exists
        getEmployeeById(id); // Will throw EmployeeNotFoundException if not found
        
        boolean deleted = mockEmployeeClient.deleteEmployee(id);
        if (deleted) {
            logger.info("Successfully deleted employee with ID: {}", id);
            return id;
        } else {
            logger.error("Failed to delete employee with ID: {}", id);
            throw new EmployeeNotFoundException("Failed to delete employee with ID: " + id);
        }
    }
}
