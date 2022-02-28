package com.bugaugaoshu.security.controller;


import com.bugaugaoshu.security.service.SystemDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-25 21:54
 */
@RestController
@RequestMapping("/api")
public class HomeController {
    private final SystemDataService systemDataService;

    @Autowired
    public HomeController(SystemDataService systemDataService) {
        this.systemDataService = systemDataService;
    }


    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/home")
    public Map<String, Object> home() {
        Map<String, Object> map = new HashMap<>();
        map.put("homeMessage", "# 基于Spring Security， JWT， Vue的前后端分离无状态认证Demo\n" +
                "\n" +
                "## 简介\n" +
                "\n" +
                "### 运行展示\n" +
                "\n" +
                "![主页](http://127.0.0.1:8088/images/home.jpg)\n" +
                "\n" +
                "#### 后端\n" +
                "\n" +
                "主要展示 Spring Security 与 JWT 结合使用构建后端 API 接口。\n" +
                "\n" +
                "主要功能包括登陆（如何在Spring Security中添加验证码登陆），查找，创建，删除并对用户权限进行区分等等。\n" +
                "\n" +
                "ps：由于只是 Demo，所以没有调用数据库，以上所说增删改查均在 HashMap 中完成。\n" +
                "\n" +
                "#### 前端\n" +
                "\n" +
                "展示如何使用 Vue 构建前端后与后端的配合，包括跨域的设置，前端登陆拦截\n" +
                "\n" +
                "并实现POST，GET，DELETE请求。包括如何在 Vue 中使用后端的 XSRF-TOKEN 防范 CSRF 攻击\n" +
                "\n" +
                "## 技术栈\n" +
                "\n" +
                "组件         | 技术\n" +
                "---               | ---\n" +
                "前端          | [Vue.js 2](https://cn.vuejs.org/)\n" +
                "后端 (REST API)    | [SpringBoot](https://projects.spring.io/spring-boot) (Java)\n" +
                "安全          | Token Based (Spring Security, [JJWT](https://github.com/auth0/java-jwt), CSRF)\n" +
                "前端脚手架| [vue-cli3](https://cli.vuejs.org/), Webpack, npm\n" +
                "后端构建| Maven\n" +
                "\n" +
                "## 快速运行\n" +
                "\n" +
                "#### 测试运行环境\n" +
                "\n" +
                "Java11， Node 12\n" +
                "\n" +
                "构建工具 Maven3， veu-cil3\n" +
                "\n" +
                "克隆项目到本地\n" +
                "\n" +
                "```bash\n" +
                "git clone https://github.com/PuZhiweizuishuai/SpringSecurity-JWT-Vue-Deom.git\n" +
                "```\n" +
                "\n" +
                "#### 后端运行\n" +
                "\n" +
                "```bash\n" +
                "cd spring-security-jwt\n" +
                "mvn clean package\n" +
                "```\n" +
                "\n" +
                "之后运行，程序默认运行端口8088\n" +
                "\n" +
                "```bash\n" +
                "java -jar target/security 0.0.1-SNAPSHOT.jar\n" +
                "```\n" +
                "\n" +
                "#### 前端运行\n" +
                "\n" +
                "```bash\n" +
                "cd vue\n" +
                "npm install\n" +
                "```\n" +
                "\n" +
                "之后运行，默认端口 8080\n" +
                "\n" +
                "```bash\n" +
                "npm run serve\n" +
                "```\n" +
                "\n" +
                "最后打开浏览器，输入\n" +
                "\n" +
                "```\n" +
                "http://127.0.0.1:8080\n" +
                "```\n" +
                "\n" +
                "## 实现细节\n" +
                "\n" +
                "待更新\n" +
                "\n" +
                "## 参考文档\n" +
                "\n" +
                "[Spring Security Reference](https://docs.spring.io/spring-security/site/docs/5.2.2.BUILD-SNAPSHOT/reference/htmlsingle/)\n" +
                "\n" +
                "\n" +
                "[Vue.js](https://cn.vuejs.org/)\n" +
                "\n" +
                "## 依赖工具\n" +
                "\n" +
                "[mavonEditor](https://github.com/hinesboy/mavonEditor)\n" +
                "\n" +
                "[element ui](https://element.eleme.io/)\n" +
                "\n" +
                "\n" +
                "## 版权和许可\n" +
                "\n" +
                "MIT license.\n" +
                "\n");
        return map;
    }

    @GetMapping("/data")
    public HttpEntity select(@RequestParam(value = "id", required = false) String id) {
        if (id == null) {
            return ResponseEntity.ok().body(systemDataService.get());
        }
        return ResponseEntity.ok().body(systemDataService.select(id));
    }
}
