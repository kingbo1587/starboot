package com.wh.starboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
@SpringBootApplication
@ImportResource({"classpath*:spring/spring.xml"})
public class Application extends SpringBootServletInitializer implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static ApplicationContext springApplicationContext;
    private static ReloadableResourceBundleMessageSource reloadableResource;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        logger.info("==============SpringApplicationBuilder==============");
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    private void init() {
        // 配置可重载的文件
        reloadableResource = new ReloadableResourceBundleMessageSource();
        reloadableResource.setBasename("classpath:properties/reload");
        reloadableResource.setCacheSeconds(60);
        reloadableResource.setConcurrentRefresh(false);// 线程安全
    }

    /**
     * 解决post中文乱码
     *
     * @return filter
     */
    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
        final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(characterEncodingFilter);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (springApplicationContext == null) {
            springApplicationContext = applicationContext;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return (T) springApplicationContext.getBean(beanName);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return springApplicationContext.getBeansOfType(type);
    }

    public static String getReloadableValue(String key) {
        return reloadableResource.getMessage(key, null, null, null);
    }

}
