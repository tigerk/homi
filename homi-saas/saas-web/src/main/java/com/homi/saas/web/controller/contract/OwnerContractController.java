package com.homi.saas.web.controller.contract;

import com.homi.common.lib.annotation.Log;
import com.homi.common.lib.enums.OperationTypeEnum;
import com.homi.common.lib.response.ResponseResult;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.owner.dto.OwnerCreateDTO;
import com.homi.model.owner.dto.OwnerContractIdDTO;
import com.homi.model.owner.dto.OwnerContractStatusDTO;
import com.homi.model.owner.dto.OwnerQueryDTO;
import com.homi.model.owner.dto.OwnerUpdateDTO;
import com.homi.model.owner.vo.OwnerDetailVO;
import com.homi.model.owner.vo.OwnerListVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.service.service.owner.OwnerContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/contract/owner")
public class OwnerContractController {
    private final OwnerContractService ownerContractService;

    @PostMapping("/create")
    @Log(title = "创建业主合同", operationType = OperationTypeEnum.INSERT)
    public ResponseResult<Long> create(@RequestBody OwnerCreateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setCreateBy(loginUser.getId());
        if (dto.getOwnerContract() != null) {
            dto.getOwnerContract().setCompanyId(loginUser.getCurCompanyId());
        }
        return ResponseResult.ok(ownerContractService.createOwnerContract(dto));
    }

    @PostMapping("/list")
    public ResponseResult<PageVO<OwnerListVO>> list(@RequestBody OwnerQueryDTO query) {
        return ResponseResult.ok(ownerContractService.getOwnerContractList(query));
    }

    @PostMapping("/detail")
    public ResponseResult<OwnerDetailVO> detail(@RequestBody OwnerContractIdDTO dto) {
        return ResponseResult.ok(ownerContractService.getOwnerContractDetail(dto));
    }

    @PostMapping("/update")
    @Log(title = "编辑业主合同", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> update(@RequestBody OwnerUpdateDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        dto.setUpdateBy(loginUser.getId());
        if (dto.getOwnerContract() != null) {
            dto.getOwnerContract().setCompanyId(loginUser.getCurCompanyId());
        }
        return ResponseResult.ok(ownerContractService.updateOwnerContract(dto));
    }

    @PostMapping("/updateStatus")
    @Log(title = "更新业主合同状态", operationType = OperationTypeEnum.UPDATE)
    public ResponseResult<Long> updateStatus(@RequestBody OwnerContractStatusDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractService.updateOwnerContractStatus(dto, loginUser.getId()));
    }

    @PostMapping("/delete")
    @Log(title = "删除业主合同", operationType = OperationTypeEnum.DELETE)
    public ResponseResult<Long> delete(@RequestBody OwnerContractIdDTO dto, @AuthenticationPrincipal UserLoginVO loginUser) {
        return ResponseResult.ok(ownerContractService.deleteOwnerContract(dto, loginUser.getId()));
    }
}
