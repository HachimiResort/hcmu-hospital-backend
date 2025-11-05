package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.DoctorProfileDTO;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.entity.Department;
import org.hcmu.hcmupojo.entity.DoctorProfile;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmuserver.mapper.doctorprofile.DoctorProfileMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
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

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<DoctorProfileDTO.DoctorProfileListDTO> createDoctorProfile(DoctorProfileDTO.DoctorProfileCreateDTO createDTO) {
        // 校验用户是否存在
        User user = userService.getById(createDTO.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long userId = createDTO.getUserId();

        // 是否为医生角色呢
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

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色，无法创建医生档案");
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
                .selectAs(User::getName, "name")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(Role::getType, RoleTypeEnum.DOCTOR.getCode())
                .eq(requestDTO.getDepartmentId() != null, DoctorProfile::getDepartmentId, requestDTO.getDepartmentId())
                .like(requestDTO.getTitle() != null && !requestDTO.getTitle().isEmpty(), DoctorProfile::getTitle, requestDTO.getTitle())
                .eq(DoctorProfile::getIsDeleted, 0)
                .orderByDesc(DoctorProfile::getCreateTime);

        IPage<DoctorProfileDTO.DoctorProfileListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                DoctorProfileDTO.DoctorProfileListDTO.class,
                queryWrapper
        );

        return Result.success(new PageDTO<>(page));
    }

    /**
     * 根据用户id查询医生档案
     * @param userId
     * @return
     */
    @Override
    public Result<DoctorProfileDTO.DoctorProfileDetailDTO> getDoctorProfileByUserId(Long userId) {

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 是否为医生角色呢
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

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色，无法查看医生档案");
        }

        // 是否已有医生档案呢
        LambdaQueryWrapper<DoctorProfile> doctorProfileWrapper = new LambdaQueryWrapper<>();
        doctorProfileWrapper.eq(DoctorProfile::getUserId, userId)
                .eq(DoctorProfile::getIsDeleted, 0);
        
        DoctorProfile doctorProfile = baseMapper.selectOne(doctorProfileWrapper);
        boolean isNewProfile = false; // 标记是否是新创建的档案
        
        // 创建一个默认的
        if (doctorProfile == null) {
            isNewProfile = true;
            doctorProfile = new DoctorProfile();
            doctorProfile.setUserId(userId);
            doctorProfile.setDepartmentId(0L); // 默认department_id=0，表示没分配部门
            doctorProfile.setTitle("暂无");
            doctorProfile.setSpecialty("暂无");
            doctorProfile.setBio("暂无");
            baseMapper.insert(doctorProfile);
        }

        // 查询医生档案详情
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
                .selectAs(User::getName, "name")
                .leftJoin(Department.class, Department::getDepartmentId, DoctorProfile::getDepartmentId)
                .selectAs(Department::getName, "departmentName")
                .eq(DoctorProfile::getUserId, userId)
                .eq(DoctorProfile::getIsDeleted, 0);

        DoctorProfileDTO.DoctorProfileDetailDTO detailDTO = baseMapper.selectJoinOne(DoctorProfileDTO.DoctorProfileDetailDTO.class, queryWrapper);
        if (detailDTO == null) {
            return Result.error("医生档案查询失败");
        }

        // 新的档案
        if (isNewProfile) {
            return Result.success("医生档案已自动创建，请完善相关信息", detailDTO);
        }

        return Result.success(detailDTO);
    }

    @Override
    public Result<String> updateDoctorProfileByUserId(Long userId, DoctorProfileDTO.DoctorProfileUpdateDTO updateDTO) {
        // 校验用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验用户是否为医生角色
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

        if (!RoleTypeEnum.DOCTOR.getCode().equals(userRole.getType())) {
            return Result.error("用户不是医生角色，无法更新医生档案");
        }

        // 看看有没有医生档案
        LambdaQueryWrapper<DoctorProfile> doctorProfileWrapper = new LambdaQueryWrapper<>();
        doctorProfileWrapper.eq(DoctorProfile::getUserId, userId)
                .eq(DoctorProfile::getIsDeleted, 0);
        
        DoctorProfile doctorProfile = baseMapper.selectOne(doctorProfileWrapper);
        
        // 没有先创建默认档案
        if (doctorProfile == null) {
            doctorProfile = new DoctorProfile();
            doctorProfile.setUserId(userId);
            doctorProfile.setDepartmentId(0L); // 默认没分配部门
            doctorProfile.setTitle("暂无");
            doctorProfile.setSpecialty("暂无");
            doctorProfile.setBio("暂无");
            baseMapper.insert(doctorProfile);
            log.info("为用户{}创建默认医生档案: {}", userId, doctorProfile.getDoctorProfileId());
        }

        // 科室是否存在
        if (updateDTO.getDepartmentId() != null) {
            Department department = departmentService.getById(updateDTO.getDepartmentId());
            if (department == null) {
                return Result.error("科室不存在");
            }
        }


        updateDTO.updateDoctorProfile(doctorProfile);

        baseMapper.updateById(doctorProfile);
        log.info("更新医生档案成功: {}", doctorProfile.getDoctorProfileId());
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteDoctorProfile(Long doctorProfileId) {
        // 校验医生档案是否存在
        DoctorProfile doctorProfile = baseMapper.selectById(doctorProfileId);
        if (doctorProfile == null) {
            return Result.error("医生档案不存在");
        }

        boolean deleted = baseMapper.deleteById(doctorProfileId) > 0;
        if (deleted) {
            log.info("删除医生档案成功: {}", doctorProfileId);
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 查询所有医生信息
     * @return
     */
    @Override
    public Result<List<DoctorProfileDTO.DoctorProfileDetailDTO>> getAllDoctors() {

        MPJLambdaWrapper<User> doctorUsersWrapper = new MPJLambdaWrapper<>();
        doctorUsersWrapper.select(User::getUserId, User::getUserName)
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(Role::getType, RoleTypeEnum.DOCTOR.getCode());

        List<User> doctorUsers = userMapper.selectJoinList(User.class, doctorUsersWrapper);
        
        if (doctorUsers.isEmpty()) {
            return Result.success(List.of());
        }


        List<DoctorProfileDTO.DoctorProfileDetailDTO> doctorProfiles = doctorUsers.stream()
                .map(user -> {
                    // 是否有档案
                    LambdaQueryWrapper<DoctorProfile> profileWrapper = new LambdaQueryWrapper<>();
                    profileWrapper.eq(DoctorProfile::getUserId, user.getUserId())
                            .eq(DoctorProfile::getIsDeleted, 0);
                    
                    DoctorProfile doctorProfile = baseMapper.selectOne(profileWrapper);
                    
                    // 创建一个默认的
                    if (doctorProfile == null) {
                        doctorProfile = new DoctorProfile();
                        doctorProfile.setUserId(user.getUserId());
                        doctorProfile.setDepartmentId(0L); // 默认没分配部门
                        doctorProfile.setTitle("暂无");
                        doctorProfile.setSpecialty("暂无");
                        doctorProfile.setBio("暂无");
                        baseMapper.insert(doctorProfile);
                    }

                    // 查询医生档案详情（包含关联信息）
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
                            .eq(DoctorProfile::getUserId, user.getUserId())
                            .eq(DoctorProfile::getIsDeleted, 0);

                    return baseMapper.selectJoinOne(DoctorProfileDTO.DoctorProfileDetailDTO.class, queryWrapper);
                })
                .filter(profile -> profile != null) // 过滤掉null值
                .collect(Collectors.toList());

        return Result.success(doctorProfiles);
    }

    @Override
    public Result<String> batchDeleteDoctorProfiles(List<Long> doctorProfileIds) {
        if (doctorProfileIds.isEmpty()) {
            return Result.error("请选择要删除的医生档案");
        }

        int deletedCount = baseMapper.deleteBatchIds(doctorProfileIds);
        if (deletedCount > 0) {
            log.info("批量删除医生档案成功，共{}条", deletedCount);
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }
}