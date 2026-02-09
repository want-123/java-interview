package com.mk.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历解析工具
 * 用于解析用户上传的简历并提取关键信息
 */
@Component
public class ResumeParserTool {
    
    @Tool("解析简历，参数说明："
            + "resumeContent-简历内容，"
            + "resumeType-简历类型（如text, pdf, docx等，默认为text）")
    public String parseResume(String resumeContent, String resumeType) {
        if (resumeType == null) {
            resumeType = "text";
        }
        
        System.out.println("------------------------执行Tools:简历解析----------------");
        
        // 1. 提取个人基本信息
        PersonalInfo personalInfo = extractPersonalInfo(resumeContent);
        
        // 2. 提取教育背景
        List<Education> educations = extractEducation(resumeContent);
        
        // 3. 提取工作经历
        List<WorkExperience> workExperiences = extractWorkExperience(resumeContent);
        
        // 4. 提取项目经验
        List<ProjectExperience> projectExperiences = extractProjectExperience(resumeContent);
        
        // 5. 提取技能
        List<String> skills = extractSkills(resumeContent);
        
        // 6. 生成解析报告
        StringBuilder report = new StringBuilder();
        report.append("# 简历解析报告\n\n");
        
        // 个人基本信息
        report.append("## 个人基本信息\n");
        report.append("- 姓名：").append(personalInfo.getName()).append("\n");
        report.append("- 电话：").append(personalInfo.getPhone()).append("\n");
        report.append("- 邮箱：").append(personalInfo.getEmail()).append("\n");
        report.append("- 地址：").append(personalInfo.getAddress()).append("\n\n");
        
        // 教育背景
        report.append("## 教育背景\n");
        if (educations.isEmpty()) {
            report.append("未提取到教育背景信息\n\n");
        } else {
            for (Education education : educations) {
                report.append("- 学校：").append(education.getSchool()).append("\n");
                report.append("  专业：").append(education.getMajor()).append("\n");
                report.append("  学历：").append(education.getDegree()).append("\n");
                report.append("  时间：").append(education.getStartDate()).append(" - ").append(education.getEndDate()).append("\n\n");
            }
        }
        
        // 工作经历
        report.append("## 工作经历\n");
        if (workExperiences.isEmpty()) {
            report.append("未提取到工作经历信息\n\n");
        } else {
            for (WorkExperience workExperience : workExperiences) {
                report.append("- 公司：").append(workExperience.getCompany()).append("\n");
                report.append("  职位：").append(workExperience.getPosition()).append("\n");
                report.append("  时间：").append(workExperience.getStartDate()).append(" - ").append(workExperience.getEndDate()).append("\n");
                report.append("  职责：").append(workExperience.getDescription()).append("\n\n");
            }
        }
        
        // 项目经验
        report.append("## 项目经验\n");
        if (projectExperiences.isEmpty()) {
            report.append("未提取到项目经验信息\n\n");
        } else {
            for (ProjectExperience projectExperience : projectExperiences) {
                report.append("- 项目名称：").append(projectExperience.getProjectName()).append("\n");
                report.append("  时间：").append(projectExperience.getStartDate()).append(" - ").append(projectExperience.getEndDate()).append("\n");
                report.append("  职责：").append(projectExperience.getDescription()).append("\n\n");
            }
        }
        
        // 技能
        report.append("## 技能\n");
        if (skills.isEmpty()) {
            report.append("未提取到技能信息\n\n");
        } else {
            for (String skill : skills) {
                report.append("- " + skill + "\n");
            }
            report.append("\n");
        }
        
        // 生成技能评估
        String skillEvaluation = evaluateSkills(skills);
        report.append("## 技能评估\n").append(skillEvaluation).append("\n");
        
        return report.toString();
    }
    
