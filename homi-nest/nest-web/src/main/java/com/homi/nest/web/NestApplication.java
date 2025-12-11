package com.homi.nest.web;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@RestController
@EnableAsync
@Slf4j
@ComponentScan(basePackages = {"com.homi"})
@MapperScan("com.homi.model.dao.mapper")
public class NestApplication extends SpringBootServletInitializer {
    @SuppressWarnings("checkstyle:OperatorWrap")
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(NestApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");

        log.info("项目名称：{}", env.getProperty("spring.application.name") +
            "\n----------------------------------------------------------\n" +
            "  _  _     ___     ___    _____  \n" +
            " | \\| |   | __|   / __|  |_   _| \n" +
            " | .` |   | _|    \\__ \\    | |   \n" +
            " |_|\\_|   |___|   |___/   _|_|_  \n" +
            "_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"| \n" +
            "\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-' \n" +
            "应用启动成功! \n" +
            "Local: http://localhost:" + port + "\n" +
            "External: https://" + ip + ":" + port + "\n" +
            "----------------------------------------------------------");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(NestApplication.class);
    }

    /**
     * 访问首页，提示语
     */
    @RequestMapping("admin/")
    public String index() {
        return "请通过前端地址访问。";
    }
}
