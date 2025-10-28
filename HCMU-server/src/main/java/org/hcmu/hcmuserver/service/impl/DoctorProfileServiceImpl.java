package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorProfileDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.mapper.doctorprofile.DoctorProfileMapper;
import org.hcmu.hcmuserver.service.DepartmentService;
import org.hcmu.hcmuserver.service.DoctorProfileService;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DoctorProfileServiceImpl extends ServiceImpl<DoctorProfileMapper, DoctorProfile> implements DoctorProfileService {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public Result<DoctorProfileDTO.DoctorProfileListDTO> createDoctorProfile(DoctorProfileDTO.DoctorProfileCreateDTO createDTO) {
        // 校验用户是否存在
        User user = userService.getById(createDTO.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验科室是否存在
        Department department = departmentService.getById(createDTO.getDepartmentId());
        if (department == null) {
            return Result.error("科室不存在");
        }

        // 校验用户是否已关联医生档案
        LambdaQueryWrapper<DoctorProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DoctorProfile::getUserId, createDTO.getUserId())
                .eq(DoctorProfile::getIsDeleted, 0);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            return Result.error("该用户已关联医生档案");
        }

        DoctorProfile doctorProfile = new DoctorProfile();
        BeanUtils.copyProperties(createDTO, doctorProfile);
        doctorProfile.setCreateTime(LocalDateTime.now());
        doctorProfile.setUpdateTime(LocalDateTime.now());
        doctorProfile.setIsDeleted(0);

        baseMapper.insert(doctorProfile);
        log.info("创建医生档案成功: {}", doctorProfile.getDoctorProfileId());

        DoctorProfileDTO.DoctorProfileListDTO resultDTO = new DoctorProfileDTO.DoctorProfileListDTO();
        BeanUtils.copyProperties(doctorProfile, resultDTO);
        resultDTO.setUserName(user.getUserName());
        resultDTO.setDepartmentName(department.getName());

        return Result.success(resultDTO);
    }

    @Override
    public Result<PageDTO<DoctorProfileDTO.DoctorProfileListDTO>> getDoctorProfiles(DoctorProfileDTO.DoctorProfileGetRequestDTO requestDTO) {
        MPJLambdaWrapper<DoctorProfile> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(DoctorProfile::getDoctorProfileId,
                        DoctorProfile::getUserId,
                        DoctorProfile::getDepartmentId,
                        DoctorProfile::getTitle,
                        DoctorProfile::getSpecialty,
                        DoctorProfile::getCreateTime)
                .leftJoin(User.class, User::getUserId, DoctorProfile::getUserId)
                .selectAs(User::getUserName, "userName")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(requestDTO.getDepartmentId() != null, DoctorProfile::getDepartmentId, requestDTO.getDepartmentId())
                .like(requestDTO.getTitle() != null && !requestDTO.getTitle().isEmpty(), DoctorProfile::getTitle, requestDTO.getTitle())
                .eq(requestDTO.getIsDeleted() != null, DoctorProfile::getIsDeleted, requestDTO.getIsDeleted())
                .orderByDesc(DoctorProfile::getCreateTime);

        if (requestDTO.getIsDeleted() == null) {
            queryWrapper.eq(DoctorProfile::getIsDeleted, 0); // 默认查询未删除
        }

        IPage<DoctorProfileDTO.DoctorProfileListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                DoctorProfileDTO.DoctorProfileListDTO.class,
                queryWrapper
        );

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<DoctorProfileDTO.DoctorProfileDetailDTO> getDoctorProfileById(Long doctorProfileId) {
        MPJLambdaWrapper<DoctorProfile> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(DoctorProfile::getDoctorProfileId,
                        DoctorProfile::getUserId,
                        DoctorProfile::getDepartmentId,
                        DoctorProfile::getTitle,
                        DoctorProfile::getSpecialty,
                        DoctorProfile::getBio,
                        DoctorProfile::getCreateTime,
                        DoctorProfile::getUpdateTime)
                .leftJoin(User.class, User::getUserId, DoctorProfile::getUserId)
                .selectAs(User::getUserName, "userName")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(DoctorProfile::getDoctorProfileId, doctorProfileId)
                .eq(DoctorProfile::getIsDeleted, 0);

        DoctorProfileDTO.DoctorProfileDetailDTO detailDTO = baseMapper.selectJoinOne(DoctorProfileDTO.DoctorProfileDetailDTO.class, queryWrapper);
        if (detailDTO == null) {
            return Result.error("医生档案不存在");
        }

        return Result.success(detailDTO);
    }

    @Override
    public Result<String> updateDoctorProfile(Long doctorProfileId, DoctorProfileDTO.DoctorProfileUpdateDTO updateDTO) {
        // 校验医生档案是否存在
        LambdaQueryWrapper<DoctorProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DoctorProfile::getDoctorProfileId, doctorProfileId)
                .eq(DoctorProfile::getIsDeleted, 0);
        DoctorProfile doctorProfile = baseMapper.selectOne(queryWrapper);
        if (doctorProfile == null) {
            return Result.error("医生档案不存在");
        }

        // 校验科室是否存在（如果更新科室）
        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentService.getById(updateDTO.getDepartmentId());
            if (department == null) {
                return Result.error("科室不存在");
            }
        }

        // 使用DTO的updateDoctorProfile方法更新字段
        updateDTO.updateDoctorProfile(doctorProfile);

        baseMapper.updateById(doctorProfile);
        log.info("更新医生档案成功: {}", doctorProfileId);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteDoctorProfile(Long doctorProfileId) {
        // 校验医生档案是否存在
        LambdaQueryWrapper<DoctorProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DoctorProfile::getDoctorProfileId, doctorProfileId)
                .eq(DoctorProfile::getIsDeleted, 0);
        DoctorProfile doctorProfile = baseMapper.selectOne(queryWrapper);
        if (doctorProfile == null) {
            return Result.error("医生档案不存在");
        }

        // 逻辑删除
        doctorProfile.setIsDeleted(1);
        doctorProfile.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(doctorProfile);
        log.info("删除医生档案成功: {}", doctorProfileId);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> batchDeleteDoctorProfiles(List<Long> doctorProfileIds) {
        if (doctorProfileIds.isEmpty()) {
            return Result.error("请选择要删除的医生档案");
        }

        // 校验所有档案是否存在且未删除
        LambdaQueryWrapper<DoctorProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DoctorProfile::getDoctorProfileId, doctorProfileIds)
                .eq(DoctorProfile::getIsDeleted, 0);
        long existCount = baseMapper.selectCount(queryWrapper);
        if (existCount != doctorProfileIds.size()) {
            return Result.error("部分医生档案不存在或已删除");
        }

        // 批量逻辑删除
        DoctorProfile update = new DoctorProfile();
        update.setIsDeleted(1);
        update.setUpdateTime(LocalDateTime.now());

        LambdaUpdateWrapper<DoctorProfile> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DoctorProfile::getDoctorProfileId, doctorProfileIds);

        baseMapper.update(update, updateWrapper);
        log.info("批量删除医生档案成功，共{}条", doctorProfileIds.size());
        return Result.success("批量删除成功");
    }
}