package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.model.entity.Medication;
import com.fpt.hivtreatment.model.entity.TreatmentProtocol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TreatmentProtocolService {
    List<TreatmentProtocol> getAllProtocols();

    Optional<TreatmentProtocol> getProtocolById(Long id);

    List<TreatmentProtocol> getProtocolsByCategory(String category);

    List<TreatmentProtocol> getProtocolsByIndication(String indication);

    TreatmentProtocol saveProtocol(TreatmentProtocol protocol);

    void deleteProtocol(Long id);

    // Methods for protocol medications
    List<Medication> getMedicationsByProtocolId(Long protocolId);

    // Method to get all protocols with their medications
    Map<TreatmentProtocol, List<Medication>> getAllProtocolsWithMedications();
}