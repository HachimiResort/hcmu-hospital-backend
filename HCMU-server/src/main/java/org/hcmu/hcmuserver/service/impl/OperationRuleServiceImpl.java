package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import io.swagger.v3.oas.models.security.SecurityScheme.In;
import lombok.extern.slf4j.Slf4j;
import org.hcmu.hcmucommon.enumeration.OpRuleEnum;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.OperationRuleDTO;
import org.hcmu.hcmupojo.dto.OperationRuleDTO.RuleInfo;
import org.hcmu.hcmupojo.entity.Appointment;
import org.hcmu.hcmupojo.entity.OperationRule;
import org.hcmu.hcmupojo.entity.DoctorSchedule;
import org.hcmu.hcmuserver.mapper.appointment.AppointmentMapper;
import org.hcmu.hcmuserver.mapper.operationrule.OperationRuleMapper;
import org.hcmu.hcmuserver.service.OperationRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OperationRuleServiceImpl extends MPJBaseServiceImpl<OperationRuleMapper, OperationRule> implements OperationRuleService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public Result<List<OperationRuleDTO.RuleListDTO>> getAllRules() {
        MPJLambdaWrapper<OperationRule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(OperationRule.class);
        List<OperationRule> existingRules = baseMapper.selectList(queryWrapper);
        
        List<OperationRuleDTO.RuleListDTO> items = OperationRuleDTO.RuleListDTO.convertList(existingRules);
        return Result.success(items);
    }

    @Override
    public Result<OperationRuleDTO.RuleListDTO> updateRule(Integer code, OperationRuleDTO.RuleUpdateRequest request) {
        if (code == null) {
            return Result.error("规则编码不能为空");
        }

        OpRuleEnum ruleDefinition = Arrays.stream(OpRuleEnum.values())
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
        if (ruleDefinition == null) {
            return Result.error("规则不存在");
        }

        if (request.getValue() != null) {
            Integer min = ruleDefinition.getMinValue();
            Integer max = ruleDefinition.getMaxValue();
            if (min != null && request.getValue() < min) {
                return Result.error("配置值不能小于 " + min);
            }
            if (max != null && request.getValue() > max) {
                return Result.error("配置值不能大于 " + max);
            }

            // 校验规则数据约束
            Result<Void> validationResult = validateRuleChange(ruleDefinition, request.getValue());
            if (validationResult.getCode() != 200) {
                return Result.error(validationResult.getMsg());
            }
        }

        MPJLambdaWrapper<OperationRule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(OperationRule::getCode, code)
            .last("limit 1");

        OperationRule existing = baseMapper.selectOne(queryWrapper);
        if (existing == null) {
            baseMapper.insert(new OperationRule() {{
                setCode(code);
                setName(ruleDefinition.name());
                if (request.getValue() != null) {
                    setValue(request.getValue());
                } else {
                    setValue(ruleDefinition.getDefaultValue());
                }
                if (request.getEnabled() != null) {
                    setEnabled(request.getEnabled());
                } else {
                    setEnabled(1);
                }
            }});
            OperationRule created = baseMapper.selectOne(queryWrapper);
            return Result.success(OperationRuleDTO.RuleListDTO.convert(created));
        }

        request.updateRule(existing);


        baseMapper.updateById(existing);

        OperationRule refreshed = baseMapper.selectOne(queryWrapper);

        return Result.success(OperationRuleDTO.RuleListDTO.convert(refreshed));
    }

    @Override
    public RuleInfo getRuleValueByCode(OpRuleEnum opRuleEnum) {
        if (opRuleEnum == null) {
            return null;
        }

        MPJLambdaWrapper<OperationRule> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.eq(OperationRule::getCode, opRuleEnum.getCode())
            .last("limit 1");

        OperationRule existing = baseMapper.selectOne(queryWrapper);
        if (existing != null) {
            return new RuleInfo() {{
                setValue(existing.getValue());
                setEnabled(existing.getEnabled());
            }};
        } else {
            Integer code = opRuleEnum.getCode();
            baseMapper.insert(new OperationRule() {{
                setCode(code);
                setName(opRuleEnum.name());
                setValue(opRuleEnum.getDefaultValue());
                setEnabled(1);
            }});
            return new RuleInfo() {{
                setValue(opRuleEnum.getDefaultValue());
                setEnabled(1);
            }};
        }
    }

    @Override
    public Result<Void> validateRuleChange(OpRuleEnum ruleEnum, Integer newValue) {
        if (ruleEnum == null || newValue == null) {
            return Result.success(null);
        }

        // 根据不同的规则类型进行不同的校验
        switch (ruleEnum) {
            case BOOKING_MAX_PER_DAY_GLOBAL:
                return validateBookingMaxPerDayGlobal(newValue);
            case BOOKING_MAX_PER_DAY_PER_DEPT:
                return validateBookingMaxPerDayPerDept(newValue);
            case BOOKING_MAX_FUTURE_DAYS:
                return validateBookingMaxFutureDays(newValue);
            case BOOKING_LIMIT_SAME_TIMESLOT:
                return validateBookingLimitSameTimeslot(newValue);
            // 可以继续添加其他规则的校验逻辑
            default:
                // 其他规则暂不校验
                return Result.success(null);
        }
    }

    /**
     * 校验 101 规则修改
     * 检查是否存在某个用户在某一天的预约数超过新值
     */
    private Result<Void> validateBookingMaxPerDayGlobal(Integer newValue) {
        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Appointment::getPatientUserId)
        .select("t1.schedule_date")
        .select("COUNT(*) AS cnt")
        .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
        .eq(Appointment::getIsDeleted, 0)
        .in(Appointment::getStatus, Arrays.asList(1, 2, 3, 4))
        .last("GROUP BY t.patient_user_id, t1.schedule_date HAVING COUNT(*) > " + newValue + " LIMIT 1");

        List<Map<String, Object>> results = appointmentMapper.selectJoinMaps(queryWrapper);

        if (results != null && !results.isEmpty()) {
            return Result.error("存在用户在某天的预约数量超过了新设定的值（" + newValue + "），无法修改此规则");
        }

        return Result.success(null);
    }

    /**
     * 校验 102 规则修改
     * 检查是否存在某个用户在某一天某个科室的预约数超过新值
     */
    private Result<Void> validateBookingMaxPerDayPerDept(Integer newValue) {

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Appointment::getPatientUserId)
        .select("t1.schedule_date")
        .select("doctor_profile.department_id")
        .select("COUNT(*) AS cnt")
        .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
        .leftJoin("doctor_profile ON doctor_profile.user_id = t1.doctor_user_id")
        .eq(Appointment::getIsDeleted, 0)
        .in(Appointment::getStatus, Arrays.asList(1, 2, 3, 4))
        .last("GROUP BY t.patient_user_id, t1.schedule_date, doctor_profile.department_id HAVING COUNT(*) > " + newValue + " LIMIT 1");

        List<Map<String, Object>> results = appointmentMapper.selectJoinMaps(queryWrapper);

        if (results != null && !results.isEmpty()) {
            return Result.error("存在用户在某天某科室的预约数量超过了新设定的值（" + newValue + "），无法修改此规则");
        }

        return Result.success(null);
    }

    /**
     * 校验 103 规则修改
     * 检查是否存在预约的排班日期晚于 今天 + newValue 天
     */
    private Result<Void> validateBookingMaxFutureDays(Integer newValue) {
        LocalDate threshold = LocalDate.now().plusDays(newValue);

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Appointment::getPatientUserId)
            .select("t1.schedule_date")
            .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
            .eq(Appointment::getIsDeleted, 0)
            .in(Appointment::getStatus, Arrays.asList(1, 2))
            .gt(DoctorSchedule::getScheduleDate, threshold)
            .last("LIMIT 1");

        List<Map<String, Object>> results = appointmentMapper.selectJoinMaps(queryWrapper);

        if (results != null && !results.isEmpty()) {
            return Result.error("存在预约日期晚于今天+" + newValue + "天的记录，无法修改此规则");
        }

        return Result.success(null);
    }

    /**
     * 104无需额外约束
     */



    /**
     * 校验 105 规则修改
     * 是否存在同一时间多个预约
     */
    private Result<Void> validateBookingLimitSameTimeslot(Integer newValue) {
        if (newValue == null || newValue == 0) {
            return Result.success(null);
        }

        MPJLambdaWrapper<Appointment> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(Appointment::getPatientUserId)
            .select("t1.schedule_date")
            .select("t1.slot_period")
            .select("COUNT(*) AS cnt")
            .leftJoin(DoctorSchedule.class, DoctorSchedule::getScheduleId, Appointment::getScheduleId)
            .eq(Appointment::getIsDeleted, 0)
            .in(Appointment::getStatus, Arrays.asList(1, 2, 3, 4))
            .last("GROUP BY t.patient_user_id, t1.schedule_date, t1.slot_period HAVING COUNT(*) > 1 LIMIT 1");

        List<Map<String, Object>> results = appointmentMapper.selectJoinMaps(queryWrapper);

        if (results != null && !results.isEmpty()) {
            return Result.error("存在用户在一天有同一时间段有超过1个有效预约, 无法修改此规则");
        }

        return Result.success(null);
    }

}
