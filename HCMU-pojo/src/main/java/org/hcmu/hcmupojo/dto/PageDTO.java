package org.hcmu.hcmupojo.dto;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.Data;

/**
 * 分页DTO
 * @author 
 * @param <T>
 */

@Data
public class PageDTO<T> {
    private long total;
    private List<T> list;

    public PageDTO(IPage<T> page) {
        this.total = page.getTotal();
        this.list = page.getRecords();
    }
}
