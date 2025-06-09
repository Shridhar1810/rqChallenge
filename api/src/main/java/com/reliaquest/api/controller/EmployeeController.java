package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller implementing the IEmployeeController interface
 * for employee operations
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Get all employees
     * 
     * @return ResponseEntity containing list of all employees
     */
    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        logger.info("Request received to get all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employees by name search
     * 
     * @param searchString string to search in employee names
     * @return ResponseEntity containing list of matching employees
     */
    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        logger.info("Request received to search employees by name: {}", searchString);
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID
     * 
     * @param id employee ID
     * @return ResponseEntity containing the employee
     */
    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        logger.info("Request received to get employee by ID: {}", id);
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Get highest salary of all employees
     * 
     * @return ResponseEntity containing the highest salary value
     */
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        logger.info("Request received to get highest salary");
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
        return ResponseEntity.ok(highestSalary);
    }

    /**
     * Get names of top 10 highest earning employees
     * 
     * @return ResponseEntity containing list of employee names
     */
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        logger.info("Request received to get top 10 highest earning employee names");
        List<String> topEmployeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
        return ResponseEntity.ok(topEmployeeNames);
    }

    /**
     * Create a new employee
     * 
     * @param employeeInput employee input data
     * @return ResponseEntity containing the created employee
     */
    @Override
    public ResponseEntity<Employee> createEmployee(@Valid EmployeeInput employeeInput) {
        logger.info("Request received to create employee: {}", employeeInput);
        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    /**
     * Delete employee by ID
     * 
     * @param id employee ID
     * @return ResponseEntity containing the deleted employee ID
     */
    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        logger.info("Request received to delete employee with ID: {}", id);
        String deletedId = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(deletedId);
    }
}
