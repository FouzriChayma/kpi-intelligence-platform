package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.EmployeeDTO;
import com.entreprise.kpi_analysis_Backend.entity.Employee;
import com.entreprise.kpi_analysis_Backend.exception.ResourceNotFoundException;
import com.entreprise.kpi_analysis_Backend.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Employee business logic
 */
@Service
@Transactional
public class EmployeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        logger.debug("Fetching all employees from repository");
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        logger.debug("Fetching employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return convertToDTO(employee);
    }
    
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        logger.debug("Creating new employee: {}", employeeDTO.getEmail());
        Employee employee = convertToEntity(employeeDTO);
        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee created successfully with ID: {}", savedEmployee.getId());
        return convertToDTO(savedEmployee);
    }
    
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        logger.debug("Updating employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        
        // Copy properties from DTO to entity (ignoring id and kpis)
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setDepartment(employeeDTO.getDepartment());
        employee.setPosition(employeeDTO.getPosition());
        
        Employee updatedEmployee = employeeRepository.save(employee);
        logger.info("Employee updated successfully with ID: {}", id);
        return convertToDTO(updatedEmployee);
    }
    
    public void deleteEmployee(Long id) {
        logger.debug("Deleting employee with ID: {}", id);
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", id);
        }
        employeeRepository.deleteById(id);
        logger.info("Employee deleted successfully with ID: {}", id);
    }
    
    // Helper methods for conversion
    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());
        dto.setPosition(employee.getPosition());
        return dto;
    }
    
    private Employee convertToEntity(EmployeeDTO dto) {
        Employee employee = new Employee();
        if (dto.getId() != null) {
            employee.setId(dto.getId());
        }
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        return employee;
    }
    
    // Internal method to get entity by ID (for use by other services)
    public Employee getEmployeeEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }
}
