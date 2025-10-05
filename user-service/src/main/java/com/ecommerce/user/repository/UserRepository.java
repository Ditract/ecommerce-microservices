package com.ecommerce.user.repository;

import com.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsActive(Boolean isActive);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName
    );


    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r " +
            "WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);


    @Query(value = "SELECT COUNT(DISTINCT u.id) FROM users u " +
            "INNER JOIN user_roles ur ON u.id = ur.user_id " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name = :roleName",
            nativeQuery = true)
    Long countUsersByRoleName(@Param("roleName") String roleName);
}