package top.hcode.hoj.manager.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.models.auth.In;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.pojo.entity.group.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.ContestValidator;
import top.hcode.hoj.validator.GroupValidator;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 20:11
 * @Description:
 */
@Component
public class ContestCalculateRankManager {

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ContestRecordEntityService contestRecordEntityService;

    @Resource
    private ContestProblemEntityService contestProblemEntityService;


    @Autowired
    private GroupMemberEntityService groupMemberEntityService;

    @Autowired
    private ContestValidator contestValidator;

    @Autowired
    private GroupValidator groupValidator;

    public List<ACMContestRankVo> calcACMRank(boolean isOpenSealRank,
                                              boolean removeStar,
                                              Contest contest,
                                              String currentUserId,
                                              List<String> concernedList,
                                              List<Integer> externalCidList) {
        return calcACMRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                false,
                null);
    }


    public List<OIContestRankVo> calcOIRank(Boolean isOpenSealRank,
                                            Boolean removeStar,
                                            Contest contest,
                                            String currentUserId,
                                            List<String> concernedList,
                                            List<Integer> externalCidList) {

        return calcOIRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                externalCidList,
                false,
                null);
    }


    /**
     * @param isOpenSealRank 是否是查询封榜后的数据
     * @param removeStar     是否需要移除打星队伍
     * @param contest        比赛实体信息
     * @param currentUserId  当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList  关注的用户（uuid）列表
     * @param externalCidList 榜单额外显示的比赛列表
     * @param useCache       是否对初始排序计算的结果进行缓存
     * @param cacheTime      缓存的时间 单位秒
     * @MethodName calcACMRank
     * @Description TODO
     * @Return
     * @Since 2021/12/10
     */
    public List<ACMContestRankVo> calcACMRank(boolean isOpenSealRank,
                                              boolean removeStar,
                                              Contest contest,
                                              String currentUserId,
                                              List<String> concernedList,
                                              List<Integer> externalCidList,
                                              boolean useCache,
                                              Long cacheTime) {
        List<ACMContestRankVo> orderResultList;
        if (useCache) {
            String key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_" + contest.getId();
            orderResultList = (List<ACMContestRankVo>) redisUtils.get(key);
            if (orderResultList == null) {
                if (isOpenSealRank) {
                    long minSealRankTime = DateUtil.between(contest.getStartTime(), contest.getSealRankTime(), DateUnit.SECOND);
                    orderResultList = getACMOrderRank(contest, true, minSealRankTime, contest.getDuration(), externalCidList);
                } else {
                    orderResultList = getACMOrderRank(contest, false, null, null, externalCidList);
                }
                redisUtils.set(key, orderResultList, cacheTime);
            }
        } else {
            if (isOpenSealRank) {
                long minSealRankTime = DateUtil.between(contest.getStartTime(), contest.getSealRankTime(), DateUnit.SECOND);
                orderResultList = getACMOrderRank(contest, true, minSealRankTime, contest.getDuration(), externalCidList);
            } else {
                orderResultList = getACMOrderRank(contest, false, null, null, externalCidList);
            }
        }

        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
        }
        // 记录当前用户排名数据和关注列表的用户排名数据
        List<ACMContestRankVo> topACMRankVoList = new ArrayList<>();
        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        int rankNum = 1;
        int len = orderResultList.size();
        ACMContestRankVo lastACMRankVo = null;
        for (int i = 0; i < len; i++) {
            ACMContestRankVo currentACMRankVo = orderResultList.get(i);
            if (starAccountMap.containsKey(currentACMRankVo.getUsername())) {
                // 打星队伍排名为-1
                currentACMRankVo.setRank(-1);
            } else {
                if (rankNum == 1) {
                    currentACMRankVo.setRank(rankNum);
                } else {
                    // 当前用户的总罚时和AC数跟前一个用户一样的话，同时前一个不应该为打星，排名则一样
                    if (lastACMRankVo.getAc().equals(currentACMRankVo.getAc())
                            && lastACMRankVo.getTotalTime().equals(currentACMRankVo.getTotalTime())) {
                        currentACMRankVo.setRank(lastACMRankVo.getRank());
                    } else {
                        currentACMRankVo.setRank(rankNum);
                    }
                }
                lastACMRankVo = currentACMRankVo;
                rankNum++;
            }

            if (!StringUtils.isEmpty(currentUserId) &&
                    currentACMRankVo.getUid().equals(currentUserId)) {
                topACMRankVoList.add(currentACMRankVo);
            }

            // 需要添加关注用户
            if (needAddConcernedUser) {
                if (concernedList.contains(currentACMRankVo.getUid())) {
                    topACMRankVoList.add(currentACMRankVo);
                }
            }
        }
        topACMRankVoList.addAll(orderResultList);
        return topACMRankVoList;
    }


    private List<ACMContestRankVo> getACMOrderRank(Contest contest,
                                                   Boolean isOpenSealRank,
                                                   Long minSealRankTime,
                                                   Long maxSealRankTime,
                                                   List<Integer> externalCidList) {


        List<ContestRecordVo> contestRecordList = contestRecordEntityService.getACMContestRecord(contest.getUid(),
                contest.getId(),
                externalCidList,
                contest.getStartTime());

        List<String> superAdminUidList = getSuperAdminUidList(contest.getGid());

        List<ACMContestRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        int index = 0;

        HashMap<String, Long> firstACMap = new HashMap<>();

        for (ContestRecordVo contestRecord : contestRecordList) {

            if (superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员的提交不入排行榜
                continue;
            }

            ACMContestRankVo ACMContestRankVo;
            if (!uidMapIndex.containsKey(contestRecord.getUid())) { // 如果该用户信息没还记录

                // 初始化参数
                ACMContestRankVo = new ACMContestRankVo();
                ACMContestRankVo.setRealname(contestRecord.getRealname())
                        .setAvatar(contestRecord.getAvatar())
                        .setSchool(contestRecord.getSchool())
                        .setGender(contestRecord.getGender())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setNickname(contestRecord.getNickname())
                        .setAc(0)
                        .setTotalTime(0L)
                        .setTotal(0);

                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                ACMContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(ACMContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                ACMContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid())); // 根据记录的index进行获取
            }

            HashMap<String, Object> problemSubmissionInfo = ACMContestRankVo.getSubmissionInfo().get(contestRecord.getDisplayId());

            if (problemSubmissionInfo == null) {
                problemSubmissionInfo = new HashMap<>();
                problemSubmissionInfo.put("errorNum", 0);
            }

            ACMContestRankVo.setTotal(ACMContestRankVo.getTotal() + 1);

            // 如果是当前是开启封榜的时段和同时该提交是处于封榜时段 尝试次数+1
            if (isOpenSealRank && isInSealTimeSubmission(minSealRankTime, maxSealRankTime, contestRecord.getTime())) {

                int tryNum = (int) problemSubmissionInfo.getOrDefault("tryNum", 0);
                problemSubmissionInfo.put("tryNum", tryNum + 1);

            } else {

                // 如果该题目已经AC过了，其它都不记录了
                if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                    continue;
                }

                // 记录已经按题目提交耗时time升序了

                // 通过的话
                if (contestRecord.getStatus().intValue() == Constants.Contest.RECORD_AC.getCode()) {
                    // 总解决题目次数ac+1
                    ACMContestRankVo.setAc(ACMContestRankVo.getAc() + 1);

                    // 判断是不是first AC
                    boolean isFirstAC = false;
                    Long time = firstACMap.getOrDefault(contestRecord.getDisplayId(), null);
                    if (time == null) {
                        isFirstAC = true;
                        firstACMap.put(contestRecord.getDisplayId(), contestRecord.getTime());
                    } else {
                        // 相同提交时间也是first AC
                        if (time.longValue() == contestRecord.getTime().longValue()) {
                            isFirstAC = true;
                        }
                    }

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("isAC", true);
                    problemSubmissionInfo.put("isFirstAC", isFirstAC);
                    problemSubmissionInfo.put("ACTime", contestRecord.getTime());
                    problemSubmissionInfo.put("errorNum", errorNumber);

                    // 同时计算总耗时，总耗时加上 该题目未AC前的错误次数*20*60+题目AC耗时
                    ACMContestRankVo.setTotalTime(ACMContestRankVo.getTotalTime() + errorNumber * 20 * 60 + contestRecord.getTime());

                    // 未通过同时需要记录罚时次数
                } else if (contestRecord.getStatus().intValue() == Constants.Contest.RECORD_NOT_AC_PENALTY.getCode()) {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber + 1);
                } else {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber);
                }
            }
            ACMContestRankVo.getSubmissionInfo().put(contestRecord.getDisplayId(), problemSubmissionInfo);
        }

        List<ACMContestRankVo> orderResultList = result.stream().sorted(Comparator.comparing(ACMContestRankVo::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                .thenComparing(ACMContestRankVo::getTotalTime) //再以总耗时升序
        ).collect(Collectors.toList());

        return orderResultList;
    }


    /**
     * @param isOpenSealRank 是否是查询封榜后的数据
     * @param removeStar     是否需要移除打星队伍
     * @param contest        比赛实体信息
     * @param currentUserId  当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList  关注的用户（uuid）列表
     * @param externalCidList 榜单额外显示比赛列表
     * @param useCache       是否对初始排序计算的结果进行缓存
     * @param cacheTime      缓存的时间 单位秒
     * @MethodName calcOIRank
     * @Description TODO
     * @Return
     * @Since 2021/12/10
     */
    public List<OIContestRankVo> calcOIRank(boolean isOpenSealRank,
                                            boolean removeStar,
                                            Contest contest,
                                            String currentUserId,
                                            List<String> concernedList,
                                            List<Integer> externalCidList,
                                            boolean useCache,
                                            Long cacheTime) {

        List<OIContestRankVo> orderResultList;
        if (useCache) {
            String key = Constants.Contest.CONTEST_RANK_CAL_RESULT_CACHE.getName() + "_" + contest.getId();
            orderResultList = (List<OIContestRankVo>) redisUtils.get(key);
            if (orderResultList == null) {
                orderResultList = getOIOrderRank(contest,externalCidList, isOpenSealRank);
                redisUtils.set(key, orderResultList, cacheTime);
            }
        } else {
            orderResultList = getOIOrderRank(contest,externalCidList, isOpenSealRank);
        }

        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
        }

        // 记录当前用户排名数据和关注列表的用户排名数据
        List<OIContestRankVo> topOIRankVoList = new ArrayList<>();
        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        int rankNum = 1;
        OIContestRankVo lastOIRankVo = null;
        int len = orderResultList.size();
        for (int i = 0; i < len; i++) {
            OIContestRankVo currentOIRankVo = orderResultList.get(i);
            if (starAccountMap.containsKey(currentOIRankVo.getUsername())) {
                // 打星队伍排名为-1
                currentOIRankVo.setRank(-1);
            } else {
                if (rankNum == 1) {
                    currentOIRankVo.setRank(rankNum);
                } else {
                    // 当前用户的程序总运行时间和总得分跟前一个用户一样的话，同时前一个不应该为打星用户，排名则一样
                    if (lastOIRankVo.getTotalScore().equals(currentOIRankVo.getTotalScore())
                            && lastOIRankVo.getTotalTime().equals(currentOIRankVo.getTotalTime())) {
                        currentOIRankVo.setRank(lastOIRankVo.getRank());
                    } else {
                        currentOIRankVo.setRank(rankNum);
                    }
                }
                lastOIRankVo = currentOIRankVo;
                rankNum++;
            }

            if (!StringUtils.isEmpty(currentUserId) &&
                    currentOIRankVo.getUid().equals(currentUserId)) {
                topOIRankVoList.add(currentOIRankVo);
            }

            // 需要添加关注用户
            if (needAddConcernedUser) {
                if (concernedList.contains(currentOIRankVo.getUid())) {
                    topOIRankVoList.add(currentOIRankVo);
                }
            }
        }
        topOIRankVoList.addAll(orderResultList);
        return topOIRankVoList;
    }

    private List<OIContestRankVo> getOIOrderRank(Contest contest,List<Integer> externalCidList, Boolean isOpenSealRank) {

        List<ContestRecordVo> oiContestRecord = contestRecordEntityService.getOIContestRecord(contest, externalCidList, isOpenSealRank);

        List<String> superAdminUidList = getSuperAdminUidList(contest.getGid());

        List<OIContestRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> uidMapTime = new HashMap<>();

        HashMap<Long, Integer> problemScore = new HashMap<>();

        List<ContestProblemVo> contestProblemList;

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");


        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目列表，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        try {
            contestValidator.validateContestAuth(contest, userRolesVo, isRoot);
        } catch (StatusFailException e) {
            e.printStackTrace();
        } catch (StatusForbiddenException e) {
            e.printStackTrace();
        }

        boolean isAdmin = isRoot
                || contest.getAuthor().equals(userRolesVo.getUsername())
                || (contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()));

        List<String> groupRootUidList = null;
        if (contest.getIsGroup() && contest.getGid() != null) {
            groupRootUidList = groupMemberEntityService.getGroupRootUidList(contest.getGid());
        }

        if (contestValidator.isSealRank(userRolesVo.getUid(), contest, true, isRoot)) {
            contestProblemList = contestProblemEntityService.getContestProblemList(contest.getId(),
                    contest.getStartTime(),
                    contest.getEndTime(),
                    contest.getSealRankTime(),
                    isAdmin,
                    contest.getAuthor(),
                    groupRootUidList);
        } else {
            contestProblemList = contestProblemEntityService.getContestProblemList(contest.getId(),
                    contest.getStartTime(),
                    contest.getEndTime(),
                    null,
                    isAdmin,
                    contest.getAuthor(),
                    groupRootUidList);
        }

        for (ContestProblemVo p : contestProblemList) {
            problemScore.put(p.getPid(), p.getCurScore());
        }

        boolean isHighestRankScore = Constants.Contest.OI_RANK_HIGHEST_SCORE.getName().equals(contest.getOiRankScoreType());

        int index = 0;

        for (ContestRecordVo contestRecord : oiContestRecord) {

            if (superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员的提交不入排行榜
                continue;
            }

            if (contestRecord.getStatus().equals(Constants.Contest.RECORD_AC.getCode())) { // AC
                HashMap<String, Integer> pidMapTime = uidMapTime.get(contestRecord.getUid());
                if (pidMapTime != null) {
                    Integer useTime = pidMapTime.get(contestRecord.getDisplayId());
                    if (useTime != null) {
                        if (useTime > contestRecord.getUseTime()) {  // 如果时间消耗比原来的少
                            pidMapTime.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                        }
                    } else {
                        pidMapTime.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                    }
                } else {
                    HashMap<String, Integer> tmp = new HashMap<>();
                    tmp.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                    uidMapTime.put(contestRecord.getUid(), tmp);
                }
            }

            OIContestRankVo oiContestRankVo;
            if (!uidMapIndex.containsKey(contestRecord.getUid())) { // 如果该用户信息没还记录
                // 初始化参数
                oiContestRankVo = new OIContestRankVo();
                oiContestRankVo.setRealname(contestRecord.getRealname())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setSchool(contestRecord.getSchool())
                        .setAvatar(contestRecord.getAvatar())
                        .setGender(contestRecord.getGender())
                        .setNickname(contestRecord.getNickname())
                        .setTotalScore(0);


                HashMap<String, Integer> submissionInfo = new HashMap<>();
                oiContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(oiContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                oiContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid())); // 根据记录的index进行获取
            }

            // 记录总分
            HashMap<String, Integer> submissionInfo = oiContestRankVo.getSubmissionInfo();
            Integer score = submissionInfo.get(contestRecord.getDisplayId());


