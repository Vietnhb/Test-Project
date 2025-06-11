package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.model.entity.Medication;
import com.fpt.hivtreatment.model.entity.TreatmentProtocol;
import com.fpt.hivtreatment.service.TreatmentProtocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/treatment-protocols")
public class TreatmentProtocolController {

    private final TreatmentProtocolService treatmentProtocolService;

    @Autowired
    public TreatmentProtocolController(TreatmentProtocolService treatmentProtocolService) {
        this.treatmentProtocolService = treatmentProtocolService;
    }

    @GetMapping
    public ResponseEntity<List<TreatmentProtocol>> getAllProtocols() {
        try {
            List<TreatmentProtocol> protocols = treatmentProtocolService.getAllProtocols();
            System.out.println("Returning " + protocols.size() + " protocols to client");
            return new ResponseEntity<>(protocols, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching protocols: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentProtocol> getProtocolById(@PathVariable Long id) {
        try {
            Optional<TreatmentProtocol> protocol = treatmentProtocolService.getProtocolById(id);
            return protocol.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            System.err.println("Error fetching protocol by id " + id + ": " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<TreatmentProtocol>> getProtocolsByCategory(@PathVariable String category) {
        try {
            List<TreatmentProtocol> protocols = treatmentProtocolService.getProtocolsByCategory(category);
            return new ResponseEntity<>(protocols, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching protocols by category: " + e.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/indication/{indication}")
    public ResponseEntity<List<TreatmentProtocol>> getProtocolsByIndication(@PathVariable String indication) {
        try {
            List<TreatmentProtocol> protocols = treatmentProtocolService.getProtocolsByIndication(indication);
            return new ResponseEntity<>(protocols, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching protocols by indication: " + e.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/medications")
    public ResponseEntity<List<Medication>> getMedicationsByProtocolId(@PathVariable Long id) {
        try {
            System.out.println("Controller: Fetching medications for protocol ID: " + id);
            List<Medication> medications = treatmentProtocolService.getMedicationsByProtocolId(id);
            System.out.println("Controller: Found " + medications.size() + " medications for protocol ID: " + id);
            return new ResponseEntity<>(medications, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching medications for protocol: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/with-medications")
    public ResponseEntity<List<Map<String, Object>>> getAllProtocolsWithMedications() {
        try {
            Map<TreatmentProtocol, List<Medication>> protocolsWithMedications = treatmentProtocolService
                    .getAllProtocolsWithMedications();

            // Convert to a JSON-friendly format as a list of maps
            List<Map<String, Object>> response = new ArrayList<>();
            for (Map.Entry<TreatmentProtocol, List<Medication>> entry : protocolsWithMedications.entrySet()) {
                Map<String, Object> protocolData = new HashMap<>();
                protocolData.put("protocol", entry.getKey());
                protocolData.put("medications", entry.getValue());
                response.add(protocolData);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching protocols with medications: " + e.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<TreatmentProtocol> createProtocol(@RequestBody TreatmentProtocol protocol) {
        try {
            TreatmentProtocol savedProtocol = treatmentProtocolService.saveProtocol(protocol);
            return new ResponseEntity<>(savedProtocol, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating protocol: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentProtocol> updateProtocol(
            @PathVariable Long id, @RequestBody TreatmentProtocol protocol) {
        try {
            Optional<TreatmentProtocol> existingProtocol = treatmentProtocolService.getProtocolById(id);
            if (existingProtocol.isPresent()) {
                protocol.setId(id);
                TreatmentProtocol updatedProtocol = treatmentProtocolService.saveProtocol(protocol);
                return new ResponseEntity<>(updatedProtocol, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error updating protocol: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProtocol(@PathVariable Long id) {
        try {
            Optional<TreatmentProtocol> existingProtocol = treatmentProtocolService.getProtocolById(id);
            if (existingProtocol.isPresent()) {
                treatmentProtocolService.deleteProtocol(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error deleting protocol: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}