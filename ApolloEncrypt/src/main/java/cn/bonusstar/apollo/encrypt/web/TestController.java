package cn.bonusstar.apollo.encrypt.web;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author raynor
 * @since 2026/4/8
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @ApolloConfig
    private Config config;

    @GetMapping("/query")
    public String query(String key) {
        return config.getProperty(key, "");
    }
}
