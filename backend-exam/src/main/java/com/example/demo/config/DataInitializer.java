package com.example.demo.config;

import com.example.demo.entity.Category;
import com.example.demo.entity.Question;
import com.example.demo.entity.SysUser;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.QuestionMapper;
import com.example.demo.mapper.SysUserMapper;
import com.example.demo.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动时初始化种子数据（账号 / 分类 / 样例题目）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initCategoriesAndQuestions();
    }

    private void initUsers() {
        if (userMapper.findByUsername("admin") != null) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("管理员");
        admin.setRole(0);
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        userMapper.insert(admin);

        SysUser student = new SysUser();
        student.setUsername("student");
        student.setPassword(passwordEncoder.encode("123456"));
        student.setNickname("学生");
        student.setRole(1);
        student.setStatus(1);
        student.setCreateTime(LocalDateTime.now());
        userMapper.insert(student);
    }

    private void initCategoriesAndQuestions() {
        if (!categoryMapper.selectAll().isEmpty()) {
            return;
        }
        String[] names = {"言语理解", "数量关系", "判断推理", "资料分析"};
        Long[] catIds = new Long[names.length];
        for (int i = 0; i < names.length; i++) {
            Category c = new Category();
            c.setName(names[i]);
            c.setParentId(0L);
            c.setSortOrder(i);
            c.setCreateTime(LocalDateTime.now());
            categoryMapper.insert(c);
            catIds[i] = c.getId();
        }

        // 言语理解
        addQuestion(catIds[0], "下列词语书写完全正确的一项是：", new String[]{"川流不息", "再接再励", "世外桃园", "按步就班"}, "A", "其他三项正确写法：再接再厉、世外桃源、按部就班。", 1);
        addQuestion(catIds[0], "成语“呕心沥血”用来形容：", new String[]{"费尽心思", "心情愉快", "身体虚弱", "目光短浅"}, "A", "呕心沥血比喻费尽心思、耗尽心血。", 2);
        addQuestion(catIds[0], "下列句子没有语病的一项是：", new String[]{"通过这次活动，使我开阔了眼界", "我们要养成认真学习的好习惯", "能否刻苦学习是取得好成绩的关键", "他大概用了大约一小时"}, "B", "A 成分残缺，C 两面对一面，D 语义重复。", 3);

        // 数量关系
        addQuestion(catIds[1], "数列 1, 3, 5, 7, 9, ( ) 的下一项是：", new String[]{"10", "11", "12", "13"}, "B", "奇数数列，公差为 2。", 1);
        addQuestion(catIds[1], "某商品原价 100 元，打八折后售价为：", new String[]{"80 元", "90 元", "75 元", "85 元"}, "A", "100 × 0.8 = 80。", 2);
        addQuestion(catIds[1], "甲乙相向而行，甲 3km/h，乙 2km/h，相距 10km，多久相遇：", new String[]{"1 小时", "2 小时", "3 小时", "4 小时"}, "B", "10 ÷ (3 + 2) = 2 小时。", 3);

        // 判断推理
        addQuestion(catIds[2], "“所有猫都是动物，这只猫是动物”属于：", new String[]{"演绎推理", "归纳推理", "类比推理", "以上都不是"}, "A", "从一般到个别，属于演绎推理。", 1);
        addQuestion(catIds[2], "如果明天下雨，我就不出门。明天没下雨，那么我：", new String[]{"一定出门", "一定不出门", "无法确定", "以上都不对"}, "C", "充分条件否定前件，结论不确定。", 2);
        addQuestion(catIds[2], "甲说乙说谎，乙说丙说谎，丙说甲乙都在说谎，说真话的是：", new String[]{"甲", "乙", "丙", "无法确定"}, "B", "假设法可推出乙说真话。", 3);

        // 资料分析
        addQuestion(catIds[3], "2019 年 GDP 1000 亿，2020 年增长 10%，则 2020 年 GDP 为：", new String[]{"1100 亿", "1000 亿", "1010 亿", "1200 亿"}, "A", "1000 × (1 + 10%) = 1100。", 1);
        addQuestion(catIds[3], "增速由 20% 下降到 10%，下降了多少个百分点：", new String[]{"10 个百分点", "50 个百分点", "10%", "5 个百分点"}, "A", "百分点直接相减：20 - 10 = 10。", 2);
        addQuestion(catIds[3], "2019 年人口 100 万，2020 年 105 万，人口增长率约为：", new String[]{"5%", "4.76%", "5.26%", "10%"}, "A", "(105 - 100) ÷ 100 = 5%。", 3);
    }

    private void addQuestion(Long categoryId, String stem, String[] opts, String answer, String analysis, int difficulty) {
        String[] letters = {"A", "B", "C", "D"};
        List<String> options = new ArrayList<>();
        for (int i = 0; i < opts.length; i++) {
            options.add(letters[i] + ". " + opts[i]);
        }
        Question q = new Question();
        q.setCategoryId(categoryId);
        q.setStem(stem);
        q.setOptions(JsonUtil.toJson(options));
        q.setAnswer(answer);
        q.setAnalysis(analysis);
        q.setDifficulty(difficulty);
        q.setViewCount(0);
        q.setType(0);
        q.setCreateTime(LocalDateTime.now());
        questionMapper.insert(q);
    }
}
