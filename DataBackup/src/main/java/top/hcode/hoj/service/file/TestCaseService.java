package top.hcode.hoj.service.file;

import top.hcode.hoj.common.exception.StatusForbiddenException;
import org.springframework.web.multipart.MultipartFile;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: tuxiaobei
 * @Date: 2022/7/15 11:21
 * @Description:
 */
public interface TestCaseService {

    public CommonResult<Map<Object, Object>> uploadTestcaseZip(MultipartFile file, Long gid);

    public CommonResult<Map<Object, Object>> uploadAnswersZip(MultipartFile file, Long gid);

    public void downloadTestcase(Long pid, HttpServletResponse response) throws StatusFailException, StatusForbiddenException;
}