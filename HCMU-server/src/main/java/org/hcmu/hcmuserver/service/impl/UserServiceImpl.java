package org.hcmu.hcmuserver.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.PermissionEnum;
import org.hcmu.hcmucommon.enumeration.RedisEnum;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmucommon.utils.RedisUtil;
import org.hcmu.hcmupojo.LoginUser;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.UserDTO;
import org.hcmu.hcmupojo.entity.Permission;
import org.hcmu.hcmupojo.entity.Role;
import org.hcmu.hcmupojo.entity.User;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import org.hcmu.hcmupojo.entity.relation.RolePermission;
import org.hcmu.hcmupojo.entity.relation.UserRole;
import org.hcmu.hcmupojo.entity.PendingUser;
import org.hcmu.hcmuserver.mapper.user.PendingUserMapper;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;
import org.hcmu.hcmuserver.mapper.user.UserMapper;
import org.hcmu.hcmuserver.mapper.user.UserRoleMapper;
import org.hcmu.hcmuserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisUtil redisCache;
    
    @Autowired
    private UserRoleMapper UserRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private MailServiceImpl mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PendingUserMapper pendingUserMapper;

    @Override
    public Result<PageDTO<UserDTO.UserListDTO>> findAllUsers(UserDTO.UserGetRequestDTO userGetRequestDTO) {
        MPJLambdaWrapper<User> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(User::getUserId, User::getUserName, User::getName, User::getPhone, User::getEmail, User::getInfo, User::getSex, User::getNickname)
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .select(UserRole::getRoleId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .selectAs(Role::getName, "roleName")
                .like(userGetRequestDTO.getUserName() != null, User::getUserName, userGetRequestDTO.getUserName())
                .like(userGetRequestDTO.getUserName() != null, User::getUserName, userGetRequestDTO.getUserName())
                .like(userGetRequestDTO.getRoleName() != null, Role::getName, userGetRequestDTO.getRoleName());
        IPage<UserDTO.UserListDTO> page = baseMapper.selectJoinPage(new Page<>(userGetRequestDTO.getPageNum(), userGetRequestDTO.getPageSize()), UserDTO.UserListDTO.class, queryWrapper);
        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result findUserById(Long userId) {
        MPJLambdaWrapper<User> queryWrapper = new MPJLambdaWrapper<User>();
        queryWrapper.select(User::getUserId, User::getUserName, User::getName, User::getPhone, User::getEmail, User::getInfo, User::getSex, User::getNickname)
                .select(UserRole::getRoleId)
                .selectAs(Role::getName, "roleName")
                .leftJoin(UserRole.class, UserRole::getUserId, User::getUserId)
                .leftJoin(Role.class, Role::getRoleId, UserRole::getRoleId)
                .eq(User::getUserId, userId);
        return Result.success(baseMapper.selectJoinOne(UserDTO.UserInfoDTO.class, queryWrapper));
    }

    @Override
    public Result<String> updateUserById(Long userId, UserDTO.UserUpdateDTO userUpdateDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userId);
        User user = baseMapper.selectOne(queryWrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        userUpdateDTO.updateUser(user);
        baseMapper.updateById(user);
        return Result.success("修改成功");
    }

    @Override
    public Result findPermissionBySelf() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();
        MPJLambdaWrapper<UserRole> queryWrapper = new MPJLambdaWrapper<UserRole>()
                .select(Permission::getKeyValue)
                .leftJoin(RolePermission.class, RolePermission::getRoleId, UserRole::getRoleId)
                .leftJoin(Permission.class, Permission::getPermissionId, RolePermission::getPermissionId)
                .eq(UserRole::getUserId, userId);
        List<PermissionEnum> permissions = UserRoleMapper.selectJoinList(PermissionEnum.class, queryWrapper);

        return Result.success(permissions);
    }



    @Override
    public Result<String> updateUserRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, userId);

        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleId, roleId);
        Role role = roleMapper.selectOne(roleQueryWrapper);

        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(User::getUserId, userId);
        User user = baseMapper.selectOne(userQueryWrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (role == null) {
            return Result.error("角色不存在");
        }


        UserRole UserRole = UserRoleMapper.selectOne(queryWrapper);
        if (UserRole == null) {
            UserRole = new UserRole();
            UserRole.setUserId(userId);
            UserRole.setRoleId(roleId);
            UserRoleMapper.insert(UserRole);
        } else {
            UserRole.setRoleId(roleId);
            UserRoleMapper.updateById(UserRole);
        }

        return Result.success("设置此用户为 " + role.getName() + " 成功");
    }


    @Override
    public Result changePassword(UserDTO.UserPasswordDTO userPassword) {
        // 获取SecurityContextHolder中的用户id
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
                .getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // LoginUser loginUser = redisCache.getCurrentUser();
        Long userId = loginUser.getUser().getUserId();
        // 获取用户信息
        User user = baseMapper.selectById(userId);
        // 判断旧密码是否正确
        if (!passwordEncoder.matches(userPassword.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误!");
        }
        // 判断两次密码是否一致
        if (!userPassword.checkPassword()) {
            return Result.error("两次密码不一致!");
        }
        // 修改密码
        user.setPassword(passwordEncoder.encode(userPassword.getNewPassword()));
        baseMapper.updateById(user);
        return Result.success("修改密码成功!", null);
    }

    @Override
    public Result getRebindEmailCode(UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();

        String email = userEmailVerifyDTO.getEmail();
        // 如果是自己本来的邮箱
        if (baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId).eq(User::getEmail, email)) != null) {
            return Result.error("请绑定新邮箱！");
        }
        if (baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null) {
            return Result.error("邮箱已被绑定");
        }

        String code = RandomUtil.randomNumbers(6);
        // 将验证码存入redis
        redisCache.setCacheObject(RedisEnum.REBIND.getDesc() + email, code, 5, TimeUnit.MINUTES);
        // 发送邮件
        mailService.sendVerifyCoed("绑定邮箱验证码", code, email);

        return Result.success("验证码发送成功!");
    }

    @Override
    public Result verifyEmailCode(UserDTO.UserEmailVerifyDTO userEmailVerifyDTO) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUser().getUserId();
        // 从redis中取出验证码
        String code = (String) redisCache.getCacheObject(RedisEnum.REBIND.getDesc() + userEmailVerifyDTO.getEmail());
        if (Objects.isNull(code)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "验证码已过期!");
        }
        // 判断验证码是否正确
        if (!code.equals(userEmailVerifyDTO.getCode())) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "验证码错误!");
        }
        // 从redis中删除
        redisCache.deleteObject(RedisEnum.REBIND.getDesc() + userEmailVerifyDTO.getEmail());

        // 更新用户邮箱
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        user.setEmail(userEmailVerifyDTO.getEmail());
        baseMapper.updateById(user);

        return Result.success("邮箱修改成功!");
    }

    @Override
    public Result<String> importPendingUsers(MultipartFile file, Long roleId) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传的文件不能为空");
        }

        String filename = file.getOriginalFilename();
        List<List<Object>> readAll;

        try {
            // 1. 自动根据文件类型选择解析器
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                // --- 处理 CSV 文件 ---
                CsvReader reader = CsvUtil.getReader();
                // CSV文件可能存在编码问题，这里使用UTF-8，如果你的文件是GBK，可以改成 CharsetUtil.CHARSET_GBK
                readAll = reader.read(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                                .getRows().stream()
                                .map(csvRow -> new ArrayList<Object>(csvRow.getRawList()))
                                .collect(Collectors.toList());
            } else if (filename != null && (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx"))) {
                // --- 处理 Excel 文件 ---
                ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
                readAll = reader.read();
                IoUtil.close(reader);
            } else {
                return Result.error("不支持的文件格式，请上传 .xls, .xlsx 或 .csv 文件");
            }

            // 2. 将解析后的数据交由统一的逻辑处理
            return processParsedData(readAll, roleId);

        } catch (Exception e) {
            log.error("导入用户时发生未知异常", e);
            return Result.error("导入失败，系统发生内部错误，请联系管理员");
        }
    }

    /**
     * 统一处理解析后的数据（无论是来自Excel还是CSV）
     */
    private Result<String> processParsedData(List<List<Object>> readAll, Long roleId) {
        if (readAll.size() <= 1) {
            return Result.error("文件为空或只包含表头，无法导入");
        }

        // --- 表头解析 ---
        List<Object> headerRow = readAll.get(0);
        Map<String, Integer> headerMap = new java.util.HashMap<>();
        for (int i = 0; i < headerRow.size(); i++) {
            headerMap.put(Objects.toString(headerRow.get(i), "").trim(), i);
        }

        Integer userNameIndex = headerMap.getOrDefault("用户名", headerMap.get("username"));
        Integer nameIndex = headerMap.getOrDefault("姓名", headerMap.get("name"));
        Integer emailIndex = headerMap.getOrDefault("邮箱", headerMap.get("email"));
        
        // --- 数据预处理与全量校验 ---
        List<PendingUser> pendingUsersToInsert = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (int i = 1; i < readAll.size(); i++) {
            List<Object> row = readAll.get(i);
            int rowNum = i + 1;

            String userName = getCellValue(row, userNameIndex);
            String name = getCellValue(row, nameIndex);
            String email = getCellValue(row, emailIndex);

            if (userName.isEmpty() && name.isEmpty() && email.isEmpty()) {
                continue; // 跳过空行
            }

            if (userName.isEmpty()) errorMessages.add("第 " + rowNum + "行：用户名不能为空");
            if (name.isEmpty()) errorMessages.add("第 " + rowNum + "行：姓名不能为空");
            if (email.isEmpty()) {
                errorMessages.add("第 " + rowNum + "行：邮箱不能为空");
            } else if (!isValidEmail(email)) {
                errorMessages.add("第 " + rowNum + "行：邮箱格式不正确 (" + email + ")");
            }

            // 只有当本行目前没有错误时，才构建对象（为了后续的文件内查重）
            if (errorMessages.stream().noneMatch(e -> e.startsWith("第 " + rowNum + "行"))) {
                 pendingUsersToInsert.add(PendingUser.builder()
                        .userName(userName).name(name).email(email).roleId(roleId).build());
            }
        }
        
        // 文件内数据重复性校验
        if (!pendingUsersToInsert.isEmpty()) {
            List<String> duplicateUserNames = findDuplicates(
                pendingUsersToInsert.stream().map(PendingUser::getUserName).collect(Collectors.toList())
            );
            if (!duplicateUserNames.isEmpty()) {
                errorMessages.add("文件内存在重复的用户名: " + String.join(", ", duplicateUserNames));
            }
        }

        // --- 原子性判断 ---
        if (!errorMessages.isEmpty()) {
            return Result.error("导入失败，数据校验未通过：\n" + String.join("\n", errorMessages));
        }
        
        if (pendingUsersToInsert.isEmpty()) {
            return Result.success("文件中没有有效数据行，成功导入 0 个用户");
        }

        for (PendingUser pu : pendingUsersToInsert) {
            pendingUserMapper.insert(pu);
        }

        return Result.success("导入成功，共导入 " + pendingUsersToInsert.size() + " 个用户");
    }
    /**
     * 安全地从行数据中获取字符串值
     */
    private String getCellValue(List<Object> row, Integer index) {
        if (index == null || index >= row.size() || row.get(index) == null) {
            return "";
        }
        return row.get(index).toString().trim();
    }

    /**
     * 简单的邮箱格式校验
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * 查找列表中的重复项
     */
    private <T> List<T> findDuplicates(List<T> list) {
        return list.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
