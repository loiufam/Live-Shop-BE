package com.lyh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyh.common.context.UserContext;
import com.lyh.common.exception.BusinessException;
import com.lyh.redis.constants.CommonRedisKey;
import com.lyh.user.entity.User;
import com.lyh.user.exception.UserCodeMsg;
import com.lyh.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import com.lyh.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.lyh.user.service.UserService;
import com.lyh.common.utils.JwtUtils;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(String username, String password) {
        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException(UserCodeMsg.USERNAME_OR_PASSWORD_EMPTY);
        }

        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (exist != null) {
            throw new BusinessException(UserCodeMsg.USERNAME_EXIST);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setBalance(BigDecimal.ZERO);

        userMapper.insert(user);
    }

    @Override
    public String login(String username, String password) {
        // 参数校验
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException(UserCodeMsg.USERNAME_OR_PASSWORD_EMPTY);
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(UserCodeMsg.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(UserCodeMsg.PASSWORD_ERROR);
        }

        String token = JwtUtils.generateToken(user.getId(), user.getUsername());

        String redisKey = CommonRedisKey.commonRedisKey.USER_TOKEN.getRealKey(token);
        long expireTime = CommonRedisKey.commonRedisKey.USER_TOKEN.getExpireTime();
        TimeUnit unit = CommonRedisKey.commonRedisKey.USER_TOKEN.getUnit();

        // 存入 Redis，存用户ID
        redisTemplate.opsForValue().set(redisKey, String.valueOf(user.getId()), expireTime, unit);

        return token;
    }
    
    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserCodeMsg.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }
    
    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(UserCodeMsg.USER_NOT_FOUND);
        }
        return getUserById(userId);
    }
    
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBalance(user.getBalance());
        return vo;
    }
}
