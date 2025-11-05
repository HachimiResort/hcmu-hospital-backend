package org.hcmu.hcmuserver.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.RoleTypeEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.PendingUserDTO;
import org.hcmu.hcmupojo.entity.PendingUser;
import org.hcmu.hcmupojo.entity.Role;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import org.hcmu.hcmuserver.mapper.user.PendingUserMapper;
import org.hcmu.hcmuserver.mapper.role.RoleMapper;
import org.hcmu.hcmuserver.service.DepartmentService;
import org.hcmu.hcmuserver.service.PendingUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PendingUserServiceImpl extends MPJBaseServiceImpl<PendingUserMapper, PendingUser> implements PendingUserService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private DepartmentService departmentService;

    @Override
    public Result<PageDTO<PendingUserDTO.PendingUserListDTO>> findAllPendingUsers(PendingUserDTO.PendingUserGetRequestDTO requestDTO) {
        MPJLambdaWrapper<PendingUser> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(PendingUser::getId, PendingUser::getUserName, PendingUser::getName, PendingUser::getEmail, PendingUser::getRoleId, PendingUser::getIdentityType, PendingUser::getStudentTeacherId, PendingUser::getDepartmentName, PendingUser::getTitle, PendingUser::getSpecialty)
                .leftJoin(Role.class, Role::getRoleId, PendingUser::getRoleId)
                .selectAs(Role::getName, "roleName")
                .like(requestDTO.getUserName() != null, PendingUser::getUserName, requestDTO.getUserName())
                .like(requestDTO.getName() != null, PendingUser::getName, requestDTO.getName())
                .like(requestDTO.getEmail() != null, PendingUser::getEmail, requestDTO.getEmail());
        IPage<PendingUserDTO.PendingUserListDTO> page = baseMapper.selectJoinPage(new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()), PendingUserDTO.PendingUserListDTO.class, queryWrapper);
        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<PendingUserDTO.PendingUserInfoDTO> findPendingUserById(Long id) {
        MPJLambdaWrapper<PendingUser> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(PendingUser::getId, PendingUser::getUserName, PendingUser::getName, PendingUser::getEmail, PendingUser::getRoleId, PendingUser::getIdentityType, PendingUser::getStudentTeacherId, PendingUser::getDepartmentName, PendingUser::getTitle, PendingUser::getSpecialty)
                .selectAs(Role::getName, "roleName")
                .leftJoin(Role.class, Role::getRoleId, PendingUser::getRoleId)
                .eq(PendingUser::getId, id);
        PendingUserDTO.PendingUserInfoDTO dto = baseMapper.selectJoinOne(PendingUserDTO.PendingUserInfoDTO.class, queryWrapper);
        if (dto == null) {
            return Result.error("待注册用户不存在");
        }
        return Result.success(dto);
    }


    @Override
    public Result<String> batchDeletePendingUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("待注册用户ID数组不能为空");
        }
        LambdaQueryWrapper<PendingUser> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(PendingUser::getId, ids);

        long existCount = baseMapper.selectCount(queryWrapper);
        if (existCount != ids.size()) {
            return Result.error("部分待注册用户不存在，无法完成批量删除");
        }


        baseMapper.delete(queryWrapper);
        return Result.success("批量删除成功");
    }


    @Override
    public Result<String> deletePendingUser(Long id) {
        LambdaQueryWrapper<PendingUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PendingUser::getId, id);
        PendingUser pendingUser = baseMapper.selectOne(queryWrapper);
        if (pendingUser == null) {
            return Result.error("待注册用户不存在");
        }
        baseMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> importPendingUsers(MultipartFile file, Long roleId) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传的文件不能为空");
        }

        String filename = file.getOriginalFilename();
        List<List<Object>> readAll;

        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }
        Integer roleType = role.getType(); // 角色类型：1-系统角色，2-医生角色，3-患者角色


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
            return processParsedData(readAll, roleId, roleType);

        } catch (Exception e) {
            log.error("导入用户时发生未知异常", e);
            return Result.error("导入失败，系统发生内部错误，请联系管理员");
        }
    }

    /**
     * 统一处理解析后的数据（无论是来自Excel还是CSV）
     */
    private Result<String> processParsedData(List<List<Object>> readAll, Long roleId, Integer roleType) {
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

        Integer departmentNameIndex = null;
        Integer titleIndex = null;
        Integer specialtyIndex = null;
        Integer identityTypeIndex = null;
        Integer studentTeacherIdIndex = null;


        RoleTypeEnum roleTypeEnum = RoleTypeEnum.getEnumByCode(roleType);

        Map<String, Integer> departmentMap = new HashMap<String,Integer>();

        switch (roleTypeEnum) {
            case DOCTOR: // 医生角色（code=2）
                departmentNameIndex = headerMap.getOrDefault("科室名称", headerMap.get("departmentName"));
                titleIndex = headerMap.getOrDefault("职称", headerMap.get("title"));
                specialtyIndex = headerMap.getOrDefault("专业特长", headerMap.getOrDefault("特长", headerMap.get("specialty")));
                break;
            case PATIENT: // 患者角色（code=3）
                identityTypeIndex =  headerMap.getOrDefault("身份类型", headerMap.get("identityType"));
                studentTeacherIdIndex = headerMap.getOrDefault("学号", headerMap.getOrDefault("工号", headerMap.getOrDefault("学号/工号", headerMap.get("studentTeacherId"))));
                break;
            case SYS: // 系统角色（code=1，修正枚举常量名）
                break;
            default:
                return Result.error("不支持的角色类型：" + roleType);
        }

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

            PendingUser pendingUser = new PendingUser();
            pendingUser.setUserName(userName);
            pendingUser.setName(name);
            pendingUser.setEmail(email);
            pendingUser.setRoleId(roleId);


            switch (roleTypeEnum) {
                case DOCTOR:
                    // 医生专属字段校验
                    String departmentName = getCellValue(row, departmentNameIndex);
                    String title = getCellValue(row, titleIndex);
                    String specialty = getCellValue(row, specialtyIndex);
                    if (departmentName.isEmpty()) errorMessages.add("第 " + rowNum + "行：科室名称不能为空");
                    if (title.isEmpty()) errorMessages.add("第 " + rowNum + "行：职称不能为空");
                    if (specialty.isEmpty()) errorMessages.add("第 " + rowNum + "行：专业特长不能为空");
                    pendingUser.setDepartmentName(departmentName);
                    pendingUser.setTitle(title);
                    pendingUser.setSpecialty(specialty);
                    break;
                case PATIENT:
                    // 患者专属字段校验
                    String identityType = getCellValue(row, identityTypeIndex);
                    String studentTeacherId = getCellValue(row, studentTeacherIdIndex);
                    if (identityType.isEmpty()) errorMessages.add("第 " + rowNum + "行：身份类型不能为空");
                    if (studentTeacherId.isEmpty()) errorMessages.add("第 " + rowNum + "行：学号/工号不能为空");
                    pendingUser.setIdentityType(identityType.isEmpty() ? null : Integer.parseInt(identityType));
                    pendingUser.setStudentTeacherId(studentTeacherId);
                    break;
                case SYS:
                    // 系统角色无专属字段，无需校验
                    break;
            }


            // 只有当本行目前没有错误时，才构建对象（为了后续的文件内查重）
            if (errorMessages.stream().noneMatch(e -> e.startsWith("第 " + rowNum + "行"))) {
                 pendingUsersToInsert.add(pendingUser);
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
            baseMapper.insert(pu);
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