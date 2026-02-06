package com.homi.service.service.contract;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.homi.common.lib.enums.contract.ContractTemplateStatusEnum;
import com.homi.common.lib.utils.BeanCopyUtils;
import com.homi.common.lib.utils.ConvertHtml2PdfUtils;
import com.homi.common.lib.vo.PageVO;
import com.homi.model.contract.dto.ContractTemplateCreateDTO;
import com.homi.model.contract.dto.ContractTemplateDeleteDTO;
import com.homi.model.contract.dto.ContractTemplateQueryDTO;
import com.homi.model.contract.dto.ContractTemplateStatusDTO;
import com.homi.model.contract.vo.ContractTemplateListVO;
import com.homi.model.contract.vo.LeaseContractVO;
import com.homi.model.dao.entity.CompanyUser;
import com.homi.model.dao.entity.ContractTemplate;
import com.homi.model.dao.repo.CompanyUserRepo;
import com.homi.model.dao.repo.ContractTemplateRepo;
import com.homi.model.house.dto.HouseLayoutDTO;
import com.homi.model.room.dto.price.OtherFeeDTO;
import com.homi.model.room.vo.RoomListVO;
import com.homi.model.tenant.vo.LeaseDetailVO;
import com.homi.model.tenant.vo.TenantMateVO;
import com.homi.model.tenant.vo.TenantPersonalVO;
import com.homi.model.tenant.vo.bill.LeaseBillListVO;
import com.homi.service.service.tenant.LeaseContractService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 应用于 domix-saas
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/11/12
 */

@Service
@RequiredArgsConstructor
public class ContractTemplateService {
    private final CompanyUserRepo companyUserRepo;
    private final ContractTemplateRepo contractTemplateRepo;
    private final LeaseContractService leaseContractService;

    public PageVO<ContractTemplateListVO> getContractTemplateList(ContractTemplateQueryDTO query) {

        LambdaQueryWrapper<ContractTemplate> wrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(query.getContractType())) {
            wrapper.eq(ContractTemplate::getContractType, query.getContractType());
        }

        if (CharSequenceUtil.isNotBlank(query.getTemplateName())) {
            wrapper.like(ContractTemplate::getTemplateName, query.getTemplateName());
        }
        if (Objects.nonNull(query.getStatus())) {
            wrapper.eq(ContractTemplate::getStatus, query.getStatus());
        }

        wrapper.like(ContractTemplate::getTemplateName, query.getTemplateName());
        wrapper.orderByDesc(ContractTemplate::getId);

        Page<ContractTemplate> page = new Page<>(query.getCurrentPage(), query.getPageSize());

        Page<ContractTemplate> dictDataPage = contractTemplateRepo.page(page, wrapper);

        PageVO<ContractTemplateListVO> pageVO = new PageVO<>();
        pageVO.setTotal(dictDataPage.getTotal());
        pageVO.setList(dictDataPage.getRecords().stream().map(c -> {
            ContractTemplateListVO contractTemplateListVO = BeanCopyUtils.copyBean(c, ContractTemplateListVO.class);
            if (Objects.nonNull(c.getDeptIds())) {
                assert contractTemplateListVO != null;
                contractTemplateListVO.setDeptIds(JSONUtil.toList(c.getDeptIds(), String.class));
            }
            return contractTemplateListVO;
        }).toList());
        pageVO.setCurrentPage(dictDataPage.getCurrent());
        pageVO.setPageSize(dictDataPage.getSize());
        pageVO.setPages(dictDataPage.getPages());


