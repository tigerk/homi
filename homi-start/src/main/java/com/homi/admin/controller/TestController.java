package com.homi.admin.controller;

import com.homi.model.entity.SysUser;
import com.homi.model.repo.SysUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 应用于 homi-boot
 *
 * @author 金华云 E-mail:jinhuayun001@ke.com
 * @version v1.0
 * {@code @date} 2025/4/26
 */


@Slf4j
@RequestMapping("/admin/test")
@RestController
@RequiredArgsConstructor
public class TestController {
    private final SysUserRepo sysUserRepo;

    /**
     * 用户列表
     *
     * @return 所有数据
     */
    @GetMapping("/user")
    public List<SysUser> list() {
        return sysUserRepo.list();
    }

}
