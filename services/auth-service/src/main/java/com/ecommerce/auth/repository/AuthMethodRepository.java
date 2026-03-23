package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.AuthMethod;
import com.ecommerce.auth.entity.AuthProvider;
import com.ecommerce.auth.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthMethodRepository extends JpaRepository<AuthMethod, Long> {

    Optional<AuthMethod> findByUserAndProvider(User user, AuthProvider provider);

    List<AuthMethod> findByUser(User user);
}
