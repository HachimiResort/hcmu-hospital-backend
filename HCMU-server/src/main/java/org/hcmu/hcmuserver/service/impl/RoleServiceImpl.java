package org.hcmu.hcmuserver.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.RoleDTO.PermissionUpdateDTO;
import org.hcmu.hcmupojo.dto.RoleDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.PermissionListDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleGetRequestDTO;
import org.hcmu.hcmupojo.entity.Permission;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.relation.RolePermission;
import org.hcmu.hcmuserver.mapper.role.PermissionMapper;
import org.hcmu.hcmuserver.mapper.role.RolePermissionMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;

import org.hcmu.hcmupojo.dto.RoleDTO.RoleListDTO;
import org.hcmu.hcmupojo.dto.RoleDTO.RoleCreateDTO;

import java.util.List;

@Service
@Slf4j
public class RoleServiceImpl extends MPJBaseServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public Result<RoleListDTO> createRole(RoleCreateDTO roleCreateDTO) {
        Role role = roleCreateDTO.toRole();

        if (baseMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getName, role.getName())) != null) {
            return Result.error("角色名已存在");
        }

        role.setIsDefault(0);
        baseMapper.insert(role);
        for (Long permissionId : roleCreateDTO.getPermissionList()) {
            Permission permission = permissionMapper.selectById(permissionId);
            if (permission == null) {
                return Result.error("权限不存在");
            }

        }
        for (Long permissionId : roleCreateDTO.getPermissionList()) {
            RolePermission rolePerm = new RolePermission();
            rolePerm.setRoleId(role.getRoleId());
            rolePerm.setPermissionId(permissionId);
            rolePermissionMapper.insert(rolePerm);
        }
        return Result.success(RoleListDTO.convert(role));
    }

    @Override
    public Result<List<RoleListDTO>> findAllRoles(RoleGetRequestDTO roleGetRequestDTO) {
        MPJLambdaWrapper<Role> queryWrapper = new MPJLambdaWrapper<Role>()
                .eq(roleGetRequestDTO.getRoleId() != null, Role::getRoleId, roleGetRequestDTO.getRoleId())
                .like(roleGetRequestDTO.getRoleName() != null, Role::getName, roleGetRequestDTO.getRoleName());
        List<Role> roleList = baseMapper.selectList(queryWrapper);
        return Result.success(RoleListDTO.convert(roleList));
    }

    @Override
    @Cacheable(value="permissions",keyGenerator = "keyGenerator")
    public Result<List<PermissionListDTO>> findAllPermissions() {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        List<Permission> permissionList = permissionMapper.selectList(queryWrapper);
        return Result.success(PermissionListDTO.convert(permissionList));
    }

    @Override
    @Cacheable(value="permissions",keyGenerator = "keyGenerator")
    public Result<List<PermissionListDTO>> findRolePermissionById(@RequestKeyParam Long roleId) {
        log.info("根据角色ID查权限信息Service: {}", roleId);
        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleId, roleId);
        Role role = baseMapper.selectOne(roleQueryWrapper);
        if (role == null) {
            return Result.error("角色不存在");
        }


        MPJLambdaWrapper<Role> queryWrapper = new MPJLambdaWrapper<Role>()
                .eq(Role::getRoleId, roleId)
                .leftJoin(RolePermission.class, RolePermission::getRoleId, Role::getRoleId)
                .leftJoin(Permission.class, Permission::getPermissionId, RolePermission::getPermissionId)
                .select(Permission::getPermissionId, Permission::getName, Permission::getKeyValue);
        List<PermissionListDTO> rolePermList = baseMapper.selectJoinList(PermissionListDTO.class, queryWrapper);
        return Result.success(rolePermList);
    }

    @Override
    public Result<String> updateRoleById(@RequestKeyParam Long roleId, RoleDTO.@Valid RoleUpdateDTO roleUpdate) {
        Role role = baseMapper.selectById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }
        int isDefault = role.getIsDefault();
        if (isDefault == 1 || isDefault == -1) {
            return Result.error("默认角色不可修改");
        }
        roleUpdate.updateRole(role);
        baseMapper.updateById(role);
        return Result.success("修改成功");
    }

    @Override
    @CacheEvict(value = "permissions", keyGenerator = "keyGenerator")
    public Result<String> updateRolePermissionById(@RequestKeyParam Long roleId, List<RoleDTO.PermissionUpdateDTO> permissionList) {
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<>();
        Role role = baseMapper.selectById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }
        int isDefault = role.getIsDefault();
        if (isDefault == 1 || isDefault == -1) {
            return Result.error("默认角色不可修改");
        }

        // 遍历permissionList，检查是否有不存在的权限
        for (PermissionUpdateDTO permissionAltDTO : permissionList) {
            Permission permission = permissionMapper.selectById(permissionAltDTO.getPermissionId());
            if (permission == null) {
                return Result.error("权限不存在");
            }
        }

        // 遍历permissionList，对每个permission进行操作
        for (PermissionUpdateDTO permissionAltDTO : permissionList) {
            queryWrapper.clear();
            queryWrapper.eq(RolePermission::getRoleId, roleId);
            queryWrapper.eq(RolePermission::getPermissionId, permissionAltDTO.getPermissionId());
            RolePermission rolePerm = rolePermissionMapper.selectOne(queryWrapper);
            // 总之先看看表里是否有这个权限，有的话看看是不是要删除
            if (rolePerm != null) {
                if (permissionAltDTO.getIsOwn() == 0) {
                    rolePermissionMapper.deleteById(rolePerm.getId());
                }
            } else if (permissionAltDTO.getIsOwn() == 1) {
                rolePerm = new RolePermission();
                rolePerm.setRoleId(roleId);
                rolePerm.setPermissionId(permissionAltDTO.getPermissionId());
                rolePermissionMapper.insert(rolePerm);
            }
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<String> deleteRoleById(@RequestKeyParam Long roleId) {
        Role role = baseMapper.selectById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }
        int isDefault = role.getIsDefault();
        if (isDefault == 1 || isDefault == -1) {
            return Result.error("默认角色不可删除");
        }

        // 删除角色
        baseMapper.deleteById(roleId);
        
        // 逻辑删除相关的角色权限关系
        LambdaUpdateWrapper<RolePermission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RolePermission::getRoleId, roleId)
                    .set(RolePermission::getIsDeleted, 1);
        rolePermissionMapper.update(null, updateWrapper);
        
        return Result.success("删除成功");
    }
}
