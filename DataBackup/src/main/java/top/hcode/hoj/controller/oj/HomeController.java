package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.vo.RPRankVo;
import top.hcode.hoj.pojo.vo.AnnouncementVo;;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.service.oj.HomeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/26 14:12
 * @Description: 处理首页的请求
 */
@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * @MethodName getRecentContest
     * @Params * @param null
     * @Description 获取最近14天的比赛信息列表
     * @Return CommonResult
     * @Since 2020/12/29
     */

    @GetMapping("/get-recent-contest")
    public CommonResult<List<ContestVo>> getRecentContest() {
        return homeService.getRecentContest();
    }


    /**
     * @MethodName getHomeCarousel
     * @Params
     * @Description 获取主页轮播图
     * @Return
     * @Since 2021/9/4
     */
    @GetMapping("/home-carousel")
    public CommonResult<List<HashMap<String, Object>>> getHomeCarousel() {
        return homeService.getHomeCarousel();
    }


    /**
     * @MethodName getRecentSevenACRank
     * @Params * @param null
     * @Description 获取最近7天用户做题榜单
     * @Return
     * @Since 2021/1/15
     */
    @GetMapping("/get-recent-seven-ac-rank")
    public CommonResult<List<ACMRankVo>> getRecentSevenACRank() {
        return homeService.getRecentSevenACRank();
    }
    /**
     * @MethodName getRPRank
     * @Params * @param null
     * @Description 获取用户rp榜单
     * @Return
     * @Since 2021/7/14
     */

    /**
     * @MethodName getRecentOtherContest
     * @Params * @param null
     * @Description 获取最近其他OJ的比赛信息列表
     * @Return CommonResult
     * @Since 2020/1/15
     */

    @GetMapping("/get-recent-other-contest")
    public CommonResult<List<HashMap<String, Object>>> getRecentOtherContest() {
        return homeService.getRecentOtherContest();
    }


    /**
     * @MethodName getCommonAnnouncement
     * @Params * @param null
     * @Description 获取主页公告列表
     * @Return CommonResult
     * @Since 2020/12/29
     */

    @GetMapping("/get-common-announcement")

    public CommonResult<IPage<AnnouncementVo>> getCommonAnnouncement(@RequestParam(value = "limit", required = false) Integer limit,
                                                                     @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return homeService.getCommonAnnouncement(limit, currentPage);
    }

    /**
     * @MethodName getWebConfig
     * @Params * @param null
     * @Description 获取网站的基础配置。例如名字，缩写名字等等。
     * @Return CommonResult
     * @Since 2020/12/29
     */

    @GetMapping("/get-website-config")
    public CommonResult<Map<Object, Object>> getWebConfig() {
        return homeService.getWebConfig();
    }

}