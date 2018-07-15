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
package org.nanoframework.orm.jdbc;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nanoframework.modules.logging.Logger;
import org.nanoframework.modules.logging.LoggerFactory;
import org.nanoframework.orm.PoolType;
import org.nanoframework.orm.jdbc.config.JdbcConfig;
import org.nanoframework.orm.jdbc.jstl.Result;
import org.nanoframework.orm.jdbc.jstl.ResultSupport;
import org.nanoframework.orm.jdbc.pool.DruidPool;
import org.nanoframework.orm.jdbc.pool.Pool;
import org.nanoframework.orm.jdbc.pool.TomcatJdbcPool;
import org.nanoframework.toolkit.lang.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.NonNull;

/**
 * JDBC适配器，基础JDBC处理对象，实例化需要实现JdbcCreater注解.
 * @author yanghe
 * @since 1.3.6
 */
public class JdbcAdapter implements DefaultSqlExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcAdapter.class);

    private static final Object LOCK = new Object();

    private static final AtomicBoolean INIT = new AtomicBoolean(false);

    private static JdbcAdapter INSTANCE;

    private Pool pool;

    private JdbcAdapter(Collection<JdbcConfig> configs, @NonNull PoolType poolType)
            throws PropertyVetoException, SQLException {
        if (INIT.get()) {
            throw new SQLException("数据源已经加载");
        }

        switch (poolType) {
            case DRUID:
                pool = new DruidPool(configs);
                break;
            case TOMCAT_JDBC_POOL:
                pool = new TomcatJdbcPool(configs);
                break;
            default:
                throw new DataSourceException("无效的PoolType");
        }

        INIT.set(true);
    }

    protected static JdbcAdapter newInstance(Collection<JdbcConfig> configs, PoolType poolType, @NonNull Object obj) {
        try {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new JdbcAdapter(configs, poolType);
                } else {
                    INSTANCE.shutdown();
                    return newInstance(configs, poolType, obj);
                }
            }

            return INSTANCE;
        } catch (SQLException | PropertyVetoException e) {
            throw new DataSourceException(e.getMessage());
        }
    }

    public static JdbcAdapter adapter() {
        return INSTANCE;
    }

    public Connection getConnection(String dataSource) throws SQLException {
        try {
            return pool.getPool(dataSource).getConnection();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public void commit(@NonNull Connection conn) throws SQLException {
        if (isTxInit(conn)) {
            conn.commit();
        }
    }

    public void rollback(@NonNull Connection conn) throws SQLException {
        if (isTxInit(conn)) {
            conn.rollback();
        }
    }

    public boolean isTxInit(@NonNull Connection conn) throws SQLException {
        return !conn.getAutoCommit();
    }

    public Statement getStatement(@NonNull Connection conn) throws SQLException {
        return conn.createStatement();
    }

    public PreparedStatement getPreparedStmt(@NonNull Connection conn, String sql, List<Object> values)
            throws SQLException {
        var pstmt = conn.prepareStatement(sql);
        setValues(pstmt, values);
        return pstmt;
    }

    public PreparedStatement getPreparedStmtForBatch(@NonNull Connection conn, String sql,
            List<List<Object>> batchValues) throws SQLException {
        var pstmt = conn.prepareStatement(sql);
        if (batchValues != null && batchValues.size() > 0) {
            for (var values : batchValues) {
                setValues(pstmt, values);
                pstmt.addBatch();
            }
        }

        return pstmt;
    }

    public Result executeQuery(String sql, @NonNull Connection conn) throws SQLException {
        var start = System.currentTimeMillis();
        Result result = null;
        ResultSet rs = null;
        Statement stmt = null;

        try {
            stmt = getStatement(conn);
            stmt.setQueryTimeout(60);
            rs = stmt.executeQuery(sql);
            rs.setFetchSize(rs.getRow());
            result = ResultSupport.toResult(rs);
        } finally {
            close(rs, stmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Query SQL ]: {} cost [ {}ms ]", sql, System.currentTimeMillis() - start);
            }
        }

        return result;
    }

    public int executeUpdate(String sql, @NonNull Connection conn) throws SQLException {
        var start = System.currentTimeMillis();
        var result = 0;
        Statement stmt = null;
        try {
            stmt = getStatement(conn);
            stmt.setQueryTimeout(60);
            result = stmt.executeUpdate(sql);
        } finally {
            close(stmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Update/Insert SQL ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
            }
        }

        return result;
    }

    public Result executeQuery(String sql, List<Object> values, @NonNull Connection conn) throws SQLException {
        var start = System.currentTimeMillis();
        Result result = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = getPreparedStmt(conn, sql, values);
            pstmt.setQueryTimeout(60);
            rs = pstmt.executeQuery();
            rs.setFetchSize(rs.getRow());
            result = ResultSupport.toResult(rs);
        } finally {
            close(rs, pstmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Query SQL ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
                LOGGER.debug("[ Execute Parameter ]: {}",
                        JSON.toJSONString(values, SerializerFeature.WriteDateUseDateFormat));
            }
        }

        return result;
    }

    public int executeUpdate(String sql, List<Object> values, @NonNull Connection conn) throws SQLException {
        var start = System.currentTimeMillis();
        PreparedStatement pstmt = null;

        try {
            pstmt = getPreparedStmt(conn, sql, values);
            pstmt.setQueryTimeout(60);
            return pstmt.executeUpdate();
        } finally {
            close(pstmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Update/Insert SQL ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
                LOGGER.debug("[ Execute Parameter ]: {}",
                        JSON.toJSONString(values, SerializerFeature.WriteDateUseDateFormat));
            }
        }

    }

    public int[] executeBatchUpdate(String sql, List<List<Object>> batchValues, @NonNull Connection conn)
            throws SQLException {
        if (CollectionUtils.isEmpty(batchValues)) {
            return new int[0];
        }

        var start = System.currentTimeMillis();
        PreparedStatement pstmt = null;
        try {
            pstmt = getPreparedStmtForBatch(conn, sql, batchValues);
            pstmt.setQueryTimeout(60);
            return pstmt.executeBatch();
        } finally {
            close(pstmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute Update/Insert SQL ] : {} [cost {}ms ]", sql,
                        System.currentTimeMillis() - start);
                LOGGER.debug("[ Execute Parameter ]: {}",
                        JSON.toJSONString(batchValues, SerializerFeature.WriteDateUseDateFormat));
            }
        }
    }

    @Override
    public boolean execute(String sql, @NonNull Connection conn) throws SQLException {
        var start = System.currentTimeMillis();
        Statement stmt = null;
        try {
            stmt = getStatement(conn);
            return stmt.execute(sql);
        } finally {
            close(stmt);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[ Execute ]: {} [cost {}ms ]", sql, System.currentTimeMillis() - start);
            }
        }
    }

    private void setValues(PreparedStatement pstmt, List<Object> values) throws SQLException {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        for (var i = 0; i < values.size(); i++) {
            if (values.get(i) instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) values.get(i));
            } else if (values.get(i) instanceof Long) {
                pstmt.setLong(i + 1, (Long) values.get(i));
            } else if (values.get(i) instanceof String) {
                pstmt.setString(i + 1, (String) values.get(i));
            } else if (values.get(i) instanceof Double) {
                pstmt.setDouble(i + 1, (Double) values.get(i));
            } else if (values.get(i) instanceof Float) {
                pstmt.setFloat(i + 1, (Float) values.get(i));
            } else if (values.get(i) instanceof Timestamp) {
                pstmt.setTimestamp(i + 1, (Timestamp) values.get(i));
            } else if (values.get(i) instanceof java.util.Date) {
                java.util.Date tempDate = (java.util.Date) values.get(i);
                pstmt.setDate(i + 1, new Date(tempDate.getTime()));
            } else {
                pstmt.setObject(i + 1, values.get(i));
            }
        }
    }

    public void close(Object... jdbcObj) {
        if (jdbcObj != null && jdbcObj.length > 0) {
            for (var obj : jdbcObj) {
                try {
                    if (obj != null) {
                        if (obj instanceof ResultSet) {
                            ((ResultSet) obj).close();
                            obj = null;
                        } else if (obj instanceof Statement) {
                            ((Statement) obj).close();
                            obj = null;
                        } else if (obj instanceof PreparedStatement) {
                            ((PreparedStatement) obj).close();
                            obj = null;
                        } else if (obj instanceof Connection) {
                            ((Connection) obj).close();
                            obj = null;
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public void shutdown() {
        pool.closeAndClear();
        pool = null;
        INIT.set(false);
        INSTANCE = null;
    }

}