        return pageVO;
    }

    /**
     * 创建合同模板
     *
     * @param createDTO 合同模板创建DTO
     * @return 是否创建成功
     */
    public Long createContractTemplate(ContractTemplateCreateDTO createDTO) {
        ContractTemplate contractTemplate = BeanCopyUtils.copyBean(createDTO, ContractTemplate.class);

        assert contractTemplate != null;

        contractTemplate.setDeptIds(JSONUtil.toJsonStr(createDTO.getDeptIds()));
        contractTemplate.setStatus(ContractTemplateStatusEnum.UNEFFECTIVE.getCode());

        contractTemplateRepo.save(contractTemplate);

        return contractTemplate.getId();
    }

    /**
     * 更新合同模板
     *
     * @param createDTO 合同模板创建DTO
     * @return 是否更新成功
     */
    public Long updateContractTemplate(ContractTemplateCreateDTO createDTO) {
        ContractTemplate contractTemplate = BeanCopyUtils.copyBean(createDTO, ContractTemplate.class);

        assert contractTemplate != null;
        contractTemplate.setDeptIds(JSONUtil.toJsonStr(createDTO.getDeptIds()));
        contractTemplateRepo.updateById(contractTemplate);

        return contractTemplate.getId();
    }

    /**
     * 合同模板预览功能
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/11/12 17:33
     *
     * @param query 参数说明
     * @return java.lang.String
     */
    public byte[] previewContractTemplate(ContractTemplateQueryDTO query) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(query.getId());

        assert contractTemplate != null;

        // 创建完整的租户详情模拟数据
        LeaseDetailVO tenantDetailVO = new LeaseDetailVO();

        // 基本信息
        tenantDetailVO.setLeaseId(1L);
        tenantDetailVO.setTenantId(1L);
        tenantDetailVO.setContractNature(1); // 1=新签
        tenantDetailVO.setCompanyId(1001L);
        tenantDetailVO.setDeptId(2001L);
        tenantDetailVO.setDeptName("租赁一部");
        tenantDetailVO.setRoomIds("[1001,1002]");
        tenantDetailVO.setTenantType(0); // 0=个人
        tenantDetailVO.setTenantTypeId(1L);

        // 租客个人信息
        TenantPersonalVO tenantPersonal = new TenantPersonalVO();
        tenantPersonal.setId(1L);
        tenantPersonal.setName("张三");
        tenantPersonal.setGender(1); // 1=男
        tenantPersonal.setIdType(0); // 0=身份证
        tenantPersonal.setIdNo("110101199001011234");
        tenantPersonal.setPhone("13800000000");
        tenantPersonal.setTags(List.of("学生", "长租"));
        tenantPersonal.setRemark("优质租客");
        tenantDetailVO.setTenantPersonal(tenantPersonal);

        // 租客名称和电话（冗余字段）
        tenantDetailVO.setTenantName("张三");
        tenantDetailVO.setTenantPhone("13800000000");

        // 房间列表
        List<RoomListVO> roomList = new ArrayList<>();
        RoomListVO room1 = new RoomListVO();
        room1.setRoomId(1001L);
        room1.setHouseId(2001L);
        room1.setCommunityId(3001L);
        room1.setCommunityName("幸福小区");
        room1.setHouseCode("XF202501");
        room1.setHouseName("幸福小区1号楼");
        room1.setDoorNumber("101");
        room1.setArea(new BigDecimal(120));
        room1.setPropertyFee(new BigDecimal(200));
        room1.setLeaseMode(2); // 2=整/合租
        room1.setRentalType(1); // 1=整租
        room1.setRoomNumber("101");
        room1.setBuilding("1号楼");
        room1.setUnit("1单元");
        room1.setFloor(1);

        // 房型信息
        HouseLayoutDTO houseLayout = new HouseLayoutDTO();
        houseLayout.setId(4001L);
        houseLayout.setLayoutName("两室一厅");
        houseLayout.setLivingRoom(1);
        houseLayout.setBathroom(1);
        houseLayout.setKitchen(1);
        houseLayout.setBedroom(2);
        room1.setHouseLayout(houseLayout);

        roomList.add(room1);
        tenantDetailVO.setRoomList(roomList);

        // 租赁信息
        tenantDetailVO.setRentPrice(new BigDecimal(5000));
        tenantDetailVO.setDepositMonths(2);
        tenantDetailVO.setPaymentMonths(3);

        DateTime start = DateUtil.date();
        DateTime end = DateUtil.offsetDay(start, 365);

        tenantDetailVO.setLeaseStart(start);
        tenantDetailVO.setLeaseEnd(end);
        tenantDetailVO.setCheckInTime(start);
        tenantDetailVO.setCheckOutTime(end);
        tenantDetailVO.setLeaseDurationDays(365);
        tenantDetailVO.setRentDueType(2); // 2=固定
        tenantDetailVO.setRentDueDay(15);
        tenantDetailVO.setRentDueOffsetDays(0);

        // 业务人员信息
        tenantDetailVO.setSalesmanId(3001L);
        tenantDetailVO.setSalesmanName("李四");
        tenantDetailVO.setHelperId(3002L);

        // 状态信息
        tenantDetailVO.setSignStatus(0); // 0=待签字
        tenantDetailVO.setCheckOutStatus(0); // 0=未退租
        tenantDetailVO.setStatus(1); // 1=生效中

        // 来源和渠道
        tenantDetailVO.setTenantSource(1L);
        tenantDetailVO.setTenantSourceName("线下推荐");
        tenantDetailVO.setDealChannel(1L);
        tenantDetailVO.setDealChannelName("门店");

        // 备注
        tenantDetailVO.setRemark("无特殊要求");

        // 合同信息
        LeaseContractVO leaseContract = new LeaseContractVO();
        leaseContract.setId(5001L);
        leaseContract.setLeaseId(1L);
        leaseContract.setContractCode("HT20250101001");
        leaseContract.setContractTemplateId(query.getId());
        leaseContract.setContractTemplateName(contractTemplate.getTemplateName());
        leaseContract.setContractContent(contractTemplate.getTemplateContent());
        leaseContract.setSignStatus(0);
        leaseContract.setRemark("合同预览");
        tenantDetailVO.setLeaseContract(leaseContract);

        // 其他费用
        List<OtherFeeDTO> otherFees = new ArrayList<>();
        OtherFeeDTO fee1 = new OtherFeeDTO();
        fee1.setDictDataId(1L);
        fee1.setName("物业费");
        fee1.setPaymentMethod(1); // 随房租付
        fee1.setPriceMethod(1); // 固定金额
        fee1.setPriceInput(new BigDecimal(200));
        otherFees.add(fee1);

        OtherFeeDTO fee2 = new OtherFeeDTO();
        fee2.setDictDataId(2L);
        fee2.setName("服务费");
        fee2.setPaymentMethod(1); // 随房租付
        fee2.setPriceMethod(2); // 按比例
        fee2.setPriceInput(new BigDecimal(5));
        otherFees.add(fee2);
        tenantDetailVO.setOtherFees(otherFees);

        // 账单列表
        List<LeaseBillListVO> leaseBillList = new ArrayList<>();
        LeaseBillListVO bill1 = new LeaseBillListVO();
        bill1.setId(6001L);
        bill1.setTenantId(1L);
        bill1.setSortOrder(1);
        bill1.setBillType(1); // 1=租金
        bill1.setRentalAmount(new BigDecimal(15000));
        bill1.setDepositAmount(new BigDecimal(10000));
        bill1.setOtherFeeAmount(new BigDecimal(850));
        bill1.setTotalAmount(new BigDecimal(25850));
        bill1.setDueDate(new Date());
        bill1.setPayStatus(0); // 0=未支付
        leaseBillList.add(bill1);
        tenantDetailVO.setLeaseBillList(leaseBillList);

        // 同住人列表
        List<TenantMateVO> tenantMateList = new ArrayList<>();
        TenantMateVO mate1 = new TenantMateVO();
        mate1.setId(1);
        mate1.setTenantId(1);
        mate1.setName("李四");
        mate1.setGender(2); // 2=女
        mate1.setIdType(0); // 0=身份证
        mate1.setIdNo("110101199001011235");
        mate1.setPhone("13900000000");
        mate1.setTags(List.of("家属"));
        tenantMateList.add(mate1);
        tenantDetailVO.setTenantMateList(tenantMateList);

        // 创建时间和修改时间
        tenantDetailVO.setCreateTime(new Date());
        tenantDetailVO.setUpdateTime(new Date());

        String renderedContent = leaseContractService.replaceContractVariables(contractTemplate.getTemplateContent(), tenantDetailVO);

        return ConvertHtml2PdfUtils.generatePdf(renderedContent);
    }

    public Boolean updateContractTemplateStatus(ContractTemplateStatusDTO updateDTO) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(updateDTO.getId());
        assert contractTemplate != null;
        contractTemplate.setStatus(updateDTO.getStatus());

        contractTemplateRepo.updateById(contractTemplate);

        return true;
    }

    public Boolean deleteContractTemplate(ContractTemplateDeleteDTO deleteDTO) {
        ContractTemplate contractTemplate = contractTemplateRepo.getById(deleteDTO.getId());
        assert contractTemplate != null;

        contractTemplateRepo.removeById(contractTemplate);

        return true;
    }

    /**
     * 获取用户可用的合同模板列表
     *
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/16 23:22
     *
     * @param companyId     公司ID
     * @param currentUserId 用户 ID
     * @param contractType  合同类型
     * @return java.util.List<com.homi.model.contract.vo.ContractTemplateListVO> 合同列表
     */
    public List<ContractTemplateListVO> getMyAvailableContractTemplates(Long companyId, Long currentUserId, Integer contractType) {
        CompanyUser companyUser = companyUserRepo.getCompanyUser(companyId, currentUserId);
        Long deptId = companyUser.getDeptId();
        if (Objects.isNull(deptId)) {
            return Collections.emptyList();
        }
        List<ContractTemplate> contractTemplateList = contractTemplateRepo.getContractTemplateList(deptId, contractType);

        return formatContractTemplateListVO(contractTemplateList);
    }

    public List<ContractTemplateListVO> getAllContractTemplateList(Long curCompanyId, Integer contractType) {
        List<ContractTemplate> contractTemplateList = contractTemplateRepo.getContractTemplateListByCompanyIdAndType(curCompanyId, contractType);

        return formatContractTemplateListVO(contractTemplateList);
    }

    /**
     * 格式化合同模板列表VO
     * <p>
     * {@code @author} tk
     * {@code @date} 2025/12/17 01:13
     *
     * @param contractTemplateList 参数说明
     * @return java.util.@org.jetbrains.annotations.NotNull List<com.homi.model.contract.vo.ContractTemplateListVO>
     */
    @NotNull
    private List<ContractTemplateListVO> formatContractTemplateListVO(List<ContractTemplate> contractTemplateList) {
        return contractTemplateList.stream().map(c -> {
            ContractTemplateListVO contractTemplateListVO = BeanCopyUtils.copyBean(c, ContractTemplateListVO.class);
            if (Objects.nonNull(c.getDeptIds())) {
                assert contractTemplateListVO != null;
                contractTemplateListVO.setDeptIds(JSONUtil.toList(c.getDeptIds(), String.class));
            }
            return contractTemplateListVO;
        }).toList();
    }
}
