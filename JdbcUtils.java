import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import java.io.IOException;
import java.io.InputStream;

public class JdbcUtils {

    private static String driver = null;
    private static String url = null;
    private static String username = null;
    private static String password = null;

    //加载驱动
    static {
        try {
            InputStream in = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties");
            Properties properties = new Properties();
            properties.load(in);
            driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //建立与数据库的连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    //资源释放
    public static void release(Connection conn, PreparedStatement st, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    //  查   ：一次查一张表，传入需要查看的字段名、所查的表名、通过什么id查、查的id号
    //  所需查的字段量小于应输入字段量时，字段输入null
    public static void select(String columnName1, String columnName2, String columnName3, String columnName4, String tableName, String selectColumnName, int id) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT " + columnName1;
            if (columnName2 != null) sql += " , " + columnName2;
            if (columnName3 != null) sql += " , " + columnName3;
            if (columnName4 != null) sql += " , " + columnName4;
            sql += " from " + tableName + " where " + selectColumnName + " = ?";  //编写sql

            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);
            st.setInt(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                System.out.print(rs.getString(columnName1) + " ");
                if (columnName2 != null) System.out.print(rs.getString(columnName2) + " ");
                if (columnName3 != null) System.out.print(rs.getString(columnName3) + " ");
                if (columnName4 != null) System.out.print(rs.getString(columnName4) + " ");
                System.out.print(rs.getString(selectColumnName) + " ");
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }


//以下为  改

    //改商品表 ：输入 需要改的数据的商品编号 ，商品名 以及 改后的 商品名，商品价格
    //  只知道商品名 时 编号输入0；只知道编号时商品名输入null
    //  只想改商品名时 改后商品价格输入0；只想改价格时 改后商品名输入null
    public static void updateProduct(int proId, String proNameUpdate, int proPriceUpdate) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = "update product set ";
            if (proNameUpdate != null) {
                sql += "proName=? ";
                if (proPriceUpdate > 0) sql += ", proPrice=? ";
                else if (proPriceUpdate < 0) {
                    System.out.println("wrong price");
                    return;
                }
                sql += "where proID= ?" ;
            }else {
                if (proPriceUpdate > 0) sql += "proPrice=? ";
                else if (proPriceUpdate < 0) {
                    System.out.println("wrong price");
                    return;
                }
                sql += "where proID= ?";// 判断需求，编写sql
            }

            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);  //预编译
            if (proNameUpdate != null && proPriceUpdate > 0) {
                st.setString(1, proNameUpdate);
                st.setInt(2, proPriceUpdate);
                st.setInt(3, proId);
            } else if (proNameUpdate == null) {
                st.setInt(1, proPriceUpdate);
                st.setInt(2, proId);
            } else if (proPriceUpdate == 0) {
                st.setString(1, proNameUpdate);
                st.setInt(2, proId);
            } else {
                System.out.println("wrong input");
                return;
            }                                                   //根据需求判断手动赋值

            if (st.executeUpdate() > 0) System.out.println("product table update successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //改订单详情表，根据订单详情中的id 看改哪条 改这条数据的订单编号/商品编号/商品数量，不想改的部分输入为0

    public static void updateOrder_infro(int id, int orderIdUpdate, int proIdUpdate, int proNumUpdate) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            String sql = "update order_infro set ";
            if (orderIdUpdate != 0) {
                Statement statement1 = conn.createStatement();
                ResultSet rs1 = statement1.executeQuery("select orderId from `order` where orderId =" + orderIdUpdate);
                if (!rs1.next()) {
                    System.out.println("wrong input,the orderId don't exist");
                    return;//判断这条订单号是否存在与订单表中
                }
                sql += "orderId=? ";
            }
            if (proIdUpdate != 0) {
                Statement statement2 = conn.createStatement();
                ResultSet rs2 = statement2.executeQuery("select proId from product where proId =" + proIdUpdate);
                if (!rs2.next()) {
                    System.out.println("wrong input,the proId don't exist");
                    release(conn, st, rs);
                    return;//判断这个商品编号是否存在与商品表中
                }
                sql += "proId=? ";
            }
            if (proNumUpdate > 0) sql += ", proNumUpdate=?";
            else if (proNumUpdate < 0) {
                System.out.println("wrong number");
                release(conn, st, rs);
                return;
            }
            sql += "where id=?";    //编写sql


            st = conn.prepareStatement(sql);  //预编译

            if (orderIdUpdate != 0 && proIdUpdate != 0 && proNumUpdate > 0) {
                st.setInt(1, orderIdUpdate);
                st.setInt(2, proIdUpdate);
                st.setInt(3, proNumUpdate);
                st.setInt(4, id);
            } else if (proIdUpdate != 0 && proNumUpdate > 0) {
                st.setInt(1, proIdUpdate);
                st.setInt(2, proNumUpdate);
                st.setInt(3, id);
            } else if (orderIdUpdate != 0 && proNumUpdate > 0) {
                st.setInt(1, orderIdUpdate);
                st.setInt(2, proNumUpdate);
                st.setInt(3, id);
            } else if (orderIdUpdate != 0 && proIdUpdate != 0) {
                st.setInt(1, orderIdUpdate);
                st.setInt(2, proIdUpdate);
                st.setInt(3, id);
            } else if (orderIdUpdate != 0) {
                st.setInt(1, orderIdUpdate);
                st.setInt(2, id);
            } else if (proIdUpdate != 0) {
                st.setInt(1, proIdUpdate);
                st.setInt(2, id);
            } else if (proNumUpdate > 0) {
                st.setInt(1, proNumUpdate);
                st.setInt(2, id);
            } else {
                System.out.println("wrong input");
                release(conn, st, rs);
                return;                                             //根据需求判断手动赋值
            }
            System.out.println(st);

            if (st.executeUpdate() > 0) System.out.println("order_infro table update successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //改订单表的时间
    public static void updateOrder(int orderId, Calendar orderTimeUpdate) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = "update `order` set orderTime=? where orderId =?";//编写sql
            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);  //预编译
            Date d = orderTimeUpdate.getTime();
            st.setDate(1, new java.sql.Date(d.getTime()));
            st.setInt(2,orderId);//根据需求手动赋值
            if (st.executeUpdate() > 0) System.out.println("order table update successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //以下为插入

    //对商品表插入数据，需要输入商品名和价格
    public static void insertProduct(String proName, int proPrice) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = "insert product (proName,proPrice) values (?,?)";//编写sql
            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);//预编译
            st.setString(1, proName);
            st.setInt(2, proPrice);    //手动赋值
            if (st.executeUpdate() > 0) System.out.println("product table insert successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //对订单详情表插入，需要订单编号，商品编号，商品数量
    public static void insertOrder_infro(int orderId, int proId, int proNum) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            Statement statement1 = conn.createStatement();
            ResultSet rs1 = statement1.executeQuery("select orderId from `order` where orderId =" + orderId);
            if (!rs1.next()) {
                System.out.println("wrong input,the orderId don't exist");
                release(conn, st, rs);
                return;//判断这条订单号是否存在与订单表中
            }
            Statement statement2 = conn.createStatement();
            ResultSet rs2 = statement2.executeQuery("select proId from product where proId =" + proId);
            if (!rs2.next()) {
                System.out.println("wrong input,the proId don't exist");
                release(conn, st, rs);
                return;//判断这个商品编号是否存在与商品表中
            }

            String sql = "insert order_infro (orderId,proId,proNum) values (?,?,?)";//编写sql

            st = conn.prepareStatement(sql);//预编译

            st.setInt(1, orderId);
            st.setInt(2, proId);
            st.setInt(3, proNum);   //手动赋值

            if (st.executeUpdate() > 0) System.out.println("order_infro table insert successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //对订单表插入，需要订单创建的时间
    public static void insertOrder(Calendar orderTime) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = "insert `order` (orderTime) values (?)";//编写sql
            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);//预编译
            Date d = orderTime.getTime();
            st.setDate(1, new java.sql.Date(d.getTime()));   //手动赋值

            if (st.executeUpdate() > 0) System.out.println("order table update successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //以下是删除

    //删除商品表的数据，需要商品编号或商品名，知道商品名则商品编号输入0，知道商品编号则商品名输入null
    public static void deleteProduct(int proId, String proName) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = null;
            if (proId != 0) {
                sql = "delete from product where proId=?;";//编写sql
            } else if (proName != null) {
                sql = "delete from product where proName=?;";
            }
            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);

            if (proId != 0) {
                st.setInt(1, proId);
            } else if (proName != null) {
                st.setString(1, proName);          //手动赋值
            }


            if (st.executeUpdate() > 0) System.out.println(" delete from product table  successful");
            conn.commit();


        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //删除订单详情表，需要订单详情表id/订单编号/商品编号 只要一个，其他两个输入0
    public static void deleteOrder_infro(int id, int orderId, int proId) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = null;
            if (id != 0) sql = "delete from order_infro where id= ?";
            else if (orderId != 0) sql = "delete from order_infro where orderId= ?";
            else sql = "delete from order_infro where int proId= ?";//编写sql

            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql);

            if (id != 0) st.setInt(1, id);
            else if (orderId != 0) st.setInt(1, orderId);
            else st.setInt(1, proId);             //手动赋值

            if (st.executeUpdate() > 0) System.out.println("delete from order_infro table  successful");
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }
    }

    //删除订单表中的数据。需要知道订单号或订单时间 ，知道订单号则时间任意，知道时间则订单号输入0
    public static void deletetOrder(int orderId, Calendar orderTime) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            String sql = null;
            if (orderId != 0) sql = "delete from `order` where orderId= ?";
            else sql = "delete from `order` where orderTime= ?";//编写sql
            conn = getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(sql); //预编译

            Date d = orderTime.getTime();

            if (orderId != 0) st.setInt(1, orderId);
            else st.setDate(1, new java.sql.Date(d.getTime())); //手动赋值


            if (st.executeUpdate() > 0) System.out.println("delete from order table successful");
            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.release(conn, st, rs);
        }

    }

}
