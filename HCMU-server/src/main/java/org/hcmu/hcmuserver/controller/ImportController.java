package org.hcmu.hcmuserver.controller;

import org.hcmu.hcmucommon.result.Result;
import org.hcmu.hcmupojo.entity.User;
import org.hcmu.hcmuserver.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/import")
public class ImportController {

    @Autowired
    @Qualifier("userImportService")
    private ImportService<User> userImportService;

    /**
     * 用户数据导入
     */
    @PostMapping("/user")
    public Result importUser(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = userImportService.importFile(file, User.class);
        return Result.success(result);
    }

    // 其他表的导入接口可以类似添加
    // @PostMapping("/otherTable")
    // public Result importOtherTable(@RequestParam("file") MultipartFile file) {
    //     Map<String, Object> result = otherTableImportService.importFile(file, OtherTable.class);
    //     return Result.success(result);
    // }
}