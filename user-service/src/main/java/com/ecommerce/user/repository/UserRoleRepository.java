package com.ecommerce.user.repository;

import com.ecommerce.user.entity.UserRole;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {


    List<UserRole> findByUser(User user);

    List<UserRole> findByRole(Role role);


    Optional<UserRole> findByUserAndRole(User user, Role role);


    boolean existsByUserAndRole(User user, Role role);


    void deleteByUser(User user);


    void deleteByUserAndRole(User user, Role role);


    @Query("SELECT ur.role FROM UserRole ur WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}