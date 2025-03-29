package com.example.GetJobV101.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private int teamSize;
    private String skills;
    private String role;

    @ElementCollection
    @CollectionTable(name = "portfolio_descriptions", joinColumns = @JoinColumn(name = "portfolio_id"))
    private List<String> descriptions = new ArrayList<>();  // ✅ 초기화 필수

    @ElementCollection
    @CollectionTable(name = "portfolio_images", joinColumns = @JoinColumn(name = "portfolio_id"))
    private List<String> imagePaths = new ArrayList<>();    // 🔥 반드시 초기화 필수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
