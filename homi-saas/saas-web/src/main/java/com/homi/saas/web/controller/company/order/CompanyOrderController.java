package com.homi.saas.web.controller.company.order;

import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.company.dto.order.CompanyConsumePageDTO;
import com.homi.model.company.dto.order.CompanyOrderCreateDTO;
import com.homi.model.company.dto.order.CompanyOrderPageDTO;
import com.homi.model.company.vo.order.CompanyConsumeRecordVO;
import com.homi.model.company.vo.order.CompanyOrderRecordVO;
import com.homi.model.company.vo.order.CompanyProductOrderVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.company.CompanyOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/company/order")
public class CompanyOrderController {

    private final CompanyOrderService companyOrderService;

    @PostMapping("/product/list")
    public ResponseResult<List<CompanyProductOrderVO>> getProductList() {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(companyOrderService.getProductList(currentUser.getCurCompanyId()));
    }

    @PostMapping("/record/page")
    public ResponseResult<PageVO<CompanyOrderRecordVO>> getOrderPage(@RequestBody CompanyOrderPageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(companyOrderService.getOrderPage(currentUser.getCurCompanyId(), dto));
    }

    @PostMapping("/consume/page")
    public ResponseResult<PageVO<CompanyConsumeRecordVO>> getConsumePage(@RequestBody CompanyConsumePageDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(companyOrderService.getConsumePage(currentUser.getCurCompanyId(), dto));
    }

    @PostMapping("/create")
    public ResponseResult<Boolean> createOrder(@RequestBody CompanyOrderCreateDTO dto) {
        UserLoginVO currentUser = LoginManager.getCurrentUser();
        return ResponseResult.ok(companyOrderService.createOrder(currentUser.getCurCompanyId(), dto));
    }
}
