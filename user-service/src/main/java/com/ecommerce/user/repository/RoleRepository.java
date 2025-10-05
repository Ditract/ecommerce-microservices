package com.ecommerce.user.repository;

import com.ecommerce.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    List<Role> findByNameContainingIgnoreCase(String name);
}