package com.homi.saas.web.controller.delivery;

import com.homi.common.lib.response.ResponseResult;
import com.homi.model.delivery.dto.DeliveryCreateDTO;
import com.homi.model.delivery.dto.DeliveryQueryDTO;
import com.homi.model.delivery.dto.DeliveryUpdateDTO;
import com.homi.model.delivery.vo.DeliveryVO;
import com.homi.saas.web.auth.vo.login.UserLoginVO;
import com.homi.saas.web.config.LoginManager;
import com.homi.service.service.delivery.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用于 domix
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/1/22
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/saas/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping("/create")
    @Operation(summary = "创建交割单")
    public ResponseResult<DeliveryVO> create(@RequestBody @Valid DeliveryCreateDTO dto) {
        UserLoginVO loginManager = LoginManager.getCurrentUser();
        dto.setCreateBy(loginManager.getId());

        return ResponseResult.ok(deliveryService.create(dto));
    }

    @PostMapping("/update")
    @Operation(summary = "更新交割单")
    public ResponseResult<DeliveryVO> update(@RequestBody @Valid DeliveryUpdateDTO dto) {
        return ResponseResult.ok(deliveryService.update(dto));
    }

    @PostMapping("/detail")
    @Operation(summary = "获取交割单详情")
    public ResponseResult<DeliveryVO> getDetail(@RequestBody DeliveryQueryDTO query) {
        return ResponseResult.ok(deliveryService.getDetail(query.getId()));
    }

    @PostMapping("/list")
    @Operation(summary = "查询交割单列表")
    public ResponseResult<List<DeliveryVO>> list(@RequestBody DeliveryQueryDTO query) {
        return ResponseResult.ok(deliveryService.list(query));
    }

    @PostMapping("/sign")
    @Operation(summary = "签署交割单")
    public ResponseResult<Void> sign(@RequestBody DeliveryQueryDTO dto) {
        deliveryService.sign(dto.getId());
        return ResponseResult.ok();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除交割单")
    public ResponseResult<Void> delete(@RequestBody DeliveryQueryDTO id) {
        deliveryService.deleteById(id.getId());
        return ResponseResult.ok();
    }

    @PostMapping("/export")
    @Operation(summary = "导出PDF")
    public void exportPdf(@RequestBody DeliveryQueryDTO dto, HttpServletResponse response) {
        deliveryService.exportPdf(dto.getId(), response);
    }
}
