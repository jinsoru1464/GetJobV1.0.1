package com.example.GetJobV101.repository;

import com.example.GetJobV101.entity.Portfolio;
import com.example.GetJobV101.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findAllByUser(User user);
}
