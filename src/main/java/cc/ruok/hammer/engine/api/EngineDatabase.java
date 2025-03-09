package cc.ruok.hammer.engine.api;

import cc.ruok.hammer.engine.Engine;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;
import java.util.*;

public class EngineDatabase extends EngineAPI {

    public EngineDatabase(Engine engine) {
        super(engine);
    }

    public DataBaseConnect connect(String url, String username, String password) throws EngineException {
        try {
            Connection conn = DriverManager.getConnection("jdbc:" + url, username, password);
            DataBaseConnect dataBaseConnect = new DataBaseConnect(conn, engine);
            engine.addCloseable(dataBaseConnect);
            return dataBaseConnect;
        } catch (SQLException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public DataBaseConnect getConnect(String name) throws EngineException {
        try {
            ComboPooledDataSource cpds = engine.getWebSite().pool.get(name);
            DataBaseConnect dataBaseConnect = new DataBaseConnect(cpds.getConnection(), engine);
            engine.addCloseable(dataBaseConnect);
            return dataBaseConnect;
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }

    public void closeAll() {
        engine.closeAllConnect();
    }

    @Override
    public String getVarName() {
        return "Database";
    }

    public static class DataBaseConnect implements Closeable {

        private final Connection connection;
        private final Engine engine;
        private boolean keep = false;

        public DataBaseConnect(Connection connection, Engine engine) {
            this.connection = connection;
            this.engine = engine;
        }

        public List<Map<String, Object>> query(String sql) throws EngineException {
            try {
                Statement stat = connection.createStatement();
                ResultSet resultSet = stat.executeQuery(sql);
                List<Map<String, Object>> result = new LinkedList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()){
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                        map.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    result.add(map);
                }
                stat.close();
                return result;
            } catch (SQLException e) {
                throw new EngineException(e.getMessage());
            }
        }

        public int update(String sql) throws EngineException {
            try {
                Statement stat = connection.createStatement();
                return stat.executeUpdate(sql);
            } catch (SQLException e) {
                throw new EngineException(e.getMessage());
            }
        }

        public void close() throws EngineException {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new EngineException(e.getMessage());
            }
        }

        @Override
        public void keep() {
            this.keep = true;
        }

        @Override
        public boolean isKeep() {
            return this.keep;
        }

        public boolean isClosed() throws EngineException {
            try {
                return connection.isClosed();
            } catch (SQLException e) {
                throw new EngineException(e.getMessage());
            }
        }

        public boolean isActive() throws EngineException {
            return !isClosed();
        }

        public void trans() throws EngineException {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new EngineException(e.getMessage());
            }
        }

        public void commit() throws EngineException {
            try {
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }

        public void rollback() throws EngineException {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }

        public Prepare prepare(String sql) throws EngineException {
            try {
                PreparedStatement p = connection.prepareStatement(sql);
                Prepare prepare = new Prepare(sql, p, engine);
                engine.addCloseable(prepare);
                return prepare;
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }

    }

    public static class Prepare implements Closeable {

        private String sql;
        private PreparedStatement stat;
        private Engine engine;
        private boolean keep = false;

        public Prepare(String sql, PreparedStatement stat, Engine engine) {
            this.sql = sql;
            this.stat = stat;
            this.engine = engine;
        }

        public void set(int index, Object value) throws EngineException {
            try {
                if (value instanceof String v) {
                    stat.setString(index, v);
                } else if (value instanceof Integer v) {
                    stat.setInt(index, v);
                } else if (value instanceof Long v) {
                    stat.setLong(index, v);
                } else if (value instanceof Float v) {
                    stat.setFloat(index, v);
                } else if (value instanceof Double v) {
                    stat.setDouble(index, v);
                } else if (value instanceof Boolean v) {
                    stat.setBoolean(index, v);
                } else {
                    stat.setObject(index, value);
                }
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }

        public int executeUpdate() throws EngineException {
            try {
                return stat.executeUpdate();
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }

        public List<Map<String, Object>> executeQuery() throws EngineException {
            try {
                ResultSet resultSet = stat.executeQuery();
                List<Map<String, Object>> result = new LinkedList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                        map.put(metaData.getColumnName(i), resultSet.getObject(i));
                    }
                    result.add(map);
                }
                stat.close();
                return result;
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }
        public void close() throws EngineException {
            try {
                stat.close();
            } catch (SQLException e) {
                throw new EngineException(e);
            }
        }

        @Override
        public void keep() {
            this.keep = true;
        }

        @Override
        public boolean isKeep() {
            return this.keep;
        }
    }
}
