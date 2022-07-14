package top.hcode.hoj.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: tuxiaobei
 * @Date: 2022/07/15 0:38
 * @Description:
 */
@Data
@AllArgsConstructor
public class ProblemInfoVo {
    private Problem problem;
    private List<Tag> tags;
    private List<String> languages;
    private ProblemCountVo problemCount;
    private HashMap<String, String> codeTemplate;
    private List<String> file_names;
}