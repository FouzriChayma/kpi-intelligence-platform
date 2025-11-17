package com.entreprise.kpi_analysis_Backend.service;

import com.entreprise.kpi_analysis_Backend.dto.FileUploadResponse;
import com.entreprise.kpi_analysis_Backend.entity.Employee;
import com.entreprise.kpi_analysis_Backend.entity.KPI;
import com.entreprise.kpi_analysis_Backend.entity.KPIMetric;
import com.entreprise.kpi_analysis_Backend.repository.EmployeeRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIRepository;
import com.entreprise.kpi_analysis_Backend.repository.KPIMetricRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for processing uploaded Excel/CSV files and creating KPIs automatically
 */
@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    private final EmployeeRepository employeeRepository;
    private final KPIRepository kpiRepository;
    private final KPIMetricRepository kpiMetricRepository;
    private final AIAnalysisService aiAnalysisService;
    
    @Autowired
    public FileUploadService(
            EmployeeRepository employeeRepository,
            KPIRepository kpiRepository,
            KPIMetricRepository kpiMetricRepository,
            AIAnalysisService aiAnalysisService) {
        this.employeeRepository = employeeRepository;
        this.kpiRepository = kpiRepository;
        this.kpiMetricRepository = kpiMetricRepository;
        this.aiAnalysisService = aiAnalysisService;
    }
    
    /**
     * Process uploaded file (Excel or CSV)
     */
    @Transactional
    public FileUploadResponse processFile(MultipartFile file, LocalDate periodStart, LocalDate periodEnd) {
        FileUploadResponse response = new FileUploadResponse();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int employeesProcessed = 0;
        int kpisCreated = 0;
        int metricsCreated = 0;
        
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Nom de fichier invalide");
                return response;
            }
            
            logger.info("Processing file: {} for period: {} to {}", fileName, periodStart, periodEnd);
            
            List<Map<String, Object>> data;
            
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                data = parseExcelFile(file.getInputStream());
            } else if (fileName.endsWith(".csv")) {
                data = parseCSVFile(file.getInputStream());
            } else {
                response.setSuccess(false);
                response.setMessage("Format de fichier non supporté. Utilisez .xlsx, .xls ou .csv");
                return response;
            }
            
            if (data.isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Aucune donnée trouvée dans le fichier");
                return response;
            }
            
            // Process data and create KPIs
            for (Map<String, Object> row : data) {
                try {
                    Employee employee = findOrCreateEmployee(row);
                    // Ensure employee has an ID (flush to database)
                    if (employee.getId() == null) {
                        employee = employeeRepository.saveAndFlush(employee);
                    }
                    
                    KPI kpi = findOrCreateKPI(employee, periodStart, periodEnd);
                    
                    if (kpi.getId() == null) {
                        kpi = kpiRepository.saveAndFlush(kpi);
                        kpisCreated++;
                    }
                    
                    // Create metrics from row data
                    int metricsInRow = createMetricsFromRow(kpi, row);
                    metricsCreated += metricsInRow;
                    
                    employeesProcessed++;
                    
                } catch (Exception e) {
                    String errorMsg = String.format("Erreur lors du traitement de la ligne: %s", e.getMessage());
                    errors.add(errorMsg);
                    logger.error("Error processing row: {}", row, e);
                    // Log full stack trace for debugging
                    logger.error("Stack trace:", e);
                }
            }
            
            response.setSuccess(true);
            response.setMessage(String.format(
                "Fichier traité avec succès: %d employé(s) traité(s), %d KPI(s) créé(s), %d métrique(s) créée(s)",
                employeesProcessed, kpisCreated, metricsCreated
            ));
            response.setEmployeesProcessed(employeesProcessed);
            response.setKpisCreated(kpisCreated);
            response.setMetricsCreated(metricsCreated);
            response.setWarnings(warnings);
            response.setErrors(errors);
            
            logger.info("File processing completed: {} employees, {} KPIs, {} metrics", 
                employeesProcessed, kpisCreated, metricsCreated);
            
        } catch (Exception e) {
            logger.error("Error processing file", e);
            response.setSuccess(false);
            response.setMessage("Erreur lors du traitement du fichier: " + e.getMessage());
            if (e.getCause() != null) {
                response.setMessage(response.getMessage() + " - Cause: " + e.getCause().getMessage());
            }
            response.setErrors(List.of(e.getMessage()));
        }
        
        // Trigger AI analysis AFTER transaction commits (outside @Transactional)
        if (response.isSuccess() && kpisCreated > 0) {
            try {
                logger.info("Triggering AI analysis for {} new KPIs", kpisCreated);
                List<KPI> newKPIs = kpiRepository.findByPeriodStartAndPeriodEnd(periodStart, periodEnd);
                for (KPI kpi : newKPIs) {
                    try {
                        aiAnalysisService.updateKPIWithAnalysis(kpi.getId());
                    } catch (Exception e) {
                        warnings.add("L'analyse IA n'a pas pu être générée pour le KPI ID: " + kpi.getId());
                        logger.warn("Failed to generate AI analysis for KPI: {}", kpi.getId(), e);
                    }
                }
                response.setWarnings(warnings);
            } catch (Exception e) {
                logger.warn("Failed to trigger AI analysis after file upload", e);
                warnings.add("L'analyse IA n'a pas pu être déclenchée automatiquement");
                response.setWarnings(warnings);
            }
        }
        
        return response;
    }
    
    /**
     * Parse Excel file (.xlsx, .xls)
     */
    private List<Map<String, Object>> parseExcelFile(InputStream inputStream) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // First sheet
            
            if (sheet.getPhysicalNumberOfRows() < 2) {
                return data; // Need at least header + 1 data row
            }
            
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            
            // Read headers
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell).trim());
            }
            
            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;
                
                for (int j = 0; j < headers.size() && j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    String value = getCellValueAsString(cell);
                    if (!value.isEmpty()) {
                        hasData = true;
                    }
                    rowData.put(headers.get(j), value);
                }
                
                if (hasData) {
                    data.add(rowData);
                }
            }
        }
        
        return data;
    }
    
    /**
     * Parse CSV file using OpenCSV
     */
    private List<Map<String, Object>> parseCSVFile(InputStream inputStream) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String[]> allRows = reader.readAll();
            
            if (allRows.isEmpty()) {
                return data;
            }
            
            // First row is headers
            String[] headers = allRows.get(0);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }
            
            // Process data rows
            for (int i = 1; i < allRows.size(); i++) {
                String[] values = allRows.get(i);
                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;
                
                for (int j = 0; j < headers.length && j < values.length; j++) {
                    String value = values[j] != null ? values[j].trim() : "";
                    if (!value.isEmpty()) {
                        hasData = true;
                    }
                    rowData.put(headers[j], value);
                }
                
                if (hasData) {
                    data.add(rowData);
                }
            }
        } catch (CsvException e) {
            logger.error("Error parsing CSV file", e);
            throw new Exception("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format as integer if no decimal part
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * Find or create employee from row data
     */
    private Employee findOrCreateEmployee(Map<String, Object> row) {
        // Try to find by email first (most reliable)
        String email = getStringValue(row, "email", "Email", "Email", "e-mail");
        if (email != null && !email.isEmpty()) {
            return employeeRepository.findByEmail(email)
                .orElseGet(() -> createEmployeeFromRow(row));
        }
        
        // Try to find by name
        String firstName = getStringValue(row, "firstName", "first_name", "First Name", "Prénom", "prenom");
        String lastName = getStringValue(row, "lastName", "last_name", "Last Name", "Nom", "nom");
        
        if (firstName != null && lastName != null) {
            // Check if employee exists (simple check - could be improved)
            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee emp : allEmployees) {
                if (emp.getFirstName().equalsIgnoreCase(firstName) && 
                    emp.getLastName().equalsIgnoreCase(lastName)) {
                    return emp;
                }
            }
        }
        
        return createEmployeeFromRow(row);
    }
    
    /**
     * Create employee from row data
     */
    private Employee createEmployeeFromRow(Map<String, Object> row) {
        Employee employee = new Employee();
        
        String firstName = getStringValue(row, "firstName", "first_name", "First Name", "Prénom", "prenom");
        String lastName = getStringValue(row, "lastName", "last_name", "Last Name", "Nom", "nom");
        String email = getStringValue(row, "email", "Email", "Email", "e-mail");
        String department = getStringValue(row, "department", "Department", "Département", "departement", "Dept", "dept");
        String position = getStringValue(row, "position", "Position", "Poste", "poste", "Job", "job", "Role", "role");
        
        // Validate required fields
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le prénom est requis pour créer un employé");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est requis pour créer un employé");
        }
        if (email == null || email.trim().isEmpty()) {
            email = generateEmail(firstName, lastName);
        }
        if (department == null || department.trim().isEmpty()) {
            department = "Non spécifié";
        }
        if (position == null || position.trim().isEmpty()) {
            position = "Non spécifié";
        }
        
        employee.setFirstName(firstName.trim());
        employee.setLastName(lastName.trim());
        employee.setEmail(email.trim());
        employee.setDepartment(department.trim());
        employee.setPosition(position.trim());
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Generate email if not provided
     */
    private String generateEmail(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return "employee" + System.currentTimeMillis() + "@example.com";
        }
        return (firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com")
            .replaceAll("\\s+", ".");
    }
    
    /**
     * Find or create KPI for employee and period
     */
    private KPI findOrCreateKPI(Employee employee, LocalDate periodStart, LocalDate periodEnd) {
        List<KPI> existingKPIs = kpiRepository.findByEmployeeId(employee.getId());
        
        for (KPI kpi : existingKPIs) {
            if (kpi.getPeriodStart().equals(periodStart) && kpi.getPeriodEnd().equals(periodEnd)) {
                return kpi;
            }
        }
        
        // Create new KPI
        KPI kpi = new KPI();
        kpi.setEmployee(employee);
        kpi.setPeriodStart(periodStart);
        kpi.setPeriodEnd(periodEnd);
        
        return kpi;
    }
    
    /**
     * Create metrics from row data
     */
    private int createMetricsFromRow(KPI kpi, Map<String, Object> row) {
        int metricsCreated = 0;
        
        // Map common column names to metric types
        Map<String, KPIMetric.MetricType> metricMappings = new HashMap<>();
        metricMappings.put("attendance", KPIMetric.MetricType.ATTENDANCE);
        metricMappings.put("assiduité", KPIMetric.MetricType.ATTENDANCE);
        metricMappings.put("assiduite", KPIMetric.MetricType.ATTENDANCE);
        metricMappings.put("présence", KPIMetric.MetricType.ATTENDANCE);
        metricMappings.put("presence", KPIMetric.MetricType.ATTENDANCE);
        metricMappings.put("velocity", KPIMetric.MetricType.VELOCITY);
        metricMappings.put("vélocité", KPIMetric.MetricType.VELOCITY);
        metricMappings.put("velocite", KPIMetric.MetricType.VELOCITY);
        metricMappings.put("quality", KPIMetric.MetricType.QUALITY);
        metricMappings.put("qualité", KPIMetric.MetricType.QUALITY);
        metricMappings.put("qualite", KPIMetric.MetricType.QUALITY);
        metricMappings.put("productivity", KPIMetric.MetricType.PRODUCTIVITY);
        metricMappings.put("productivité", KPIMetric.MetricType.PRODUCTIVITY);
        metricMappings.put("productivite", KPIMetric.MetricType.PRODUCTIVITY);
        metricMappings.put("efficiency", KPIMetric.MetricType.EFFICIENCY);
        metricMappings.put("efficacité", KPIMetric.MetricType.EFFICIENCY);
        metricMappings.put("efficacite", KPIMetric.MetricType.EFFICIENCY);
        
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String columnName = entry.getKey().toLowerCase().trim();
            Object value = entry.getValue();
            
            // Check if this column matches a metric type
            KPIMetric.MetricType metricType = null;
            for (Map.Entry<String, KPIMetric.MetricType> mapping : metricMappings.entrySet()) {
                if (columnName.contains(mapping.getKey())) {
                    metricType = mapping.getValue();
                    break;
                }
            }
            
            if (metricType != null && value != null) {
                try {
                    double doubleValue = parseDouble(value);
                    
                    // Check for target value (column name might contain "target", "objectif", "goal")
                    Double targetValue = null;
                    for (Map.Entry<String, Object> targetEntry : row.entrySet()) {
                        String targetColumnName = targetEntry.getKey().toLowerCase();
                        if (targetColumnName.contains("target") ||
                            targetColumnName.contains("objectif") ||
                            targetColumnName.contains("goal")) {
                            try {
                                targetValue = parseDouble(targetEntry.getValue());
                                break;
                            } catch (NumberFormatException e) {
                                // Continue searching
                            }
                        }
                    }
                    
                    // Create metric
                    KPIMetric metric = new KPIMetric();
                    metric.setKpi(kpi);
                    metric.setMetricType(metricType);
                    metric.setValue(doubleValue);
                    metric.setTargetValue(targetValue);
                    metric.setUnit("%");
                    
                    kpiMetricRepository.save(metric);
                    metricsCreated++;
                    
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse value for column {}: {}", columnName, value);
                }
            }
        }
        
        return metricsCreated;
    }
    
    /**
     * Parse value to double
     */
    private double parseDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String str = ((String) value).trim().replace(",", ".");
            return Double.parseDouble(str);
        }
        throw new NumberFormatException("Cannot parse: " + value);
    }
    
    /**
     * Get string value from row with multiple possible keys
     */
    private String getStringValue(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    Object value = entry.getValue();
                    return value != null ? value.toString().trim() : null;
                }
            }
        }
        return null;
    }
}

