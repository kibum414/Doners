package com.doners.donersbackend.application.service;

import com.doners.donersbackend.application.dto.request.CommentChangePatchDTO;
import com.doners.donersbackend.application.dto.request.CommentRegisterPostDTO;
import com.doners.donersbackend.application.dto.response.comment.CommentGetListWrapperResponseDTO;
import com.doners.donersbackend.application.dto.response.comment.CommentResponseDTO;
import com.doners.donersbackend.domain.dao.epilogue.Epilogue;
import com.doners.donersbackend.domain.dao.Comment;
import com.doners.donersbackend.domain.dao.Community;
import com.doners.donersbackend.domain.repository.epilogue.EpilogueRepository;
import com.doners.donersbackend.domain.repository.CommentRepository;
import com.doners.donersbackend.domain.repository.CommunityRepository;
import com.doners.donersbackend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;
    private final EpilogueRepository epilogueRepository;
    private final UserRepository userRepository;

    @Override
    public void registerComment(CommentRegisterPostDTO commentRegisterPostDTO) {
        Comment parentComment = null;
        if(commentRepository.findById(commentRegisterPostDTO.getCommentId()).isPresent()){
            parentComment = commentRepository.findById(commentRegisterPostDTO.getCommentId()).get();
        }
        // 댓글 작성 정보
        Comment comment = Comment.builder()
                .commentDescription(commentRegisterPostDTO.getCommentDescription())
                .user(userRepository.findByUserAccount(commentRegisterPostDTO.getUserAccount()).get())
                .parentCommentId(parentComment)
                .commentCreateTime(LocalDateTime.now()).build();

        if(commentRegisterPostDTO.getCommunityId().length()==0){// 감사 글 댓글
            comment.changeEpilougeId(epilogueRepository.findById(commentRegisterPostDTO.getEpilougeId()).get());
        }else{// 커뮤니티 글 댓글
            comment.changeCommunityId(communityRepository.findById(commentRegisterPostDTO.getCommunityId()).get());
        }
        commentRepository.save(comment);
    }

    @Override
    public Integer changeComment(String commentId, CommentChangePatchDTO commentChangePatchDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        try {
            comment.changeComment(commentChangePatchDTO.getCommentDescription());
        } catch(Exception e) {
            return 409;
        }

        commentRepository.save(comment);
        return 200;
    }

    @Override
    public Integer deleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        try {
            comment.deleteComment();
        } catch(Exception e) {
            return 409;
        }

        commentRepository.save(comment);
        return 200;
    }

    @Override
    public CommentGetListWrapperResponseDTO getEpilogueCommentList(String id) {
        List<CommentResponseDTO> list = new ArrayList<>();
        Epilogue epilogue = epilogueRepository.findById(id).get();

        for(Comment c : commentRepository.findAllByEpilougeAndCommentIsDeletedOrderByCommentCreateTime(epilogue, false).get()) {
            list.add(new CommentResponseDTO(c.getId(), c.getCommentCreateTime(), c.getCommentDescription()));
        }

        return new CommentGetListWrapperResponseDTO(list);
    }

    @Override
    public CommentGetListWrapperResponseDTO getCommunityCommentList(String id) {
        List<CommentResponseDTO> list = new ArrayList<>();
        Community community = communityRepository.findById(id).get();

        for(Comment c : commentRepository.findAllByCommunityAndCommentIsDeletedOrderByCommentCreateTime(community, false).get()) {
            list.add(new CommentResponseDTO(c.getId(), c.getCommentCreateTime(), c.getCommentDescription()));
        }

        return new CommentGetListWrapperResponseDTO(list);
    }

    @Override
    public CommentGetListWrapperResponseDTO getSubCommentList(String parentId) {
        List<CommentResponseDTO> list = new ArrayList<>();

        Comment comment = commentRepository.findById(parentId).get();
        for(Comment c : commentRepository.findAllByParentCommentIdAndCommentIsDeletedOrderByCommentCreateTime(comment, false).get()) {
            list.add(new CommentResponseDTO(c.getId(), c.getCommentCreateTime(), c.getCommentDescription()));
        }

        return new CommentGetListWrapperResponseDTO(list);
    }

}