//            int cur_AC = 0;
//            int total_score = 0;
//            for (ContestRecordVo curcontestRecord : oiContestRecord) {
//                if (curcontestRecord.getDisplayId().equals(contestRecord.getDisplayId())) {
//                    if (curcontestRecord.getStatus().equals(Constants.Contest.RECORD_AC.getCode())) {
//                        total_score += curcontestRecord.getScore();
//                    }
//                }
//            }
//            System.out.println("cur_AC: " + cur_AC);
//            cur_AC = 1;
//            if (contest.getType() != Constants.Contest.TYPE_CTF.getCode()) {
//                cur_AC = 1; // 如果不是CTF比赛，则不动态积分。
//            }
            int cur_score = problemScore.get(contestRecord.getPid());
            if (isHighestRankScore) {
                if (score == null) {
                    oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() + contestRecord.getScore());
                    submissionInfo.put(contestRecord.getDisplayId(), (int) Math.ceil((contestRecord.getScore() / 100.0) * cur_score));
                }
            } else
            {
                if (contestRecord.getScore() != null) {
                    if (score != null) { // 为了避免同个提交时间的重复计算
                        oiContestRankVo.setTotalScore((int) Math.ceil(oiContestRankVo.getTotalScore() - score + (contestRecord.getScore() / 100.0) * cur_score));
                    } else {
                        oiContestRankVo.setTotalScore((int) Math.ceil(oiContestRankVo.getTotalScore() + (contestRecord.getScore() / 100.0) * cur_score));
                    }
                }
                submissionInfo.put(contestRecord.getDisplayId(), (int) Math.ceil(oiContestRankVo.getTotalScore() + (contestRecord.getScore() / 100.0) * cur_score));
            }

        }


        for (OIContestRankVo oiContestRankVo : result) {
            HashMap<String, Integer> pidMapTime = uidMapTime.get(oiContestRankVo.getUid());
            int sumTime = 0;
            if (pidMapTime != null) {
                for (String key : pidMapTime.keySet()) {
                    Integer time = pidMapTime.get(key);
                    sumTime += time == null ? 0 : time;
                }
            }
            oiContestRankVo.setTotalTime(sumTime);
            oiContestRankVo.setTimeInfo(pidMapTime);
        }

        // 根据总得分进行降序,再根据总时耗升序排序
        List<OIContestRankVo> orderResultList = result.stream()
                .sorted(Comparator.comparing(OIContestRankVo::getTotalScore, Comparator.reverseOrder())
                        .thenComparing(OIContestRankVo::getTotalTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());
        return orderResultList;
    }


    private List<String> getSuperAdminUidList(Long gid) {

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();

        if (gid != null) {
            QueryWrapper<GroupMember> groupMemberQueryWrapper = new QueryWrapper<>();
            groupMemberQueryWrapper.eq("gid", gid).eq("auth", 5);

            List<GroupMember> groupRootList = groupMemberEntityService.list(groupMemberQueryWrapper);

            for (GroupMember groupMember : groupRootList) {
                superAdminUidList.add(groupMember.getUid());
            }
        }
        return superAdminUidList;
    }

    private boolean isInSealTimeSubmission(Long minSealRankTime, Long maxSealRankTime, Long time) {
        return time >= minSealRankTime && time <= maxSealRankTime;
    }

    private HashMap<String, Boolean> starAccountToMap(String starAccountStr) {
        if (StringUtils.isEmpty(starAccountStr)) {
            return new HashMap<>();
        }
        JSONObject jsonObject = JSONUtil.parseObj(starAccountStr);
        List<String> list = jsonObject.get("star_account", List.class);
        HashMap<String, Boolean> res = new HashMap<>();
        for (String str : list) {
            if (!StringUtils.isEmpty(str)) {
                res.put(str, true);
            }
        }
        return res;
    }
}