package org.hcmu.hcmuserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleTemplateDTO;
import org.hcmu.hcmupojo.entity.Schedule;
import org.hcmu.hcmupojo.entity.ScheduleTemplate;
import org.hcmu.hcmuserver.mapper.scheduletemplate.ScheduleTemplateMapper;
import org.hcmu.hcmuserver.mapper.scheduletemplate.TemplateScheduleMapper;
import org.hcmu.hcmuserver.service.ScheduleTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScheduleTemplateServiceImpl extends MPJBaseServiceImpl<ScheduleTemplateMapper, ScheduleTemplate>
        implements ScheduleTemplateService {

    @Autowired
    private TemplateScheduleMapper templateScheduleMapper;

    @Override
    public Result<ScheduleTemplateDTO.TemplateListDTO> createTemplate(ScheduleTemplateDTO.TemplateCreateDTO createDTO) {
        LambdaQueryWrapper<ScheduleTemplate> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(ScheduleTemplate::getTemplateName, createDTO.getTemplateName())
                .eq(ScheduleTemplate::getIsDeleted, 0);
        if (baseMapper.selectCount(nameWrapper) > 0) {
            return Result.error("模板名称已存在");
        }

        ScheduleTemplate template = new ScheduleTemplate();
        template.setTemplateName(createDTO.getTemplateName());

        baseMapper.insert(template);

        ScheduleTemplateDTO.TemplateListDTO dto = new ScheduleTemplateDTO.TemplateListDTO();
        dto.setTemplateId(template.getTemplateId());
        dto.setTemplateName(template.getTemplateName());
        dto.setCreateTime(template.getCreateTime());
        dto.setUpdateTime(template.getUpdateTime());
        return Result.success(dto);
    }

    @Override
    public Result<PageDTO<ScheduleTemplateDTO.TemplateListDTO>> findAllTemplates(ScheduleTemplateDTO.TemplateGetRequestDTO requestDTO) {
        MPJLambdaWrapper<ScheduleTemplate> wrapper = new MPJLambdaWrapper<>();
        wrapper.select(ScheduleTemplate::getTemplateId, ScheduleTemplate::getTemplateName,
                        ScheduleTemplate::getCreateTime, ScheduleTemplate::getUpdateTime)
                .like(requestDTO.getTemplateName() != null, ScheduleTemplate::getTemplateName, requestDTO.getTemplateName())
                .eq(ScheduleTemplate::getIsDeleted, 0)
                .orderByDesc(ScheduleTemplate::getCreateTime);

        IPage<ScheduleTemplateDTO.TemplateListDTO> page = baseMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                ScheduleTemplateDTO.TemplateListDTO.class,
                wrapper);
        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<ScheduleTemplateDTO.TemplateListDTO> findTemplateById(Long templateId) {
        ScheduleTemplate template = baseMapper.selectById(templateId);
        if (template == null || template.getIsDeleted() == 1) {
            return Result.error("模板不存在");
        }

        ScheduleTemplateDTO.TemplateListDTO dto = new ScheduleTemplateDTO.TemplateListDTO();
        dto.setTemplateId(template.getTemplateId());
        dto.setTemplateName(template.getTemplateName());
        dto.setCreateTime(template.getCreateTime());
        dto.setUpdateTime(template.getUpdateTime());
        return Result.success(dto);
    }

    @Override
    public Result<String> updateTemplateById(Long templateId, ScheduleTemplateDTO.TemplateUpdateDTO updateDTO) {
        ScheduleTemplate template = baseMapper.selectById(templateId);
        if (template == null || template.getIsDeleted() == 1) {
            return Result.error("模板不存在");
        }

        if (updateDTO.getTemplateName() != null && !updateDTO.getTemplateName().equals(template.getTemplateName())) {
            LambdaQueryWrapper<ScheduleTemplate> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(ScheduleTemplate::getTemplateName, updateDTO.getTemplateName())
                    .eq(ScheduleTemplate::getIsDeleted, 0);
            if (baseMapper.selectCount(nameWrapper) > 0) {
                return Result.error("模板名称已存在");
            }
        }

        updateDTO.updateTemplate(template);
        baseMapper.updateById(template);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteTemplateById(Long templateId) {
        ScheduleTemplate template = baseMapper.selectById(templateId);
        if (template == null || template.getIsDeleted() == 1) {
            return Result.error("模板不存在");
        }

        baseMapper.deleteById(templateId);
        return Result.success("删除成功");
    }

    @Override
    public Result<ScheduleTemplateDTO.TemplateScheduleListDTO> createSchedule(Long templateId, ScheduleTemplateDTO.TemplateScheduleCreateDTO createDTO) {
        ScheduleTemplate template = baseMapper.selectById(templateId);
        if (template == null || template.getIsDeleted() == 1) {
            return Result.error("模板不存在");
        }

        LambdaQueryWrapper<Schedule> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(Schedule::getTemplateId, templateId)
                .eq(Schedule::getSlotPeriod, createDTO.getSlotPeriod());
        if (templateScheduleMapper.selectCount(duplicateWrapper) > 0) {
            return Result.error("该模板在该时间段已存在排班");
        }

        Schedule schedule = new Schedule();
        schedule.setTemplateId(templateId);
        schedule.setSlotType(createDTO.getSlotType());
        schedule.setTotalSlots(createDTO.getTotalSlots());
        schedule.setSlotPeriod(createDTO.getSlotPeriod());
        schedule.setWeekday(createDTO.getWeekday());
        schedule.setFee(createDTO.getFee());

        templateScheduleMapper.insert(schedule);

        ScheduleTemplateDTO.TemplateScheduleListDTO dto = new ScheduleTemplateDTO.TemplateScheduleListDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setTemplateId(schedule.getTemplateId());
        dto.setSlotType(schedule.getSlotType());
        dto.setTotalSlots(schedule.getTotalSlots());
        dto.setWeekday(schedule.getWeekday());
        dto.setSlotPeriod(schedule.getSlotPeriod());
        dto.setFee(schedule.getFee());
        return Result.success(dto);
    }

    @Override
    public Result<PageDTO<ScheduleTemplateDTO.TemplateScheduleListDTO>> findSchedules(Long templateId, ScheduleTemplateDTO.TemplateScheduleGetRequestDTO requestDTO) {
        ScheduleTemplate template = baseMapper.selectById(templateId);
        if (template == null || template.getIsDeleted() == 1) {
            return Result.error("模板不存在");
        }

        MPJLambdaWrapper<Schedule> wrapper = new MPJLambdaWrapper<>();
        wrapper.select(Schedule::getScheduleId, Schedule::getTemplateId, Schedule::getSlotType,
                        Schedule::getTotalSlots, Schedule::getSlotPeriod, Schedule::getFee,
                        Schedule::getCreateTime, Schedule::getUpdateTime, Schedule::getWeekday)
                .eq(Schedule::getTemplateId, templateId)
                .eq(requestDTO.getSlotType() != null, Schedule::getSlotType, requestDTO.getSlotType())
                .eq(requestDTO.getSlotPeriod() != null, Schedule::getSlotPeriod, requestDTO.getSlotPeriod())
                .eq(requestDTO.getWeekday() != null, Schedule::getWeekday, requestDTO.getWeekday())
                .orderByDesc(Schedule::getCreateTime);

        IPage<ScheduleTemplateDTO.TemplateScheduleListDTO> page = templateScheduleMapper.selectJoinPage(
                new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize()),
                ScheduleTemplateDTO.TemplateScheduleListDTO.class,
                wrapper);
        return Result.success(new PageDTO<>(page));
    }

    @Override
    public Result<ScheduleTemplateDTO.TemplateScheduleListDTO> findScheduleById(Long templateId, Long scheduleId) {
        Schedule schedule = templateScheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1 || !templateId.equals(schedule.getTemplateId())) {
            return Result.error("模板排班不存在");
        }

        ScheduleTemplateDTO.TemplateScheduleListDTO dto = new ScheduleTemplateDTO.TemplateScheduleListDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setTemplateId(schedule.getTemplateId());
        dto.setSlotType(schedule.getSlotType());
        dto.setWeekday(schedule.getWeekday());
        dto.setTotalSlots(schedule.getTotalSlots());
        dto.setSlotPeriod(schedule.getSlotPeriod());
        dto.setFee(schedule.getFee());
        dto.setCreateTime(schedule.getCreateTime());
        dto.setUpdateTime(schedule.getUpdateTime());
        return Result.success(dto);
    }

    @Override
    public Result<String> updateScheduleById(Long templateId, Long scheduleId, ScheduleTemplateDTO.TemplateScheduleUpdateDTO updateDTO) {
        Schedule schedule = templateScheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1 || !templateId.equals(schedule.getTemplateId())) {
            return Result.error("模板排班不存在");
        }

        if (updateDTO.getSlotPeriod() != null && !updateDTO.getSlotPeriod().equals(schedule.getSlotPeriod())) {
            LambdaQueryWrapper<Schedule> duplicateWrapper = new LambdaQueryWrapper<>();
            duplicateWrapper.eq(Schedule::getTemplateId, templateId)
                    .eq(Schedule::getSlotPeriod, updateDTO.getSlotPeriod())
                    .ne(Schedule::getScheduleId, scheduleId);
            if (templateScheduleMapper.selectCount(duplicateWrapper) > 0) {
                return Result.error("该模板在该时间段已存在排班");
            }
        }

        updateDTO.updateSchedule(schedule);
        templateScheduleMapper.updateById(schedule);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deleteScheduleById(Long templateId, Long scheduleId) {
        Schedule schedule = templateScheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getIsDeleted() == 1 || !templateId.equals(schedule.getTemplateId())) {
            return Result.error("模板排班不存在");
        }

        templateScheduleMapper.deleteById(scheduleId);
        return Result.success("删除成功");
    }
}
