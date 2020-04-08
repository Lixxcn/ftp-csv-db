import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Li-Xiaoxu
 * @version 1.0
 * @date 2020/4/7 11:19
 */
public class Test {
    public static void main(String[] args) {
        //获取前一天
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMM");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,-1);
        Date date = calendar.getTime();
        String dataDir = format.format(date);
        String monthDir = format1.format(date);

        System.out.println(dataDir);
        System.out.println(monthDir);

        File file = new File("F:\\test\\test.csv");
        System.out.println(file.getAbsolutePath());
    }
}
