package com.travel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * <p><b>作用：</b>bootstrap 模块的冒烟测试：启动完整 Spring 上下文，验证各模块配置与 Bean 能正常装配。</p>
 * <p>若缺少数据库 / Redis 等依赖，可能需在测试中配合 Testcontainers 或 profile 关闭自动配置。</p>
 */
@SpringBootTest
class TravelAssistantApplicationTests {

    /**
     * 空方法即可触发上下文加载；若启动失败测试会报错。
     */
    @Test
    void contextLoads() {
    }
}
