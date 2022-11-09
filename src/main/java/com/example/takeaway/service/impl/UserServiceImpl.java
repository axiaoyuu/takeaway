package com.example.takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.takeaway.mapper.UserMapper;
import com.example.takeaway.service.UserService;
import org.springframework.stereotype.Service;
import com.example.takeaway.entity.User;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
