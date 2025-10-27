package org.hcmu.hcmupojo.dto;

import lombok.Data;
import org.hcmu.hcmucommon.annotation.RequestKeyParam;
import org.hcmu.hcmupojo.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    @Data
    public static class UserGetRequestDTO {
        private String userName;
        private String nickname;
        private String roleName;
        private Long pageNum = 1L;
        private Long pageSize = 20L;
    }


    @Data
    public static class UserLoginDTO {
        @RequestKeyParam
        @NotBlank(message = "用户名不能为空")
        private String userName;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class UserRegisterDTO {
        @RequestKeyParam
        @NotBlank(message = "用户名不能为空")
        private String userName;

        @NotBlank(message = "密码不能为空")
        private String password;

        @NotBlank(message = "确认密码不能为空")
        private String checkPassword;

        @NotBlank(message = "姓名不能为空")
        private String name;

        private String phone;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        /**
         * 验证码
         */
        private String code;

        public boolean checkPassword() {
            return password.equals(checkPassword);
        }

        public User toUser() {
            User user = new User();
            user.setPassword(password);
            user.setUserName(userName);
            user.setPhone(phone);
            user.setEmail(email);
            user.setName(name);
            user.setNickname(name);
            return user;
        }
    }

    @Data
    public static class UserUpdateDTO {
        private String phone;

        private String info;

        private String nickname;

        private String sex;

        private Integer age;

        public void updateUser(User user) {
            if (phone != null) {
                user.setPhone(phone);
            }
            if (info != null) {
                user.setInfo(info);
            }
            if (nickname != null) {
                user.setNickname(nickname);
            }
            if (sex != null) {
                user.setSex(sex);
            }
        }
    }

    @Data
    public static class UserPasswordDTO {
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;

        @NotBlank(message = "确认密码不能为空")
        private String checkPassword;

        public boolean checkPassword() {
            return newPassword.equals(checkPassword);
        }
    }

    @Data
    public static class UserListDTO {
        private Long userId;
        private String userName;
        private String name;
        private String roleId;
        private String roleName;
        private Integer stateId;

        static public UserListDTO convert(User user) {
            if (user == null) {
                return null;
            }
            UserListDTO userListDTO = new UserListDTO();
            userListDTO.setUserId(user.getUserId());
            userListDTO.setUserName(user.getUserName());
            return userListDTO;
        }

        static public List<UserListDTO> convert(List<User> userList) {
            if (userList == null) {
                return new ArrayList<>();
            }
            return userList.stream().map(UserListDTO::convert).collect(Collectors.toList());
        }
    }

    @Data
    public static class UserInfoDTO {
        private Long userId;
        private String userName;
        private String name;
        private String roleId;
        private String roleName;
        private String phone;
        private String email;
        private String nickname;

        public void applyMask(Long mask) {
            try {
                Field[] fields = this.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ((mask & 1) == 0) {
                        field.set(this, "*");
                    }
                    mask >>= 1;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Data
    public static class UserFieldGetRequestDTO {
        private List<Long> fieldIds;
    }

    @Data
    public static class UserFieldDTO {
        private Long fieldId;
        private String fieldName;
        private String value;
    }

    @Data
    public static class UserEmailVerifyDTO {
        private String code;
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        @RequestKeyParam
        private String email;
    }

    @Data
    public static class UserResetPasswordDTO {
        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        @RequestKeyParam
        private String email;

        @NotBlank(message = "密码不能为空")
        private String password;

        @NotBlank(message = "确认密码不能为空")
        private String checkPassword;

        @NotBlank(message = "验证码不能为空")
        private String code;

        public boolean checkPassword() {
            return password.equals(checkPassword);
        }
    }
}
