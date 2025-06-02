package com.fpt.hivtreatment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fpt.hivtreatment.model.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}