    /**
     * 提取个人基本信息
     */
    private PersonalInfo extractPersonalInfo(String resumeContent) {
        PersonalInfo info = new PersonalInfo();
        
        // 提取姓名
        Pattern namePattern = Pattern.compile("姓名[：:]\s*([^\n]+)");
        Matcher nameMatcher = namePattern.matcher(resumeContent);
        if (nameMatcher.find()) {
            info.setName(nameMatcher.group(1).trim());
        }
        
        // 提取电话
        Pattern phonePattern = Pattern.compile("1[3-9]\\d{9}");
        Matcher phoneMatcher = phonePattern.matcher(resumeContent);
        if (phoneMatcher.find()) {
            info.setPhone(phoneMatcher.group(0));
        }
        
        // 提取邮箱
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(resumeContent);
        if (emailMatcher.find()) {
            info.setEmail(emailMatcher.group(0));
        }
        
        // 提取地址
        Pattern addressPattern = Pattern.compile("地址[：:]\s*([^\n]+)");
        Matcher addressMatcher = addressPattern.matcher(resumeContent);
        if (addressMatcher.find()) {
            info.setAddress(addressMatcher.group(1).trim());
        }
        
        return info;
    }
    
    /**
     * 提取教育背景
     */
    private List<Education> extractEducation(String resumeContent) {
        List<Education> educations = new ArrayList<>();
        
        // 简单的教育背景提取逻辑
        Pattern eduPattern = Pattern.compile("(.*大学|.*学院).*?(\\d{4}年.*?)(\\d{4}年.*?)", Pattern.DOTALL);
        Matcher eduMatcher = eduPattern.matcher(resumeContent);
        
        while (eduMatcher.find()) {
            Education education = new Education();
            education.setSchool(eduMatcher.group(1).trim());
            education.setStartDate(eduMatcher.group(2).trim());
            education.setEndDate(eduMatcher.group(3).trim());
            educations.add(education);
        }
        
        return educations;
    }
    
    /**
     * 提取工作经历
     */
    private List<WorkExperience> extractWorkExperience(String resumeContent) {
        List<WorkExperience> workExperiences = new ArrayList<>();
        
        // 简单的工作经历提取逻辑
        Pattern workPattern = Pattern.compile("(.*公司|.*企业|.*集团).*?(\\d{4}年.*?)(\\d{4}年.*?)([\\s\\S]*?)(?=\\d{4}年|$)", Pattern.DOTALL);
        Matcher workMatcher = workPattern.matcher(resumeContent);
        
        while (workMatcher.find()) {
            WorkExperience workExperience = new WorkExperience();
            workExperience.setCompany(workMatcher.group(1).trim());
            workExperience.setStartDate(workMatcher.group(2).trim());
            workExperience.setEndDate(workMatcher.group(3).trim());
            workExperience.setDescription(workMatcher.group(4).trim());
            workExperiences.add(workExperience);
        }
        
        return workExperiences;
    }
    
    /**
     * 提取项目经验
     */
    private List<ProjectExperience> extractProjectExperience(String resumeContent) {
        List<ProjectExperience> projectExperiences = new ArrayList<>();
        
        // 简单的项目经验提取逻辑
        Pattern projectPattern = Pattern.compile("项目名称[：:]\\s*([^\\n]+).*?时间[：:]\\s*([^\\n]+).*?职责[：:]([\\s\\S]*?)(?=项目名称|$)", Pattern.DOTALL);
        Matcher projectMatcher = projectPattern.matcher(resumeContent);
        
        while (projectMatcher.find()) {
            ProjectExperience projectExperience = new ProjectExperience();
            projectExperience.setProjectName(projectMatcher.group(1).trim());
            projectExperience.setStartDate(projectMatcher.group(2).trim());
            projectExperience.setEndDate(projectMatcher.group(2).trim()); // 简化处理
            projectExperience.setDescription(projectMatcher.group(3).trim());
            projectExperiences.add(projectExperience);
        }
        
        return projectExperiences;
    }
    
    /**
     * 提取技能
     */
    private List<String> extractSkills(String resumeContent) {
        List<String> skills = new ArrayList<>();
        
        // 常见技能关键词
        String[] commonSkills = {
            "Java", "Python", "C++", "JavaScript", "React", "Vue", "Spring", "MySQL", "Oracle", "Redis",
            "MongoDB", "Docker", "Kubernetes", "Git", "Linux", "AWS", "Azure", "GCP", "Spring Boot",
            "MyBatis", "Hibernate", "Spring Cloud", "微服务", "分布式", "高并发", "性能优化", "安全",
            "前端", "后端", "全栈", "移动端", "iOS", "Android", "Flutter", "React Native", "Node.js",
            "Express", "Koa", "Nest.js", "TypeScript", "HTML", "CSS", "Sass", "Less", "Webpack",
            "Vite", "Jest", "Mocha", "单元测试", "集成测试", "TDD", "BDD", "敏捷开发", "Scrum",
            "Kanban", "DevOps", "CI/CD", "Jenkins", "GitHub Actions", "GitLab CI", "SonarQube",
            "Jira", "Confluence", "Maven", "Gradle", "NPM", "Yarn", "PNPM", "Ant", "Make"
        };
        
        for (String skill : commonSkills) {
            if (resumeContent.contains(skill)) {
                skills.add(skill);
            }
        }
        
        return skills;
    }
    
