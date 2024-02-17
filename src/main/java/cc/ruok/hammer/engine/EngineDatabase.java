package cc.ruok.hammer.engine;

import java.sql.*;
import java.util.*;

public class EngineDatabase {

    private final List<DataBaseConnect> connects = new ArrayList<>();

    public DataBaseConnect connect(String url, String username, String password) throws EngineException {
        try {
            Connection conn = DriverManager.getConnection("jdbc:" + url, username, password);
            DataBaseConnect dataBaseConnect = new DataBaseConnect(conn);
            connects.add(dataBaseConnect);
            return dataBaseConnect;
        } catch (SQLException e) {
            throw new EngineException(e.getMessage());
        }
    }

    public void closeAll() {
        for (DataBaseConnect connect : connects) {
            try {
                connect.close();
            } catch (EngineException e) {
            }
        }
    }

    public static class DataBaseConnect {

        private final Connection connection;

        public DataBaseConnect(Connection connection) {
            this.connection = connection;
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

    }

}
