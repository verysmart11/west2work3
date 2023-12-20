import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;


public class Test {
    public static void main(String[] args) throws SQLException {
        Calendar orderTime = Calendar.getInstance();
        //查数据
        JdbcUtils.selectThreeTable("orderId", "orderTime", "orderPrice", "orderNum", "`order`", "orderId", 886001);
        JdbcUtils.selectThreeTable("orderId", "orderTime", "orderPrice", "orderNum", "`order`", "orderTime", "2023-11-18");
        JdbcUtils.selectThreeTable("proId", "proName", "proPrice", null, "product", "proId", 100005);
        JdbcUtils.selectThreeTable("orderId","proId","proNum",null,"order_infro","id",540011);
        //修改数据
        orderTime.set(2019, 6, 29);
        JdbcUtils.updateOrder(886004,orderTime);
        JdbcUtils.updateOrder_infro(540033, 886000, 0, 0);
        JdbcUtils.updateProduct(100000, "电灯", 31);//失败的
        JdbcUtils.updateProduct(100009, "硬盘", 31);
        //插入新数据
        orderTime.set(2023,12,17);
        JdbcUtils.insertOrder(orderTime);
        JdbcUtils.insertOrder_infro(886008, 100013, 18);
        JdbcUtils.insertOrder_infro(886000, 100013, 18);  //orderId don't exist
        JdbcUtils.insertOrder_infro(886008, 100000, 18);  //proId don't exist
        JdbcUtils.insertProduct("帽子", 32);
        //删除数据
        orderTime.set(2023,12,17);
        JdbcUtils.deletetOrder(886000, orderTime);
        JdbcUtils.deleteProduct(100000, null);
        JdbcUtils.deleteOrder_infro(540031, 0, 0);








    }
}
