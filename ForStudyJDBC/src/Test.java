import java.util.Calendar;
import java.util.Date;


public class Test {
    public static void main(String[] args) {
        Calendar orderTime = Calendar.getInstance();

        JdbcUtils.select("orderId", "orderTime", "orderPrice", "orderNum", "`order`", "orderId", 886000);

        orderTime.set(2019, 6, 29);

        JdbcUtils.updateOrder(886004,orderTime);
        JdbcUtils.updateOrder_infro(540033, 886000, 0, 0);
        JdbcUtils.updateProduct(100000, "电灯", 31);

        orderTime.set(2023,12,17);
        JdbcUtils.insertOrder(orderTime);
        JdbcUtils.insertOrder_infro(886008, 100013, 18);
        JdbcUtils.insertProduct("帽子", 32);
        JdbcUtils.deleteOrder_infro(540031, 0, 0);

        orderTime.set(2023,12,17);
        JdbcUtils.deletetOrder(886000, orderTime);
        JdbcUtils.deleteProduct(100000, null);

    }
}
