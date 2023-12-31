package top.hcode.hoj.dao.judge;


import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.judge.Judge;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.ProblemCountVo;
import top.hcode.hoj.pojo.vo.RpChangeVo;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */

public interface JudgeEntityService extends IService<Judge> {

    IPage<JudgeVo> getCommonJudgeList(Integer limit,
                                      Integer currentPage,
                                      String searchPid,
                                      Integer status,
                                      String username,
                                      String uid,
                                      Boolean completeProblemID,
                                      Long gid);

    IPage<JudgeVo> getContestJudgeList(Integer limit,
                                       Integer currentPage,
                                       String displayId,
                                       Long cid,
                                       Integer status,
                                       String username,
                                       String uid,
                                       Boolean beforeContestSubmit,
                                       String rule,
                                       Date startTime,
                                       Date sealRankTime,
                                       String sealTimeUid,
                                       Boolean completeProblemID);
    public IPage<RpChangeVo> getRpChangeList(Integer limit,
                                             Integer currentPage,
                                             String searchUsername,
                                             String Id,
                                             String username,
                                             String uid,
                                             Integer rpChange,
                                             String description);

    void failToUseRedisPublishJudge(Long submitId, Long pid, Boolean isContest);

    ProblemCountVo getContestProblemCount(Long pid,
                                          Long cpid,
                                          Long cid,
                                          Date startTime,
                                          Date sealRankTime,
                                          List<String> adminList);

    ProblemCountVo getProblemCount(Long pid, Long gid);

    public int getTodayJudgeNum();

    public List<ProblemCountVo> getProblemListCount(List<Long> pidList);
}
