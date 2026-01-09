package com.homi.saas.web.controller.company;

import com.homi.common.lib.response.ResponseResult;
import com.homi.model.common.vo.IdNameVO;
import com.homi.service.service.company.CompanyService;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/company")
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



