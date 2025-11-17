package com.entreprise.kpi_analysis_Backend.controller;

import com.entreprise.kpi_analysis_Backend.dto.EmployeeDTO;
import com.entreprise.kpi_analysis_Backend.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Employee operations
 */
@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        logger.debug("Fetching all employees");
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        logger.info("Retrieved {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id) {
        logger.debug("Fetching employee with ID: {}", id);
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        logger.info("Retrieved employee: {}", id);
        return ResponseEntity.ok(employee);
    }
    
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        logger.debug("Creating new employee: {}", employeeDTO.getEmail());
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        logger.info("Created employee with ID: {}", createdEmployee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id, 
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        logger.debug("Updating employee with ID: {}", id);
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        logger.info("Updated employee with ID: {}", id);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable @Min(value = 1, message = "ID must be greater than 0") Long id) {
        logger.debug("Deleting employee with ID: {}", id);
        employeeService.deleteEmployee(id);
        logger.info("Deleted employee with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
