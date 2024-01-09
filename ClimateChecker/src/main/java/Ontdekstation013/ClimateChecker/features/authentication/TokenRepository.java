package Ontdekstation013.ClimateChecker.features.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Ontdekstation013.ClimateChecker.features.user.User;

@Repository

public interface TokenRepository extends JpaRepository<Token, Long> {
    boolean existsByUser(User user);
    Token findByUser(User user);
}