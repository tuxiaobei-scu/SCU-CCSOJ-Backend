package top.hcode.hoj.manager.msg;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.SysMsgVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.dao.msg.AdminSysNoticeEntityService;
import top.hcode.hoj.dao.msg.UserSysNoticeEntityService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 11:37
 * @Description:
 */
@Component
public class NoticeManager {

    @Resource
    private UserSysNoticeEntityService userSysNoticeEntityService;

    @Resource
    private AdminSysNoticeEntityService adminSysNoticeEntityService;

    @Resource
    private ApplicationContext applicationContext;

    public IPage<SysMsgVo> getSysNotice(Integer limit,Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        IPage<SysMsgVo> sysNotice = userSysNoticeEntityService.getSysNotice(limit, currentPage, userRolesVo.getUid());
        applicationContext.getBean(NoticeManager.class).updateSysOrMineMsgRead(sysNotice);
        return sysNotice;
    }



    public IPage<SysMsgVo> getMineNotice(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        IPage<SysMsgVo> mineNotice = userSysNoticeEntityService.getMineNotice(limit, currentPage, userRolesVo.getUid());
        applicationContext.getBean(NoticeManager.class).updateSysOrMineMsgRead(mineNotice);
        return mineNotice;
    }


    @Async
    public void updateSysOrMineMsgRead(IPage<SysMsgVo> userMsgList) {
        List<Long> idList = userMsgList.getRecords().stream()
                .filter(userMsgVo -> !userMsgVo.getState())
                .map(SysMsgVo::getId)
                .collect(Collectors.toList());
        if (idList.size() == 0) {
            return;
        }
        UpdateWrapper<UserSysNotice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", idList)
                .set("state", true);
        userSysNoticeEntityService.update(null, updateWrapper);
    }

    @Async
    public void syncNoticeToNewRegisterUser(String uid) {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper
                .eq("type", "All")
                .le("gmt_create", new Date())
                .eq("state", true);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeEntityService.list(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }
        List<UserSysNotice> userSysNoticeList = new ArrayList<>();
        for (AdminSysNotice adminSysNotice : adminSysNotices) {
            UserSysNotice userSysNotice = new UserSysNotice();
            userSysNotice.setType("Sys")
                    .setSysNoticeId(adminSysNotice.getId())
                    .setRecipientId(uid);
            userSysNoticeList.add(userSysNotice);
        }
        userSysNoticeEntityService.saveOrUpdateBatch(userSysNoticeList);
    }
}