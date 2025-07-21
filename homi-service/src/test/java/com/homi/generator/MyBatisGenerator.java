package com.homi.generator;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.Builder;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.type.JdbcType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 应用于 homi-boot
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2025/4/17
 */
@Data
@Builder
public class MyBatisGenerator {
    String dbUrl;

    String dbUsername;

    String dbPassword;

    String schema;

    String moduleNameOfDao;
    String entityPackageName;
    String mapperPackageName;
    String servicePackageName;

    List<String> tblNameList;

    String tblPrefix;

    public static void main(String[] args) {
        MyBatisGenerator myBatisGenerator = MyBatisGenerator.builder()
                .dbUrl("jdbc:postgresql://localhost:5432/homi")
                .dbUsername("postgres")
                .dbPassword("123456")
                .schema("house")
                .tblPrefix("")
                .moduleNameOfDao("homi-service")
                .entityPackageName("com.homi.model.entity")
                .mapperPackageName("com.homi.model.mapper")
                .servicePackageName("com.homi.model.repo")
                .tblNameList(Arrays.asList(
                        "house",
                        "room",
                        "room_layout",
                        "house_focus"
                )).build();

        myBatisGenerator.generate();
    }

    public void generate() {
        DataSourceConfig.Builder dataSourceConfig = getDataSourceConfig();

        // 代码生成器
        FastAutoGenerator.create(dataSourceConfig)
                .globalConfig(builder -> builder.author("tk").dateType(DateType.ONLY_DATE).disableOpenDir().enableSpringdoc())
                .packageConfig(builder -> {
                    String projectPath = System.getProperty("user.dir") + "/";
                    /*
                     * Java源文件的路径
                     */
                    Map<OutputFile, String> pathInfo = new EnumMap<>(OutputFile.class);
                    String sourcePath = "/src/main/java/";
                    //entity 路径
                    pathInfo.put(OutputFile.entity, projectPath + moduleNameOfDao + sourcePath + entityPackageName.replaceAll("\\.", "/"));
                    //mapper 路径
                    pathInfo.put(OutputFile.mapper, projectPath + moduleNameOfDao + sourcePath + mapperPackageName.replaceAll("\\.", "/"));
                    //mapper xml文件 路径
                    pathInfo.put(OutputFile.xml, projectPath + moduleNameOfDao + "/src/main/resources/mapper");
                    //service 路径
                    pathInfo.put(OutputFile.serviceImpl, projectPath + moduleNameOfDao + sourcePath + servicePackageName.replaceAll("\\.", "/"));
                    /*
                     * 类文件里边的包路径：package xxx.xxx.xxx
                     */
                    builder.parent("").entity(entityPackageName).mapper(mapperPackageName).serviceImpl(servicePackageName).pathInfo(pathInfo);

                }).strategyConfig(builder -> {
                    /*
                     * 设置需要生成的表名
                     */
                    builder.addInclude(tblNameList);

                    if (CharSequenceUtil.isNotBlank(tblPrefix)) {
                        builder.addTablePrefix(tblPrefix);
                    }

                    builder.enableSchema();

                    //mapper注解生效
                    builder.mapperBuilder().mapperAnnotation(Mapper.class);
                    builder.serviceBuilder().formatServiceImplFileName("%sRepo").disableService()
                            .serviceImplTemplate("repo.template.java");
                    builder.controllerBuilder().disable();
                    builder.entityBuilder().enableFileOverride().enableLombok().logicDeleteColumnName("deleted")
                            .enableTableFieldAnnotation()
                            .naming(NamingStrategy.underline_to_camel).columnNaming(NamingStrategy.underline_to_camel)
                            .javaTemplate("entity.template.java")
                            .enableFileOverride();
                }).templateEngine(new FreemarkerTemplateEngine()).execute();
    }

    private DataSourceConfig.Builder getDataSourceConfig() {
        DataSourceConfig.Builder dataSourceConfig = new DataSourceConfig.Builder(dbUrl, dbUsername, dbPassword)
                .dbQuery(new PostgreSqlQuery()).schema(schema);


        // 当字段长度大于1时,默认转换成Byte,符合类型长度范围,如果想继续转换成Integer.
        dataSourceConfig.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
            if (JdbcType.TINYINT == metaInfo.getJdbcType() || JdbcType.SMALLINT == metaInfo.getJdbcType() || JdbcType.INTEGER == metaInfo.getJdbcType()) {
                return DbColumnType.INTEGER;
            }

            return typeRegistry.getColumnType(metaInfo);
        });


        return dataSourceConfig;
    }
}
