package com.doners.donersbackend.application.service;

import com.doners.donersbackend.application.dto.request.user.UserRegisterRequestDTO;
import com.doners.donersbackend.application.dto.response.user.*;
import com.doners.donersbackend.domain.dao.community.Community;
import com.doners.donersbackend.domain.dao.donation.Donation;
import com.doners.donersbackend.domain.dao.email.EmailConfirmation;
import com.doners.donersbackend.domain.dao.epilogue.Epilogue;
import com.doners.donersbackend.domain.dao.image.Image;
import com.doners.donersbackend.domain.dao.user.User;
import com.doners.donersbackend.domain.repository.CommunityRepository;
import com.doners.donersbackend.domain.repository.EmailConfirmationRepository;
import com.doners.donersbackend.domain.repository.ImageRepository;
import com.doners.donersbackend.domain.repository.UserRepository;
import com.doners.donersbackend.domain.repository.donation.DonationRepository;
import com.doners.donersbackend.domain.repository.epilogue.EpilogueRepository;
import com.doners.donersbackend.security.util.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ImageRepository imageRepository;

    private final EmailConfirmationRepository emailConfirmationRepository;

    private final DonationRepository donationRepository;

    private final CommunityRepository communityRepository;

    private final EpilogueRepository epilogueRepository;

    private final AwsS3Service awsS3Service;

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    // 회원가입 : 필수 회원 정보 입력 - 이름, 이메일, 닉네임
    @Override
    public Integer registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {

        String userEmail = userRegisterRequestDTO.getUserEmail();
        String userAccount = userRegisterRequestDTO.getUserAccount();

        // 이미 해당 이메일로 가입한 계정 존재하는지 확인
        if(userRepository.findByUserEmailAndUserIsDeleted(userEmail, false).isPresent()) {
            return 409;
        }

        // 이미 해당 메타마스크 계정 주소로 가입한 계정 존재하는지 확인
        if(userRepository.findByUserAccountAndUserIsDeleted(userAccount, false).isPresent()) {
            return 409;
        }

        // account 정보 추가할 것
        User user = User.builder()
                .userName(userRegisterRequestDTO.getUserName())
                .userNickname(userRegisterRequestDTO.getUserNickname())
                .userEmail(userEmail)
                .userAccount(userAccount)
                .userCode(userRegisterRequestDTO.getUserCode())
                .password("")
                .build();

        userRepository.save(user);

        return 201;

    }

    @Override
    public UserLoginResponseDTO getUserLoginResponseDTO(String userAccount) {
        User user = userRepository.findByUserAccountAndUserIsDeleted(userAccount, false)
                .orElseThrow(() -> new IllegalArgumentException("해당 메타마스크 계정 주소로 가입된 정보가 없습니다."));

        try {
            return UserLoginResponseDTO.builder()
                    .userNickname(user.getUserNickname())
                    .userCode(user.getUserCode()).build();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UserAccountResponseDTO getUserAccountResponseDTO(String accessToken, String userNickname) {
        User requestUser = getUserFromAccessToken(accessToken);

        User user = userRepository.findByUserNicknameAndUserIsDeleted(userNickname, false)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        return UserAccountResponseDTO.builder()
                .userAccount(user.getUserAccount()).build();
    }

    @Override
    public UserNameResponseDTO getUserNameResponseDTO(String accessToken, String userNickname) {
        User requestUser = getUserFromAccessToken(accessToken);

        User user = userRepository.findByUserNicknameAndUserIsDeleted(userNickname, false)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        return UserNameResponseDTO.builder()
                .userName(user.getUserName()).build();
    }

    // 닉네임 변경
    @Override
    public Integer changeUserNickname(String accessToken, String userNickname) {
        User user = getUserFromAccessToken(accessToken);

        try {
            user.changeNickname(userNickname);
        } catch(Exception e) {
            return 409;
        }

        userRepository.save(user);

        return 200;
    }

    // 닉네임 중복 체크
    @Override
    public Integer checkNickname(String userNickname) {
        if(userRepository.findByUserNicknameAndUserIsDeleted(userNickname, false).isPresent())
            return 409;

        return 200;
    }

    @Transactional
    @Override
    public void deleteUser(String accessToken) {
        User user = getUserFromAccessToken(accessToken);

        user.deleteUser();
        userRepository.save(user);

        EmailConfirmation emailConfirmation = emailConfirmationRepository.findByEmailAddress(user.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 인증 정보를 찾을 수 없습니다."));

        try {
            emailConfirmationRepository.delete(emailConfirmation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void uploadProfileImage(String accessToken, MultipartFile multipartFile) {
        User user = getUserFromAccessToken(accessToken);

        String fileName = awsS3Service.uploadImage(multipartFile);

        Image image = imageRepository.findByUserAndImageIsResized(user, false).orElse(null);

        if (image == null) {
            image = Image.builder()
                    .imageOriginFileName(multipartFile.getOriginalFilename())
                    .imageNewFileName(fileName)
                    .user(user).build();
        } else {
            image.changeImage(multipartFile.getOriginalFilename(), fileName);
        }

        imageRepository.save(image);

        String thumbnailFileName = awsS3Service.uploadThumbnailImage(fileName, multipartFile);

        Image thumbnailImage = imageRepository.findByUserAndImageIsResized(user, true).orElse(null);

        if (thumbnailImage == null) {
            thumbnailImage = Image.builder()
                    .imageOriginFileName(multipartFile.getOriginalFilename())
                    .imageNewFileName(thumbnailFileName)
                    .imageIsResized(true)
                    .user(user).build();
        } else {
            thumbnailImage.changeImage(multipartFile.getOriginalFilename(), thumbnailFileName);
        }

        imageRepository.save(thumbnailImage);

    }

    @Override
    public String getProfileImage(String accessToken, String userNickname) {
        User reqeustUser = getUserFromAccessToken(accessToken);

        User user = userRepository.findByUserNicknameAndUserIsDeleted(userNickname, false)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임을 가진 유저 정보가 존재하지 않습니다."));

        Image profileImage = imageRepository.findByUserAndImageIsResized(user, false).orElse(null);

        return profileImage == null ? "" : "https://donersa404.s3.ap-northeast-2.amazonaws.com/" + profileImage.getImageNewFileName();
    }

    @Override
    public UserMyPageCommunityHistoryWrapperResponseDTO getCommunityHistoryList(String accessToken) {
        User user = getUserFromAccessToken(accessToken);

        List<Community> communityList = communityRepository
                .findByUserAndCommunityIsDeletedOrderByCommunityCreateTimeDesc(user, false)
                .orElseThrow(() -> new IllegalArgumentException("작성한 커뮤니티 글이 존재하지 않습니다."));

        List<UserMyPageCommunityHistoryResponseDTO> list = new ArrayList<>();

        try {
            communityList.forEach(community -> {
                list.add(
                        UserMyPageCommunityHistoryResponseDTO.builder()
                                .communityId(community.getId())
                                .communityTitle(community.getCommunityTitle())
                                .communityCreateTime(community.getCommunityCreateTime()).build()
                );
            });
        } catch (Exception e) {
            return null;
        }

        return UserMyPageCommunityHistoryWrapperResponseDTO.builder()
                .userMyPageCommunityHistoryResponseDTOList(list).build();
    }

    @Override
    public UserMyPageEpilogueHistoryWrapperResponseDTO getEpilogueHistoryList(String accessToken) {
        User user = getUserFromAccessToken(accessToken);

        List<Epilogue> epilogueList = epilogueRepository
                .findByUserAndEpilogueIsDeletedOrderByEpilogueCreateTimeDesc(user, false)
                .orElseThrow(() -> new IllegalArgumentException("작성한 감사 글이 존재하지 않습니다."));

        List<UserMyPageEpilogueHistoryResponseDTO> list = new ArrayList<>();

        try {
            epilogueList.forEach(epilogue -> {
                list.add(
                        UserMyPageEpilogueHistoryResponseDTO.builder()
                                .epilogueId(epilogue.getId())
                                .epilogueTitle(epilogue.getEpilogueTitle())
                                .epilogueCreateTime(epilogue.getEpilogueCreateTime()).build()
                );
            });
        } catch (Exception e) {
            return null;
        }

        return UserMyPageEpilogueHistoryWrapperResponseDTO.builder()
                .userMyPageEpilogueHistoryResponseDTOList(list).build();
    }

    @Override
    public UserMyPageDonationHistoryWrapperResponseDTO getDonationHistoryList(String accessToken) {
        User user = getUserFromAccessToken(accessToken);

        List<Donation> donationList = donationRepository
                .findByUserAndIsDeletedOrderByEndDateDesc(user, false)
                .orElseThrow(() -> new IllegalArgumentException("기부 신청 내역이 존재하지 않습니다."));

        List<UserMyPageDonationHistoryResponseDTO> list = new ArrayList<>();

        try {
            donationList.forEach(donation -> {
                Image thumbnailImage = imageRepository.findByDonationAndImageIsResized(donation, true).orElse(null);
                list.add(
                        UserMyPageDonationHistoryResponseDTO.builder()
                                .donationId(donation.getId())
                                .donationCategoryCode(donation.getCategoryCode())
                                .donationIsApproved(donation.isApproved())
                                .donationApprovalStatusCode(donation.getApprovalStatusCode())
                                .donationTitle(donation.getTitle())
                                .donationStartDate(donation.getStartDate())
                                .thumbnailImage("https://donersa404.s3.ap-northeast-2.amazonaws.com/" + thumbnailImage.getImageNewFileName())
                                .targetAmount(donation.getAmount())
                                .endDate(donation.getEndDate())
                                .contractAddress(donation.getContractAddress()).build()
                );
            });
        } catch (Exception e) {
            return null;
        }

        return UserMyPageDonationHistoryWrapperResponseDTO.builder()
                .userMyPageDonationHistoryResponseDTOList(list).build();
    }

    public User getUserFromAccessToken(String accessToken) {
        String token = accessToken.split(" ")[1];
        String userAccount = jwtAuthenticationProvider.getUserAccount(token);

        return userRepository.findByUserAccount(userAccount)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }
}
