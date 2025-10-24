package org.hcmu.hcmupojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

public class LogDTO {
    @Data
    public static class LogGetRequestDTO {
        private String userAccount;
        private String userName;
        private String roleName;
        private String ip;
        private String operation;
        private long pageNum = 1;
        private long pageSize = 20;
    }

    @Data
    public static class LogListDTO {
        private long logId;
        private String operation;
        private String userId;
        private String userName;
        private String userAccount;
        private String ip;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",locale = "zh", timezone = "GMT+8")
        private Date createTime;
    }
}
