package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.ProtocolMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProtocolMedicationRepository extends JpaRepository<ProtocolMedication, Long> {

    @Query("SELECT pm FROM ProtocolMedication pm JOIN FETCH pm.medication WHERE pm.protocol.id = :protocolId")
    List<ProtocolMedication> findByProtocolId(@Param("protocolId") Long protocolId);
}