package com.ape.apeframework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**

 * @description: 图片、视频、文件拦截
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        String fileSeparator = System.getProperty("file.separator");
        String userDir = System.getProperty("user.dir");
        //歌手头像地址
        registry.addResourceHandler("/img/**").addResourceLocations(
                "file:" + userDir + fileSeparator + "img" + fileSeparator + fileSeparator,
                "file:" + userDir + fileSeparator + ".." + fileSeparator + "img" + fileSeparator + fileSeparator
        );

        registry.addResourceHandler("/video/**").addResourceLocations(
                "file:" + userDir + fileSeparator + "video" + fileSeparator + fileSeparator,
                "file:" + userDir + fileSeparator + ".." + fileSeparator + "video" + fileSeparator + fileSeparator
        );

        registry.addResourceHandler("/file/**").addResourceLocations(
                "file:" + userDir + fileSeparator + "file" + fileSeparator + fileSeparator,
                "file:" + userDir + fileSeparator + ".." + fileSeparator + "file" + fileSeparator + fileSeparator
        );
    }

}
