package org.hcmu.hcmupojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;
import org.hcmu.hcmupojo.entity.Permission;
import org.hcmu.hcmupojo.entity.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoleDTO {
    @Data
    public static class RoleGetRequestDTO {
        private String roleName;
        @RequestKeyParam
        private Long roleId;

    }

    @Data
    public static class RoleCreateDTO {
        @NotBlank(message = "角色名不能为空")
        private String roleName;

        @NotBlank(message = "角色描述不能为空")
        private String roleInfo;

        @NotNull(message = "不能没有权限")
        private List<Long> permissionList;

        public Role toRole() {
            Role role = new Role();
            role.setName(roleName);
            role.setRoleInfo(roleInfo);
            return role;
        }
    }

    @Data
    public static class RoleListDTO {
        @NotNull(message = "角色ID不能为空")
        private Long roleId;

        @NotNull(message = "角色名不能为空")
        private String roleName;

        @NotNull(message = "角色描述不能为空")
        private String roleInfo;

        private Integer isDefault = 0;
        /**
         * Role转RoleListDTO
         *
         * @param role
         * @return RoleListDTO
         */
        public static RoleListDTO convert(Role role) {
            if (role == null) {
                return null;
            }
            RoleListDTO roleListDTO = new RoleListDTO();
            roleListDTO.setRoleId(role.getRoleId());
            roleListDTO.setRoleName(role.getName());
            roleListDTO.setRoleInfo(role.getRoleInfo());
            roleListDTO.setIsDefault(role.getIsDefault());
            return roleListDTO;
        }

        /**
         * List<Role>转List<RoleListDTO>
         *
         * @param roleList
         * @return List<RoleListDTO>
         */
        public static List<RoleListDTO> convert(List<Role> roleList) {
            if (roleList == null) {
                return new ArrayList<>();
            }
            return roleList.stream().map(RoleListDTO::convert).collect(Collectors.toList());
        }
    }

    @Data
    public static class RoleUserUpdateDTO {
        @NotNull(message = "角色ID不能为空")
        private Long roleId;
    }

    @Data
    public static class RoleUpdateDTO {
        private String roleName;

        private String roleInfo;

        public void updateRole(Role role) {
            if (roleName != null) {
                role.setName(roleName);
            }
            if (roleInfo != null) {
                role.setRoleInfo(roleInfo);
            }
        }
    }

    @Data
    public static class PermissionUpdateDTO {
        @NotNull(message = "权限ID不能为空")
        private Long permissionId;

        @NotNull(message = "是否拥有不能为空")
        @Max(value = 1, message = "是否拥有只能为0或1")
        @Min(value = 0, message = "是否拥有只能为0或1")
        private int isOwn;
    }

    @Data
    public static class PermissionListDTO {

        private String name;

        private Long permissionId;

        private PermissionEnum keyValue;

        private Integer type;

        /**
         * Permission转PermissionListDTO
         *
         * @param permission
         * @return PermissionListDTO
         */
        public static PermissionListDTO convert(Permission permission) {
            if (permission == null) {
                return null;
            }
            PermissionListDTO permissionListDTO = new PermissionListDTO();
            permissionListDTO.setName(permission.getName());
            permissionListDTO.setPermissionId(permission.getPermissionId());
            permissionListDTO.setKeyValue(permission.getKeyValue());
            return permissionListDTO;
        }

        /**
         * List<Permission>转List<PermissionListDTO>
         *
         * @param permissionList
         * @return List<PermissionListDTO>
         */
        public static List<PermissionListDTO> convert(List<Permission> permissionList) {
            if (permissionList == null) {
                return new ArrayList<>();
            }
            return permissionList.stream().map(PermissionListDTO::convert).collect(Collectors.toList());
        }
    }
}
