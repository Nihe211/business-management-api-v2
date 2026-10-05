package com.business.business_management_api_v2.mapper;

import com.business.business_management_api_v2.dto.response.UserResponse;
import com.business.business_management_api_v2.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
