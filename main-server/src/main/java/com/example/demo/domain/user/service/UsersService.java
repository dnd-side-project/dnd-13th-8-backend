package com.example.demo.domain.user.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;

    public Users findUserByShareCode(String shareCode) {
        return usersRepository.findUsersByShareCode(shareCode)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
