// 文件路径: HCMU-Hospital-Backend/HCMU-server/src/main/java/org/hcmu/hcmuserver/service/impl/AbstractImportServiceImpl.java
package org.hcmu.hcmuserver.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.*;
import org.hcmu.hcmucommon.exception.ServiceException;
import org.hcmu.hcmuserver.service.ImportService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象导入服务实现类
 * @param <T> 实体类泛型
 * @param <M> Mapper类泛型
 */
public abstract class AbstractImportServiceImpl<T, M extends BaseMapper<T>>
        extends ServiceImpl<M, T> implements ImportService<T> {

    @Override
    public Map<String, Object> importFile(MultipartFile file, Class<T> clazz) {
        Map<String, Object> result = new HashMap<>(4);
        List<T> successList = new ArrayList<>();
        List<Map<String, Object>> errorList = new ArrayList<>();

        try {
            // 1. 解析文件
            List<T> dataList = parseFile(file, clazz);
            if (CollUtil.isEmpty(dataList)) {
                result.put("success", 0);
                result.put("fail", 0);
                result.put("errorData", errorList);
                return result;
            }

            // 2. 数据校验
            for (int i = 0; i < dataList.size(); i++) {
                T entity = dataList.get(i);
                String errorMsg = validateData(entity);
                if (errorMsg == null) {
                    successList.add(entity);
                } else {
                    Map<String, Object> errorData = new HashMap<>(2);
                    errorData.put("row", i + 1);
                    errorData.put("message", errorMsg);
                    errorList.add(errorData);
                }
            }

            // 3. 批量保存
            if (CollUtil.isNotEmpty(successList)) {
                boolean saveResult = batchSave(successList);
                if (!saveResult) {
                    throw new ServiceException("批量保存数据失败");
                }
            }

            // 4. 整理结果
            result.put("success", successList.size());
            result.put("fail", errorList.size());
            result.put("errorData", errorList);

        } catch (Exception e) {
            throw new ServiceException("文件导入失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 解析文件为实体列表
     */
    private List<T> parseFile(MultipartFile file, Class<T> clazz) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new ServiceException("文件名称不能为空");
        }

        try (InputStream inputStream = file.getInputStream()) {
            if (fileName.endsWith(".csv")) {
                // 解析CSV文件
                CsvReader reader = CsvUtil.getReader();
                return reader.read(inputStream.toString(), clazz);
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                // 解析Excel文件
                ExcelReader reader = ExcelUtil.getReader(inputStream);
                return reader.readAll(clazz);
            } else {
                throw new ServiceException("不支持的文件格式，请上传CSV或Excel文件");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<T> entityList) {
        // 调用MyBatis-Plus的批量保存方法
        return saveBatch(entityList);
    }
}