package org.hcmu.hcmupojo.dto;

import lombok.Data;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmupojo.entity.PendingUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PendingUserDTO {

    @Data
    public static class PendingUserGetRequestDTO {
        private String userName;
        private String name;
        private String email;
        private Long pageNum = 1L;
        private Long pageSize = 20L;
    }

    @Data
    public static class PendingUserListDTO {
        private Long id;
        private String userName;
        private String name;
        private String email;
        private Long roleId;
        private String roleName;

        static public PendingUserListDTO convert(PendingUser pendingUser) {
            if (pendingUser == null) {
                return null;
            }
            PendingUserListDTO dto = new PendingUserListDTO();
            dto.setId(pendingUser.getId());
            dto.setUserName(pendingUser.getUserName());
            dto.setName(pendingUser.getName());
            dto.setEmail(pendingUser.getEmail());
            dto.setRoleId(pendingUser.getRoleId());
            return dto;
        }

        static public List<PendingUserListDTO> convert(List<PendingUser> pendingUserList) {
            if (pendingUserList == null) {
                return new ArrayList<>();
            }
            return pendingUserList.stream().map(PendingUserListDTO::convert).collect(Collectors.toList());
        }
    }

    @Data
    public static class PendingUserInfoDTO {
        private Long id;
        private String userName;
        private String name;
        private String email;
        private Long roleId;
        private String roleName;
    }

}