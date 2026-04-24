package com.homi.common.lib.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 应用于 homi
 *
 * @author tk
 * @version v1.0
 * {@code @date} 2026/3/15
 */

@Configuration
public class OpenApiEnumConfig {

    @Bean
    public OpenApiCustomizer registerAllEnumsCustomizer() {
        return openApi -> {
            Components components = Optional.ofNullable(openApi.getComponents())
                .orElseGet(Components::new);
            openApi.setComponents(components);

            ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(Enum.class) {
                @Override
                protected boolean matchClassName(String className) {
                    try {
                        return Class.forName(className).isEnum();
                    } catch (ClassNotFoundException e) {
                        return false;
                    }
                }
            });

            for (BeanDefinition bd : scanner.findCandidateComponents("com.homi.common.lib.enums")) {
                try {
                    Class<?> enumClass = Class.forName(bd.getBeanClassName());
                    if (!enumClass.isEnum()) continue;

                    String schemaName = enumClass.getSimpleName();
                    if (components.getSchemas() != null
                        && components.getSchemas().containsKey(schemaName)) continue;

                    components.addSchemas(schemaName, buildEnumSchema(enumClass));

                } catch (ClassNotFoundException e) { /* ignore */ }
            }
        };
    }

    @Bean
    public OpenApiCustomizer checkoutPresetFeeSchemaCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null || components.getSchemas() == null) {
                return;
            }

            Schema<?> initSchema = components.getSchemas().get("LeaseCheckoutInitVO");
            Schema<?> feeSchema = components.getSchemas().get("LeaseCheckoutFeeVO");
            if (initSchema == null || feeSchema == null || initSchema.getProperties() == null) {
                return;
            }

            ArraySchema presetFeesSchema = new ArraySchema();
            presetFeesSchema.setItems(new Schema<>().$ref("#/components/schemas/LeaseCheckoutFeeVO"));
            initSchema.addProperty("presetFees", presetFeesSchema);

            components.getSchemas().remove("PresetFeeVO");
        };
    }

    private Schema<?> buildEnumSchema(Class<?> enumClass) {
        StringSchema schema = new StringSchema();

        // 读取类注解描述
        io.swagger.v3.oas.annotations.media.Schema ann =
            enumClass.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        if (ann != null && !ann.description().isBlank()) {
            schema.setDescription(ann.description());
        }

        Object[] constants = enumClass.getEnumConstants();

        // 枚举字符串值
        for (Object constant : constants) {
            schema.addEnumItem(((Enum<?>) constant).name());
        }

        // 尝试反射读取所有非枚举字段（code、name、label、desc 等任意字段名）
        List<Field> fields = getNonEnumFields(enumClass);
        if (!fields.isEmpty()) {
            // x-enum-varnames
            List<String> varNames = Arrays.stream(constants)
                .map(c -> ((Enum<?>) c).name())
                .toList();
            schema.addExtension("x-enum-varnames", varNames);

            // 每个额外字段单独输出一个扩展，字段名即 key
            for (Field field : fields) {
                field.setAccessible(true);
                List<Object> values = new ArrayList<>();
                for (Object constant : constants) {
                    try {
                        values.add(field.get(constant));
                    } catch (IllegalAccessException e) {
                        values.add(null);
                    }
                }
                schema.addExtension("x-enum-" + field.getName(), values);
            }
        }

        return schema;
    }

    /**
     * 获取枚举类中非枚举常量本身的字段（即业务字段：code、name 等）
     */
    private List<Field> getNonEnumFields(Class<?> enumClass) {
        return Arrays.stream(enumClass.getDeclaredFields())
            .filter(f -> !f.isEnumConstant())          // 排除枚举常量本身
            .filter(f -> !f.isSynthetic())             // 排除编译器生成字段
            .filter(f -> !Modifier.isStatic(f.getModifiers())) // 排除静态字段
            .toList();
    }
}
