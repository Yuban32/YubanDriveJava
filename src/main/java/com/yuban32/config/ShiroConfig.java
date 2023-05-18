package com.yuban32.config;


import com.yuban32.shiro.JWTFilter;
import com.yuban32.shiro.realm.AccountRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Shiro 启用注解拦截器
@Configuration
public class ShiroConfig {

    /**
     * 当有请求进入系统时，Shiro的Filter会拦截请求。
     * 根据ShiroFilterChainDefinition中配置的过滤器链规则，选择相应的过滤器进行处理。
     * 如果过滤器链规则中包括"jwt"过滤器，则调用JWTFilter进行相关的JWT认证处理，并完成相应的登录操作。
     * 如果认证通过，则根据权限注解进行相应的权限校验；否则，返回相关的错误信息。
     * 如果过滤器链规则中还包括其他过滤器，则依次执行相应的过滤器。
     * 最终，返回相关的结果信息，完成整个请求处理过程。
     **/
    @Autowired
    JWTFilter jwtFilter;

    @Autowired
    private RedisSessionDAO redisSessionDAO;
    @Autowired
    private RedisCacheManager redisCacheManager;
    @Value("${yuban32.jwt.expire}")
    private int expire;
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();

        // 注入 redisSessionDAO
        sessionManager.setSessionDAO(redisSessionDAO);
        return sessionManager;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(AccountRealm accountRealm,
                                                     SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(accountRealm);

        //注入 sessionManager
        securityManager.setSessionManager(sessionManager);

        // 注入 redisCacheManager
        redisCacheManager.setExpire(expire);
        securityManager.setCacheManager(redisCacheManager);
        return securityManager;
    }
    /**
     * @description 拦截规则
     * @param
     * @return ShiroFilterChainDefinition
     **/
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();

        Map<String, String> filterMap = new LinkedHashMap<>();
        //所有接口都交由jwtFilter来进行处理
        //"jwt"在下方有定义
        filterMap.put("/**", "jwt");
//        filterMap.put("/authctest","authc");
//        filterMap.put("/admin/**","authc");

        chainDefinition.addPathDefinitions(filterMap);
        return chainDefinition;
    }

    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter);
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = shiroFilterChainDefinition.getFilterChainMap();
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }


}
