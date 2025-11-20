package com.homi.admin.controller.company;


import com.homi.admin.auth.vo.login.UserLoginVO;
import com.homi.admin.config.LoginManager;
import com.homi.domain.base.ResponseResult;
import com.homi.domain.vo.IdNameVO;
import com.homi.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/company")
@RestController
@RequiredArgsConstructor
public class CompanyController {
    /**
     * 服务对象
     */
    private final CompanyService companyService;

    /**
     * 用户列表
     *
     * @return 所有数据
     */
    @GetMapping("/userOptions")
    public ResponseResult<List<IdNameVO>> userOptions() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(companyService.getUserOptions(currentUser.getCurCompanyId()));
    }
}

