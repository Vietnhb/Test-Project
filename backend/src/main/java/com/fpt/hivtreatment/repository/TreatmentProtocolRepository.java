package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.TreatmentProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreatmentProtocolRepository extends JpaRepository<TreatmentProtocol, Long> {
    List<TreatmentProtocol> findByCategory(String category);

    List<TreatmentProtocol> findByIndication(String indication);
}