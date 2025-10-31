package org.hcmu.hcmuserver.mapper.patientprofile;

import com.github.yulichang.base.MPJBaseMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hcmu.hcmupojo.entity.PatientProfile;

@Mapper
public interface PatientProfileMapper extends MPJBaseMapper<PatientProfile> {
}