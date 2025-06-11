package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.TestType;
import com.fpt.hivtreatment.repository.TestTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller xử lý các yêu cầu API liên quan đến các loại xét nghiệm
 */
@RestController
@RequestMapping("/api/test-types")
public class TestTypeController {

    @Autowired
    private TestTypeRepository testTypeRepository;

    /**
     * API lấy tất cả loại xét nghiệm
     */
    @GetMapping
    public ResponseEntity<List<TestType>> getAllTestTypes() {
        List<TestType> testTypes = testTypeRepository.findAll();
        return ResponseEntity.ok(testTypes);
    }

    /**
     * API lấy loại xét nghiệm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTestTypeById(@PathVariable Long id) {
        Optional<TestType> testType = testTypeRepository.findById(id);

        if (testType.isPresent()) {
            return ResponseEntity.ok(testType.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Không tìm thấy loại xét nghiệm");
            response.put("id", id.toString());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * API lấy loại xét nghiệm theo category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TestType>> getTestTypesByCategory(@PathVariable String category) {
        List<TestType> testTypes = testTypeRepository.findByCategory(category);
        return ResponseEntity.ok(testTypes);
    }

    /**
     * API lấy loại xét nghiệm theo test group
     */
    @GetMapping("/group/{testGroup}")
    public ResponseEntity<List<TestType>> getTestTypesByTestGroup(@PathVariable String testGroup) {
        List<TestType> testTypes = testTypeRepository.findByTestGroup(testGroup);
        return ResponseEntity.ok(testTypes);
    }

    /**
     * API lấy xét nghiệm HIV theo category
     */
    @GetMapping("/hiv/{category}")
    public ResponseEntity<List<TestType>> getHIVTestsByCategory(@PathVariable String category) {
        List<TestType> testTypes = testTypeRepository.findHIVTestsByCategory(category);
        return ResponseEntity.ok(testTypes);
    }

    /**
     * API lấy tất cả xét nghiệm HIV
     */
    @GetMapping("/hiv")
    public ResponseEntity<List<TestType>> getAllHIVTests() {
        List<TestType> testTypes = testTypeRepository.findAllHIVTests();
        return ResponseEntity.ok(testTypes);
    }
}