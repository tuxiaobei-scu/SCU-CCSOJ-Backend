package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.entity.user.UserRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.CTFRankVo;
import top.hcode.hoj.pojo.vo.OIRankVo;
import top.hcode.hoj.pojo.vo.UserHomeVo;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface UserRecordMapper extends BaseMapper<UserRecord> {
    IPage<ACMRankVo> getACMRankList(Page<ACMRankVo> page, @Param("uidList") List<String> uidList);

    List<ACMRankVo> getRecent7ACRank();

    IPage<OIRankVo> getOIRankList(Page<OIRankVo> page, @Param("uidList") List<String> uidList);

    IPage<CTFRankVo> getCTFRankList(Page<CTFRankVo> page, @Param("uidList") List<String> uidList);

    UserHomeVo getUserHomeInfo(@Param("uid") String uid, @Param("username") String username);

    IPage<OIRankVo> getGroupRankList(Page<OIRankVo> page,
                                     @Param("gid") Long gid,
                                     @Param("uidList") List<String> uidList,
                                     @Param("rankType") String rankType);

}
