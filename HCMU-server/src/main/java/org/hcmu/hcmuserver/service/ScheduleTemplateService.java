package org.hcmu.hcmuserver.service;

import com.github.yulichang.base.MPJBaseService;
import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.dto.PageDTO;
import org.hcmu.hcmupojo.dto.ScheduleTemplateDTO;
import org.hcmu.hcmupojo.entity.ScheduleTemplate;

public interface ScheduleTemplateService extends MPJBaseService<ScheduleTemplate> {
    Result<ScheduleTemplateDTO.TemplateListDTO> createTemplate(ScheduleTemplateDTO.TemplateCreateDTO createDTO);

    Result<PageDTO<ScheduleTemplateDTO.TemplateListDTO>> findAllTemplates(ScheduleTemplateDTO.TemplateGetRequestDTO requestDTO);

    Result<ScheduleTemplateDTO.TemplateListDTO> findTemplateById(Long templateId);

    Result<String> updateTemplateById(Long templateId, ScheduleTemplateDTO.TemplateUpdateDTO updateDTO);

    Result<String> deleteTemplateById(Long templateId);

    Result<ScheduleTemplateDTO.TemplateScheduleListDTO> createSchedule(Long templateId, ScheduleTemplateDTO.TemplateScheduleCreateDTO createDTO);

    Result<PageDTO<ScheduleTemplateDTO.TemplateScheduleListDTO>> findSchedules(Long templateId, ScheduleTemplateDTO.TemplateScheduleGetRequestDTO requestDTO);

    Result<ScheduleTemplateDTO.TemplateScheduleListDTO> findScheduleById(Long templateId, Long scheduleId);

    Result<String> updateScheduleById(Long templateId, Long scheduleId, ScheduleTemplateDTO.TemplateScheduleUpdateDTO updateDTO);

    Result<String> deleteScheduleById(Long templateId, Long scheduleId);
}
