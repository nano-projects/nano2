/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.orm.mybatis;

import static com.google.inject.util.Providers.guicify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.sql.DataSource;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;
import org.nanoframework.beans.format.ClassCast;
import org.nanoframework.spi.def.Module;
import org.nanoframework.toolkit.io.support.ClassPathResource;
import org.nanoframework.toolkit.lang.CollectionUtils;
import org.nanoframework.toolkit.lang.ResourceUtils;
import org.nanoframework.toolkit.lang.StringUtils;
import org.nanoframework.toolkit.properties.exception.LoaderException;
import org.nanoframework.toolkit.util.Assert;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * MyBatis XML模式读取数据源，并通过jdbc属性文件进行配置<br>
 * 将创建的SqlSessionFactory写入至全局管理类.
 * @author yanghe
 * @since 1.2
 */
public class MultiDataSourceModule extends AbstractModule implements Module {
    private String envId;

    private Properties jdbc;

    private String mybatisConfigPath;

    private String[] mapperPackageName;

    private String[] typeAliasPackageName;

    /**
     * @param conf DataSourceConfig
     */
    public MultiDataSourceModule(DataSourceConfig conf) {
        jdbc = conf.getJdbc();
        envId = conf.getEnvId();
        mybatisConfigPath = conf.getMybatisConfigPath();
        mapperPackageName = conf.getMapperPackageName();
        typeAliasPackageName = conf.getTypeAliasPackageName();

        Assert.notNull(jdbc);
        Assert.hasLength(envId);
        Assert.hasLength(mybatisConfigPath);
        Assert.notEmpty(mapperPackageName);
    }

    @Override
    protected void configure() {
        Reader reader = null;
        try {
            InputStream input;
            try {
                var resource = new ClassPathResource(mybatisConfigPath);
                input = resource.getInputStream();
                if (input == null) {
                    input = new FileInputStream(ResourceUtils.getFile(mybatisConfigPath));
                }
            } catch (IOException e) {
                throw new LoaderException("加载文件异常: " + e.getMessage());
            }

            reader = new InputStreamReader(input);
            var sessionFactory = new SqlSessionFactoryBuilder().build(reader, envId, jdbc);
            var sessionManager = SqlSessionManager.newInstance(sessionFactory);
            GlobalSqlSession.set(envId, sessionManager);

            var configuration = sessionFactory.getConfiguration();
            var registry = configuration.getMapperRegistry();
            for (var pkg : mapperPackageName) {
                var classes = getClasses(pkg);
                if (CollectionUtils.isNotEmpty(classes)) {
                    for (var cls : classes) {
                        if (!registry.hasMapper(cls)) {
                            registry.addMapper(cls);
                        }
                    }
                }
            }

            var typeAliasRegistry = configuration.getTypeAliasRegistry();
            Arrays.stream(typeAliasPackageName).forEach(pkg -> typeAliasRegistry.registerAliases(pkg));
            settings(jdbc, configuration);

            // bind mappers
            var mapperClasses = registry.getMappers();
            for (var mapperType : mapperClasses) {
                bindMapper(mapperType, sessionManager);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {}
            }
        }
    }

    protected void settings(Properties proerties, Configuration conf) {
        var prefix = "mybatis.settings.";
        var fields = allFields(Lists.newArrayList(), Configuration.class);
        proerties.keySet().stream().filter(key -> ((String) key).startsWith(prefix)).forEach(k -> {
            var key = (String) k;
            var value = proerties.getProperty(key);
            var name = key.substring(prefix.length());
            fields.stream().filter(field -> StringUtils.equals(field.getName(), name)).forEach(field -> {
                try {
                    field.setAccessible(true);
                    field.set(conf, ClassCast.cast(value, field.getType().getName()));
                } catch (Throwable e) {
                    // ignore
                }
            });
        });
    }

    /**
     * Set the DataSource Provider type has to be bound.
     * @param dataSourceProviderType the DataSource Provider type
     */
    protected void bindDataSourceProviderType(Class<? extends Provider<DataSource>> dataSourceProviderType) {
        Assert.notNull(dataSourceProviderType, "Parameter 'dataSourceProviderType' must be not null");
        bind(DataSource.class).toProvider(dataSourceProviderType).in(Scopes.SINGLETON);
    }

    /**
     * @param <T>
     * @param mapperType
     */
    <T> void bindMapper(Class<T> mapperType, SqlSessionManager sessionManager) {
        bind(mapperType).toProvider(guicify(new MapperProvider<T>(mapperType, sessionManager))).in(Scopes.SINGLETON);
    }

    /**
     * Return a set of all classes contained in the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    Set<Class<?>> getClasses(String packageName) {
        return getClasses(new ResolverUtil.IsA(Object.class), packageName);
    }

    /**
     * Return a set of all classes contained in the given package that match with the given test requirement.
     * @param test the class filter on the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    Set<Class<?>> getClasses(ResolverUtil.Test test, String packageName) {
        Assert.notNull(test, "Parameter 'test' must not be null");
        Assert.notNull(packageName, "Parameter 'packageName' must not be null");
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }

    private List<Field> allFields(List<Field> allFields, Class<?> cls) {
        allFields.addAll(Arrays.asList(cls.getDeclaredFields()));
        if (cls.getSuperclass() == null) {
            return allFields;
        }

        return allFields(allFields, cls.getSuperclass());
    }

    @Override
    public List<Module> load() {
        return List.of(this);
    }

    @Override
    public void config(ServletConfig config) {

    }

    @Override
    public void destroy() {

    }

}