    /**
     * 评估技能
     */
    private String evaluateSkills(List<String> skills) {
        if (skills.isEmpty()) {
            return "未提取到技能信息，无法进行评估。";
        }
        
        // 分类技能
        List<String> languages = new ArrayList<>();
        List<String> frameworks = new ArrayList<>();
        List<String> databases = new ArrayList<>();
        List<String> devOps = new ArrayList<>();
        List<String> other = new ArrayList<>();
        
        for (String skill : skills) {
            if (skill.matches("Java|Python|C\\+\\+|JavaScript|TypeScript|HTML|CSS")) {
                languages.add(skill);
            } else if (skill.matches("React|Vue|Spring|Spring Boot|MyBatis|Hibernate|Spring Cloud|Express|Koa|Nest.js|Flutter|React Native|Sass|Less|Webpack|Vite")) {
                frameworks.add(skill);
            } else if (skill.matches("MySQL|Oracle|Redis|MongoDB")) {
                databases.add(skill);
            } else if (skill.matches("Docker|Kubernetes|Git|Linux|AWS|Azure|GCP|Jenkins|GitHub Actions|GitLab CI|SonarQube|CI/CD|DevOps")) {
                devOps.add(skill);
            } else {
                other.add(skill);
            }
        }
        
        StringBuilder evaluation = new StringBuilder();
        
        evaluation.append("技能评估：\n");
        evaluation.append("- 编程语言：").append(languages.isEmpty() ? "无" : String.join(", ", languages)).append("\n");
        evaluation.append("- 框架：").append(frameworks.isEmpty() ? "无" : String.join(", ", frameworks)).append("\n");
        evaluation.append("- 数据库：").append(databases.isEmpty() ? "无" : String.join(", ", databases)).append("\n");
        evaluation.append("- DevOps工具：").append(devOps.isEmpty() ? "无" : String.join(", ", devOps)).append("\n");
        evaluation.append("- 其他技能：").append(other.isEmpty() ? "无" : String.join(", ", other)).append("\n\n");
        
        // 生成技能匹配建议
        if (languages.size() >= 2 && frameworks.size() >= 2 && databases.size() >= 1) {
            evaluation.append("技能匹配建议：您的技能组合较为全面，适合全栈开发或相关岗位。\n");
        } else if (languages.size() >= 2 && frameworks.size() >= 2) {
            evaluation.append("技能匹配建议：您的编程和框架技能较为突出，适合前端或后端开发岗位。\n");
        } else if (devOps.size() >= 3) {
            evaluation.append("技能匹配建议：您的DevOps技能较为突出，适合DevOps或运维相关岗位。\n");
        } else {
            evaluation.append("技能匹配建议：建议您进一步提升技能广度和深度，以增强竞争力。\n");
        }
        
        return evaluation.toString();
    }
    
    /**
     * 个人基本信息类
     */
    private static class PersonalInfo {
        private String name = "未知";
        private String phone = "未知";
        private String email = "未知";
        private String address = "未知";
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    /**
     * 教育背景类
     */
    private static class Education {
        private String school = "未知";
        private String major = "未知";
        private String degree = "未知";
        private String startDate = "未知";
        private String endDate = "未知";
        
        public String getSchool() { return school; }
        public void setSchool(String school) { this.school = school; }
        public String getMajor() { return major; }
        public void setMajor(String major) { this.major = major; }
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }
    
    /**
     * 工作经历类
     */
    private static class WorkExperience {
        private String company = "未知";
        private String position = "未知";
        private String startDate = "未知";
        private String endDate = "未知";
        private String description = "未知";
        
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 项目经验类
     */
    private static class ProjectExperience {
        private String projectName = "未知";
        private String startDate = "未知";
        private String endDate = "未知";
        private String description = "未知";
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
