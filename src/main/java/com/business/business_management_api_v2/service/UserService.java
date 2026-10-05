package com.business.business_management_api_v2.service;

import com.business.business_management_api_v2.dto.response.UserResponse;
import com.business.business_management_api_v2.entity.User;
import com.business.business_management_api_v2.exception.ResourceNotFoundException;
import com.business.business_management_api_v2.mapper.UserMapper;
import com.business.business_management_api_v2.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    public UserResponse getById(Long id){
        User me = userRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("Không tìm thấy user có ID: " + id));
        return userMapper.toResponse(me);
    }
}
