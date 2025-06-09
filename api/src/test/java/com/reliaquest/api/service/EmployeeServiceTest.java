package com.reliaquest.api.service;

import com.reliaquest.api.client.MockEmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
public class EmployeeServiceTest {

    @Mock
    private MockEmployeeClient mockEmployeeClient;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private List<Employee> testEmployees;
    private String employeeId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID().toString();
        testEmployee1 = Employee.builder()
                .id(employeeId)
                .name("Test Employee 1")
                .salary(75000)
                .age(30)
                .title("Software Developer")
                .build();

        testEmployee2 = Employee.builder()
                .id(UUID.randomUUID().toString())
                .name("Test Employee 2")
                .salary(85000)
                .age(35)
                .title("QA Engineer")
                .build();

        testEmployees = Arrays.asList(testEmployee1, testEmployee2);
    }

    @Test
    @DisplayName("Get All Employees - Should Return All Employees")
    void getAllEmployees_ShouldReturnAllEmployees() {
        when(mockEmployeeClient.getAllEmployees()).thenReturn(testEmployees);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("Test Employee 1", result.get(0).getName());
        assertEquals("Test Employee 2", result.get(1).getName());
        verify(mockEmployeeClient, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Get Employee By ID - When Employee Exists - Should Return Employee")
    void getEmployeeById_WhenEmployeeExists_ShouldReturnEmployee() {
        when(mockEmployeeClient.getEmployeeById(employeeId)).thenReturn(testEmployee1);

        Employee result = employeeService.getEmployeeById(employeeId);

        assertNotNull(result);
        assertEquals(employeeId, result.getId());
        assertEquals("Test Employee 1", result.getName());
        verify(mockEmployeeClient, times(1)).getEmployeeById(employeeId);
    }

    @Test
    @DisplayName("Get Employee By ID - When Employee Does Not Exist - Should Throw Exception")
    void getEmployeeById_WhenEmployeeDoesNotExist_ShouldThrowException() {
        String nonExistentId = UUID.randomUUID().toString();
        when(mockEmployeeClient.getEmployeeById(nonExistentId)).thenThrow(new EmployeeNotFoundException("Employee not found with ID: " + nonExistentId));

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.getEmployeeById(nonExistentId));
        assertEquals("Employee not found with ID: " + nonExistentId, exception.getMessage());
        verify(mockEmployeeClient, times(1)).getEmployeeById(nonExistentId);
    }

    @Test
    @DisplayName("Get Employees By Name Search - Should Filter By Name")
    void getEmployeesByNameSearch_ShouldFilterByName() {
        when(mockEmployeeClient.getAllEmployees()).thenReturn(testEmployees);
        String searchString = "1";

        List<Employee> result = employeeService.getEmployeesByNameSearch(searchString);

        assertEquals(1, result.size());
        assertEquals("Test Employee 1", result.get(0).getName());
        verify(mockEmployeeClient, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Get Highest Salary of Employees - Should Return Highest Salary")
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() {
        when(mockEmployeeClient.getAllEmployees()).thenReturn(testEmployees);

        Integer result = employeeService.getHighestSalaryOfEmployees();

        assertEquals(85000, result);
        verify(mockEmployeeClient, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Get Top Ten Highest Earning Employee Names - Should Return Sorted Names")
    void getTopTenHighestEarningEmployeeNames_ShouldReturnSortedNames() {
        when(mockEmployeeClient.getAllEmployees()).thenReturn(testEmployees);

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(2, result.size());
        assertEquals("Test Employee 2", result.get(0)); // Higher salary
        assertEquals("Test Employee 1", result.get(1)); // Lower salary
        verify(mockEmployeeClient, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Create Employee - Should Create and Return New Employee")
    void createEmployee_ShouldCreateAndReturnNewEmployee() {
        EmployeeInput input = EmployeeInput.builder()
                .name("New Employee")
                .salary(80000)
                .age(28)
                .title("Frontend Developer")
                .build();
        
        Employee newEmployee = Employee.builder()
                .id(UUID.randomUUID().toString())
                .name(input.getName())
                .salary(input.getSalary())
                .age(input.getAge())
                .title(input.getTitle())
                .build();
        
        when(mockEmployeeClient.createEmployee(input)).thenReturn(newEmployee);

        Employee result = employeeService.createEmployee(input);

        assertNotNull(result);
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getSalary(), result.getSalary());
        assertEquals(input.getAge(), result.getAge());
        assertEquals(input.getTitle(), result.getTitle());
        verify(mockEmployeeClient, times(1)).createEmployee(input);
    }

    @Test
    @DisplayName("Delete Employee By ID - When Successful - Should Return Deleted ID")
    void deleteEmployeeById_WhenSuccessful_ShouldReturnDeletedId() {
        when(mockEmployeeClient.getEmployeeById(employeeId)).thenReturn(testEmployee1);
        when(mockEmployeeClient.deleteEmployee(employeeId)).thenReturn(true);

        String result = employeeService.deleteEmployeeById(employeeId);

        assertEquals(employeeId, result);
        verify(mockEmployeeClient, times(1)).getEmployeeById(employeeId);
        verify(mockEmployeeClient, times(1)).deleteEmployee(employeeId);
    }

    @Test
    @DisplayName("Delete Employee By ID - When Employee Does Not Exist - Should Throw Exception")
    void deleteEmployeeById_WhenEmployeeDoesNotExist_ShouldThrowException() {
        String nonExistentId = UUID.randomUUID().toString();
        when(mockEmployeeClient.getEmployeeById(nonExistentId)).thenThrow(new EmployeeNotFoundException("Employee not found with ID: " + nonExistentId));

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class,
                () -> employeeService.deleteEmployeeById(nonExistentId));
        assertEquals("Employee not found with ID: " + nonExistentId, exception.getMessage());
        verify(mockEmployeeClient, times(1)).getEmployeeById(nonExistentId);
        verify(mockEmployeeClient, never()).deleteEmployee(anyString());
    }
}
