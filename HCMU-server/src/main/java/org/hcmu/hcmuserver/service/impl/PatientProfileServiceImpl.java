package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;

import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PatientProfileDTO;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.PatientProfile;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.patientprofile.PatientProfileMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.PatientProfileService;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PatientProfileServiceImpl extends ServiceImpl<PatientProfileMapper, PatientProfile> implements PatientProfileService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public Result<PatientProfileDTO.PatientProfileListDTO> createPatientProfile(PatientProfileDTO.PatientProfileCreateDTO createDTO) {
        // 校验用户是否存在
        User user = userService.getById(createDTO.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验用户是否已关联患者档案
        LambdaQueryWrapper<PatientProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PatientProfile::getUserId, createDTO.getUserId())
                .eq(PatientProfile::getIsDeleted, 0);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            return Result.error("该用户已关联患者档案");
        }

        PatientProfile patientProfile = new PatientProfile();
        BeanUtils.copyProperties(createDTO, patientProfile);
        patientProfile.setCreateTime(LocalDateTime.now());
        patientProfile.setUpdateTime(LocalDateTime.now());
        patientProfile.setIsDeleted(0);

        baseMapper.insert(patientProfile);
        log.info("创建患者档案成功: {}", patientProfile.getPatientProfileId());

        PatientProfileDTO.PatientProfileListDTO resultDTO = new PatientProfileDTO.PatientProfileListDTO();
        BeanUtils.copyProperties(patientProfile, resultDTO);
        resultDTO.setUserName(user.getUserName());

        return Result.success(resultDTO);
    }

    @Override
    public Result<PageDTO<PatientProfileDTO.PatientProfileListDTO>> getPatientProfiles(PatientProfileDTO.PatientProfileGetRequestDTO requestDTO) {
        MPJLambdaWrapper<PatientProfile> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(PatientProfile::getPatientProfileId,
                        PatientProfile::getUserId,
                        PatientProfile::getIdentityType,
                        PatientProfile::getStudentTeacher,
                        PatientProfile::getEmergencyContact,
                        PatientProfile::getCreateTime)
                .leftJoin(User.class, User::getUserId, PatientProfile::getUserId)
                .selectAs(User::getUserName, "userName")
                .eq(requestDTO.getIdentityType() != null, PatientProfile::getIdentityType, requestDTO.getIdentityType())
                .eq(requestDTO.getIsDeleted() != null, PatientProfile::getIsDeleted, requestDTO.getIsDeleted())
                .orderByDesc(PatientProfile::getCreateTime);

        if (requestDTO.getIsDeleted() == null) {
            queryWrapper.eq(PatientProfile::getIsDeleted, 0); // 默认查询未删除
        }

        IPage<PatientProfileDTO.PatientProfileListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                PatientProfileDTO.PatientProfileListDTO.class,
                queryWrapper
        );

        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<PatientProfileDTO.PatientProfileDetailDTO> getPatientProfileByUserId(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 查询患者档案详情
        MPJLambdaWrapper<PatientProfile> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(PatientProfile::getPatientProfileId,
                        PatientProfile::getUserId,
                        PatientProfile::getIdentityType,
                        PatientProfile::getStudentTeacher,
                        PatientProfile::getEmergencyContact,
                        PatientProfile::getEmergencyContactPhone,
                        PatientProfile::getMedicalHistory,
                        PatientProfile::getAllergyHistory,
                        PatientProfile::getCreateTime,
                        PatientProfile::getUpdateTime)
                .leftJoin(User.class, User::getUserId, PatientProfile::getUserId)
                .selectAs(User::getUserName, "userName")
                .eq(PatientProfile::getUserId, userId)
                .eq(PatientProfile::getIsDeleted, 0);

        PatientProfileDTO.PatientProfileDetailDTO detailDTO = baseMapper.selectJoinOne(PatientProfileDTO.PatientProfileDetailDTO.class, queryWrapper);

        if (detailDTO == null) {
            // 创建默认档案
            PatientProfile patientProfile = new PatientProfile();
            patientProfile.setUserId(userId);
            patientProfile.setIdentityType(0); // 默认身份类型
            patientProfile.setStudentTeacher("暂无");
            patientProfile.setEmergencyContact("暂无");
            patientProfile.setEmergencyContactPhone("暂无");
            patientProfile.setMedicalHistory("暂无");
            patientProfile.setAllergyHistory("暂无");
            baseMapper.insert(patientProfile);

            detailDTO = new PatientProfileDTO.PatientProfileDetailDTO();
            BeanUtils.copyProperties(patientProfile, detailDTO);
            detailDTO.setUserName(user.getUserName());

            return Result.success("患者档案已自动创建，请完善相关信息", detailDTO);
        }

        return Result.success(detailDTO);
    }

    @Override
    public Result<String> updatePatientProfileByUserId(Long userId, PatientProfileDTO.PatientProfileUpdateDTO updateDTO) {
        // 校验用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 查询患者档案
        LambdaQueryWrapper<PatientProfile> patientProfileWrapper = new LambdaQueryWrapper<>();
        patientProfileWrapper.eq(PatientProfile::getUserId, userId)
                .eq(PatientProfile::getIsDeleted, 0);

        PatientProfile patientProfile = baseMapper.selectOne(patientProfileWrapper);

        // 没有则创建默认档案
        if (patientProfile == null) {
            patientProfile = new PatientProfile();
            patientProfile.setUserId(userId);
            patientProfile.setIdentityType(0);
            patientProfile.setStudentTeacher("");
            patientProfile.setEmergencyContact("暂无");
            patientProfile.setEmergencyContactPhone("暂无");
            patientProfile.setMedicalHistory("暂无");
            patientProfile.setAllergyHistory("暂无");
            baseMapper.insert(patientProfile);
            log.info("为用户{}创建默认患者档案: {}", userId, patientProfile.getPatientProfileId());
        }

        updateDTO.updatePatientProfile(patientProfile);

        baseMapper.updateById(patientProfile);
        log.info("更新患者档案成功: {}", patientProfile.getPatientProfileId());
        return Result.success("更新成功");
    }

    @Override
    public Result<String> updateSelfPatientProfile(PatientProfileDTO.PatientProfileUpdateSelfDTO updateDTO) {
        // 获取当前用户
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();

        // 检查用户角色是否为患者
        MPJLambdaWrapper<UserRole> roleQueryWrapper = new MPJLambdaWrapper<>();
        roleQueryWrapper.select(Role::getType)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getIsDeleted, 0)
                .eq(Role::getIsDeleted, 0);

        Role userRole = userRoleMapper.selectJoinOne(Role.class, roleQueryWrapper);
        if (userRole == null) {
            return Result.error("用户未分配角色");
        }

        if (!RoleTypeEnum.PATIENT.getCode().equals(userRole.getType())) {
            return Result.error("用户不是患者角色，无法修改患者档案");
        }

        // 查询患者档案
        LambdaQueryWrapper<PatientProfile> patientProfileWrapper = new LambdaQueryWrapper<>();
        patientProfileWrapper.eq(PatientProfile::getUserId, userId)
                .eq(PatientProfile::getIsDeleted, 0);

        PatientProfile patientProfile = baseMapper.selectOne(patientProfileWrapper);

        // 没有则创建默认档案
        if (patientProfile == null) {
            patientProfile = new PatientProfile();
            patientProfile.setUserId(userId);
            patientProfile.setIdentityType(0);
            patientProfile.setStudentTeacher("暂无");
            patientProfile.setEmergencyContact("暂无");
            patientProfile.setEmergencyContactPhone("暂无");
            patientProfile.setMedicalHistory("暂无");
            patientProfile.setAllergyHistory("暂无");
            baseMapper.insert(patientProfile);
            log.info("为用户{}创建默认患者档案: {}", userId, patientProfile.getPatientProfileId());
        }

        updateDTO.updatePatientProfile(patientProfile);

        baseMapper.updateById(patientProfile);
        log.info("患者{}更新自己的档案成功: {}", userId, patientProfile.getPatientProfileId());
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deletePatientProfile(Long patientProfileId) {
        // 校验患者档案是否存在
        PatientProfile patientProfile = baseMapper.selectById(patientProfileId);
        if (patientProfile == null) {
            return Result.error("患者档案不存在");
        }

        boolean deleted = baseMapper.deleteById(patientProfileId) > 0;
        if (deleted) {
            log.info("删除患者档案成功: {}", patientProfileId);
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @Override
    public Result<List<PatientProfileDTO.PatientProfileDetailDTO>> getAllPatients() {
        MPJLambdaWrapper<PatientProfile> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(PatientProfile::getPatientProfileId,
                        PatientProfile::getUserId,
                        PatientProfile::getIdentityType,
                        PatientProfile::getStudentTeacher,
                        PatientProfile::getEmergencyContact,
                        PatientProfile::getEmergencyContactPhone,
                        PatientProfile::getMedicalHistory,
                        PatientProfile::getAllergyHistory,
                        PatientProfile::getCreateTime,
                        PatientProfile::getUpdateTime)
                .leftJoin(User.class, User::getUserId, PatientProfile::getUserId)
                .selectAs(User::getUserName, "userName")
                .eq(PatientProfile::getIsDeleted, 0)
                .orderByDesc(PatientProfile::getCreateTime);

        List<PatientProfileDTO.PatientProfileDetailDTO> patientProfiles = baseMapper.selectJoinList(PatientProfileDTO.PatientProfileDetailDTO.class, queryWrapper);

        return Result.success(patientProfiles);
    }

    @Override
    public Result<String> batchDeletePatientProfiles(List<Long> patientProfileIds) {
        if (patientProfileIds.isEmpty()) {
            return Result.error("请选择要删除的患者档案");
        }

        int deletedCount = baseMapper.deleteBatchIds(patientProfileIds);
        if (deletedCount > 0) {
            log.info("批量删除患者档案成功，共{}条", deletedCount);
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }
}