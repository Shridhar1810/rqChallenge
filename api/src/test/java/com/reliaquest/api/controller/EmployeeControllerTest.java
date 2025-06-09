package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Employee Controller Tests")
@ExtendWith(SpringExtension.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;
    private List<Employee> testEmployees;
    private String employeeId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID().toString();
        testEmployee = Employee.builder()
                .id(employeeId)
                .name("Test Employee")
                .salary(75000)
                .age(30)
                .title("Software Developer")
                .build();

        testEmployees = Arrays.asList(
                testEmployee,
                Employee.builder()
                        .id(UUID.randomUUID().toString())
                        .name("Another Employee")
                        .salary(85000)
                        .age(35)
                        .title("Senior Developer")
                        .build()
        );
    }

    @Test
    @WithMockUser
    @DisplayName("Get All Employees - Should Return All Employees")
    void getAllEmployees_ShouldReturnAllEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(testEmployees);

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Employee")));
    }

    @Test
    @WithMockUser
    @DisplayName("Get Employee By ID - Should Return Employee")
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById(employeeId)).thenReturn(testEmployee);

        mockMvc.perform(get("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employeeId)))
                .andExpect(jsonPath("$.name", is("Test Employee")))
                .andExpect(jsonPath("$.salary", is(75000)))
                .andExpect(jsonPath("$.title", is("Software Developer")));
    }

    @Test
    @WithMockUser
    @DisplayName("Get Employees By Name Search - Should Return Matching Employees")
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() throws Exception {
        when(employeeService.getEmployeesByNameSearch("Test")).thenReturn(Arrays.asList(testEmployee));

        mockMvc.perform(get("/api/v1/employees/search/{searchString}", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Employee")));
    }

    @Test
    @WithMockUser
    @DisplayName("Get Highest Salary of Employees - Should Return Highest Salary")
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() throws Exception {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(85000);

        mockMvc.perform(get("/api/v1/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("85000"));
    }

    @Test
    @WithMockUser
    @DisplayName("Get Top Ten Highest Earning Employee Names - Should Return Employee Names")
    void getTopTenHighestEarningEmployeeNames_ShouldReturnEmployeeNames() throws Exception {
        List<String> topEmployeeNames = Arrays.asList("Another Employee", "Test Employee");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEmployeeNames);

        mockMvc.perform(get("/api/v1/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("Another Employee")))
                .andExpect(jsonPath("$[1]", is("Test Employee")));
    }

    @Test
    @WithMockUser
    @DisplayName("Create Employee - Should Return Created Employee")
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        EmployeeInput input = EmployeeInput.builder()
                .name("New Employee")
                .salary(80000)
                .age(25)
                .title("QA Engineer")
                .build();

        Employee createdEmployee = Employee.builder()
                .id(UUID.randomUUID().toString())
                .name(input.getName())
                .salary(input.getSalary())
                .age(input.getAge())
                .title(input.getTitle())
                .build();

        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(createdEmployee);

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Employee")))
                .andExpect(jsonPath("$.salary", is(80000)))
                .andExpect(jsonPath("$.age", is(25)))
                .andExpect(jsonPath("$.title", is("QA Engineer")));
    }

    @Test
    @WithMockUser
    @DisplayName("Delete Employee By ID - Should Return Deleted ID")
    void deleteEmployeeById_ShouldReturnDeletedId() throws Exception {
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(employeeId);

        mockMvc.perform(delete("/api/v1/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().string(employeeId));
    }
}
