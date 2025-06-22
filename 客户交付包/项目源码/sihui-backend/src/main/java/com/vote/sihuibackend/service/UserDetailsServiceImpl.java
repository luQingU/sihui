package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.repository.UserRepository;
import com.vote.sihuibackend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自定义用户详情服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("尝试加载用户: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", usernameOrEmail);
                    return new UsernameNotFoundException("用户不存在: " + usernameOrEmail);
                });

        log.debug("成功加载用户: {}, 状态: {}", user.getUsername(), user.getStatus());
        return UserPrincipal.create(user);
    }
}