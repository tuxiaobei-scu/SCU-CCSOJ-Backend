package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.ProblemManager;
import top.hcode.hoj.pojo.dto.PidListDto;
import top.hcode.hoj.pojo.vo.ProblemInfoVo;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.vo.RandomProblemVo;
import top.hcode.hoj.service.oj.ProblemService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 11:06
 * @Description:
 */
@Service
public class ProblemServiceImpl implements ProblemService {

    @Resource
    private ProblemManager problemManager;

    @Override
    public CommonResult<Page<ProblemVo>> getProblemList(Integer limit, Integer currentPage, String keyword, List<Long> tagId, Integer difficulty, String oj) {
        return CommonResult.successResponse(problemManager.getProblemList(limit, currentPage, keyword, tagId, difficulty, oj));
    }

    @Override
    public CommonResult<RandomProblemVo> getRandomProblem() {
        try {
            return CommonResult.successResponse(problemManager.getRandomProblem());
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<HashMap<Long, Object>> getUserProblemStatus(PidListDto pidListDto) {
        try {
            return CommonResult.successResponse(problemManager.getUserProblemStatus(pidListDto));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<ProblemInfoVo> getProblemInfo(String problemId, Long gid) {
        try {
            return CommonResult.successResponse(problemManager.getProblemInfo(problemId, gid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}