package com.homi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
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
public class HomiApplication extends SpringBootServletInitializer {
    @SuppressWarnings("checkstyle:OperatorWrap")
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(HomiApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");

        log.info("项目名称：{}", env.getProperty("spring.application.name"));

        log.info("\n----------------------------------------------------------\n" +
                "  _  _     ___   __  __    ___   \n" +
                " | || |   / _ \\ |  \\/  |  |_ _|  \n" +
                " | __ |  | (_) || |\\/| |   | |   \n" +
                " |_||_|   \\___/ |_|__|_|  |___|  \n" +
                "_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"| \n" +
                "\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-' \n" +
                "应用启动成功! \n" +
                "Local: http://localhost:" + port + "\n" +
                "External: https://" + ip + ":" + port + "\n" +
                "----------------------------------------------------------");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(HomiApplication.class);
    }

    /**
     * 访问首页，提示语
     */
    @RequestMapping("admin/")
    public String index() {
        return "请通过前端地址访问。";
    }
}
