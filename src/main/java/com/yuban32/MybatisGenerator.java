package com.yuban32;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Yuban32
 * @Date: 2022年11月25日
 */
public class MybatisGenerator {
//    @Value("${spring.datasource.url}")
//    private static String url;
//    @Value("${spring.datasource.username}")
//    private static String username;
//    @Value("${spring.datasource.password}")
//    private static String password;
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.129.76.216:3306/Yuban_Drive?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=UTC";
        String username = "Yuban_Drive";
        String password = "3DDdsB33ZK5WzjA4";
        //表名集合
        List<String> tables = new ArrayList<>();
        tables.add("t_chunk_info");
        tables.add("t_file_info");

        FastAutoGenerator.create(url,username,password)
                .globalConfig(builder -> {
                    builder.fileOverride()
                            .author("Yuban32")
                            .outputDir(System.getProperty("user.dir")+"\\src\\main\\java")
                            .dateType(DateType.TIME_PACK)
                            .commentDate("yyy-MM-dd");
                })
                .packageConfig(builder -> {
                    builder.parent("com.yuban32")
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml,System.getProperty("user.dir")+"\\src\\main\\resources\\mapper"));

                })
                .strategyConfig(builder -> {
                    builder.addInclude(tables)
                            .addTablePrefix("t_")
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .controllerBuilder()
                            .formatFileName("%sController")
                            .enableRestStyle()
                            .mapperBuilder()
                            .superClass(BaseMapper.class)
                            .formatMapperFileName("%sMapper")
                            .enableMapperAnnotation()
                            .formatXmlFileName("%sMapper");
                }).execute();
    }
}
