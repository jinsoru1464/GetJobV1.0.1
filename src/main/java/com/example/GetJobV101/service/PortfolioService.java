package com.example.GetJobV101.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.example.GetJobV101.dto.PortfolioDto;
import com.example.GetJobV101.entity.Portfolio;
import com.example.GetJobV101.entity.User;
import com.example.GetJobV101.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.lang.Nullable; // 또는 javax.annotation.Nullable

/*//서버 열면{
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;
    //}*/

//로컬 열면{
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket:}")
    private String bucket;

    public PortfolioService(
            PortfolioRepository portfolioRepository,
            @Nullable AmazonS3 amazonS3 // 🚩 null 허용
    ) {
        this.portfolioRepository = portfolioRepository;
        this.amazonS3 = amazonS3;
    }

    public boolean isS3Enabled() {
        return amazonS3 != null;
    }
//}

    // ✅ 포트폴리오 저장 메소드
    public Portfolio savePortfolio(PortfolioDto dto) {
        Portfolio portfolio = new Portfolio();
        portfolio.setTitle(dto.getTitle());
        portfolio.setSubject(dto.getSubject());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        portfolio.setStartDate(LocalDate.parse(dto.getStartDate(), formatter));
        portfolio.setEndDate(LocalDate.parse(dto.getEndDate(), formatter));

        portfolio.setTeamSize(dto.getTeamSize());
        portfolio.setSkills(dto.getSkills());
        portfolio.setRole(dto.getRole());
        portfolio.setDescriptions(dto.getDescriptions());

        // ✅ 이미지 경로 저장
        List<String> imagePaths = dto.getImagePaths();
        if (imagePaths == null || imagePaths.isEmpty()) {
            imagePaths = List.of("https://get-job-bucket.s3.ap-northeast-2.amazonaws.com/defaults/default.png");
        }
        portfolio.setImagePaths(imagePaths);

        portfolio.setUser(dto.getUser());
        return portfolioRepository.save(portfolio);
    }

    // ✅ 이미지 추가 저장 메소드
    public void addImagePaths(Long portfolioId, List<String> imagePaths) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다."));
        portfolio.getImagePaths().addAll(imagePaths);
        portfolioRepository.save(portfolio);
    }

    // 파일 업로드용 임시 url인 presigned url을 생성해서 프론트한테 반환
    public Map<String, String> getPresignedUrl(String prefix, String fileName) {
        if (!prefix.isEmpty()) {
            fileName = createPath(prefix, fileName);
        }

        GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePresignedUrlRequest(bucket, fileName);
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return Map.of("preSignedUrl", url.toString());
    }






    //presigned url 생성 (put 메소드로)
    private GeneratePresignedUrlRequest getGeneratePresignedUrlRequest(String bucket, String fileName) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(getPresignedUrlExpiration());

        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL,
                CannedAccessControlList.PublicRead.toString()
        );

        return generatePresignedUrlRequest;
    }

    // presigned 유효기간은 2분.
    private Date getPresignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 2;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    // UUID로 고유한 파일 id 만들기
    private String createFileId() {
        return UUID.randomUUID().toString();
    }

    // 고유한 파일 id를 써서 또 고유한 path를 만들기
    private String createPath(String prefix, String fileName) {
        String fileId = createFileId();
        return String.format("%s/%s", prefix, fileId + "-" + fileName);
    }

    // ✅ 포트폴리오 목록 조회 메소드
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

    public List<Portfolio> getPortfoliosByUser(User user) {
        return portfolioRepository.findAllByUser(user);
    }

    // ✅ 단일 포트폴리오 조회 메소드
    public Optional<Portfolio> getPortfolioById(Long id) {
        return portfolioRepository.findById(id);
    }

    // ✅ 포트폴리오 삭제 메소드
    public void deletePortfolio(Long id) {
        if (portfolioRepository.existsById(id)) {
            portfolioRepository.deleteById(id);
        } else {
            throw new RuntimeException("삭제하려는 포트폴리오가 존재하지 않습니다.");
        }
    }
    // ✅ 포트폴리오 수정 메소드
    public Portfolio updatePortfolio(Long id, PortfolioDto dto) {
        // 기존 포트폴리오를 조회하여 존재 여부 확인
        Optional<Portfolio> existingPortfolioOpt = portfolioRepository.findById(id);
        if (existingPortfolioOpt.isPresent()) {
            Portfolio existingPortfolio = existingPortfolioOpt.get();

            // 제목, 날짜, 인원, 스킬, 역할 등 업데이트
            existingPortfolio.setTitle(dto.getTitle());
            existingPortfolio.setSubject(dto.getSubject());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            existingPortfolio.setStartDate(LocalDate.parse(dto.getStartDate(), formatter));
            existingPortfolio.setEndDate(LocalDate.parse(dto.getEndDate(), formatter));

            existingPortfolio.setTeamSize(dto.getTeamSize());
            existingPortfolio.setSkills(dto.getSkills());
            existingPortfolio.setRole(dto.getRole());

            // 설명 업데이트
            existingPortfolio.setDescriptions(dto.getDescriptions());

            // 이미지 경로 업데이트
            List<String> imagePaths = dto.getImagePaths();
            if (imagePaths != null && !imagePaths.isEmpty()) {
                existingPortfolio.setImagePaths(imagePaths);
            }
// null이거나 빈 배열이면 기존 이미지 유지

// else일 경우 -> 기존 이미지 유지


            if (dto.getUser() != null) {
                existingPortfolio.setUser(dto.getUser());
            }

            // 수정된 포트폴리오 저장
            return portfolioRepository.save(existingPortfolio);
        } else {
            throw new RuntimeException("수정하려는 포트폴리오가 존재하지 않습니다.");
        }
    }


}