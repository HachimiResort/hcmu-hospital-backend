// 文件路径: HCMU-Hospital-Backend/HCMU-server/src/main/java/org/hcmu/hcmuserver/service/ImportService.java
package org.hcmu.hcmuserver.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 通用导入服务接口
 * @param <T> 实体类泛型
 */
public interface ImportService<T> {
    /**
     * 导入文件并处理数据
     * @param file 上传的文件
     * @param clazz 实体类字节码
     * @return 导入结果
     */
    Map<String, Object> importFile(MultipartFile file, Class<T> clazz);

    /**
     * 验证单条数据
     * @param entity 实体对象
     * @return 验证结果，null表示验证通过
     */
    String validateData(T entity);

    /**
     * 批量保存数据
     * @param entityList 实体列表
     * @return 保存结果
     */
    boolean batchSave(List<T> entityList);
}