package com.inspur;


import com.inspur.dao.IInfoDataDao;
import com.inspur.domain.AttaData;
import com.inspur.domain.InfoData;
import com.inspur.domain.ReadAttaData;
import com.inspur.util.SftpUploadTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Li-Xiaoxu
 * @version 1.0
 * @date 2020/4/2 10:25
 */
public class ZipCsv {
    public static String vendorFileEncoding = "GBK";
    public static List<AttaData> attaDataList = new ArrayList<AttaData>();
    public static List<InfoData> infoDataList = new ArrayList<InfoData>();
    public static List<InfoData> deleteInfoDataList = new ArrayList<InfoData>();
    public static List<ReadAttaData> readAttaDataList = new ArrayList<ReadAttaData>();
    public static String zipDir = "";
    public static String localPath = "";
    public static String driver;
    public static String url;
    public static String username;
    public static String password;
    public static String sqlldrDir;
    private static String separator="~";
    private static Map<String, InfoData> receiptMap = new HashMap<String, InfoData>();
    private static InputStream in;
    private static SqlSession sqlSession;
    private static IInfoDataDao iInfoDataDao;

    private static Logger logger = Logger.getLogger(ZipCsv.class);


    public static void main(String[] args) throws Exception {
        init();
        File file = new File(localPath);
        String[] fileNames = file.list();
//        for (int i = 0; i < fileNames.length; i++) {
//            if(fileNames[i].endsWith("zip")){
//                logger.info("找到压缩文件：" + fileNames[i]);
//                zipUncompress(zipDir + File.separator + fileNames[i], zipDir);
//                logger.info("解压完成");
//            }
//        }
//        fileNames = file.list();
//        lastDayData();
        ParaProcess(fileNames);
        interDbattaData();


        close();
    }
    static void lastDayData() throws ClassNotFoundException, SQLException {
        //RECEIPT_ID '收据编号';
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(url, username, password);
        String newSql = "SELECT * FROM I_PMS_RECEIPT_INFO";
        PreparedStatement ps = conn.prepareStatement(newSql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            InfoData infoData = new InfoData();
            infoData.setINT_ID(rs.getInt("INT_ID"));
            infoData.setRECEIPT_ID(rs.getString("RECEIPT_ID"));
            receiptMap.put(infoData.getRECEIPT_ID(),infoData);
        }

    }

    static void interDbattaData(){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        if (!sqlldrDir.endsWith(File.separator)) {
            sqlldrDir = sqlldrDir + File.separator;
        }
        File f = new File(sqlldrDir);
        if (!f.exists()) {
            logger.info("目录不存在，开始创建。。。");
            f.mkdirs();
            logger.info("目录创建完毕");
        }
        String result = createDataFilAttaData();
        logger.info(result);
        String fileName = result.split("￥")[0];
        String fieldName = result.split("￥")[1];
        String ctlfileName = "ctl" + "_attaData_" + df.format(new Date())+ ".ctl";// 控制文件名
        String table_name = "I_PMS_RECEIPT_ATTA";
        String logfileName = table_name + df.format(new Date()) + ".log";
        stlFileWriter(sqlldrDir, fileName, table_name, fieldName, ctlfileName);
        Executive(username, password, url.split("@")[1], sqlldrDir, ctlfileName, logfileName, fileName, table_name);


        compare();

        result = createDataFileinfoData();
        logger.info(result);
        fileName = result.split("￥")[0];
        fieldName = result.split("￥")[1];
        ctlfileName = "ctl" + "_infoData_" + df.format(new Date())+ ".ctl";// 控制文件名
        table_name = "I_PMS_RECEIPT_INFO";
        logfileName = table_name + df.format(new Date()) + ".log";
        stlFileWriter(sqlldrDir, fileName, table_name, fieldName, ctlfileName);
        Executive(username, password, url.split("@")[1], sqlldrDir, ctlfileName, logfileName, fileName, table_name);

        Iterator<InfoData> iter = infoDataList.iterator();
        while (iter.hasNext()) {
            InfoData infoData = iter.next();
            iInfoDataDao.mergeInto(infoData);
        }


        result = createDataFilereadAttaData();
        logger.info(result);
        fileName = result.split("￥")[0];
        fieldName = result.split("￥")[1];
        ctlfileName = "ctl" + "_readAttaData_" + df.format(new Date())+ ".ctl";// 控制文件名
        table_name = "I_PMS_READ_ATTA";
        logfileName = table_name + df.format(new Date()) + ".log";
        stlFileWriter(sqlldrDir, fileName, table_name, fieldName, ctlfileName);
        Executive(username, password, url.split("@")[1], sqlldrDir, ctlfileName, logfileName, fileName, table_name);

    }

    public static void compare(){
        List<InfoData> infoDataListLast =  iInfoDataDao.findAll();
//        for(InfoData infoData : infoDataListLast){
//            logger.info(infoData);
//        }

        InfoData ModifyInfoData = new InfoData();
        for(InfoData infoDataLast : infoDataListLast){
            for(InfoData infoData : infoDataList){
                if(infoDataLast.getRECEIPT_ID().equalsIgnoreCase(infoData.getRECEIPT_ID())){
                    deleteInfoDataList.add(infoDataLast);
                    infoData.setINT_ID(infoDataLast.getINT_ID());
                    infoData.setCREATE_TIME(infoDataLast.getCREATE_TIME());
                    infoData.setUPDATE_USER("系统创建");
                    infoData.setUPDATE_TME(new Date());
                }
            }
        }
        logger.info("今日的infoData数据，RECEIPT_ID指标在昨天存在，今日需要更新的条数为" + deleteInfoDataList.size());
        logger.info("今日的infoData数据新增的条数为：" + (infoDataList.size() - deleteInfoDataList.size()));
        for(InfoData infoData : deleteInfoDataList){
//            iInfoDataDao.deleteByRECEIPT_ID(infoData.getRECEIPT_ID());
            iInfoDataDao.deleteByINT_ID(infoData.getINT_ID());
        }
        logger.info("需要更新的数据删除完毕");

    }
    public static String createDataFilereadAttaData(){
        DateFormat fileTimeDateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = fileTimeDateFormat.format(System.currentTimeMillis());
        if (!sqlldrDir.endsWith(File.separator)) {// 返回文件的分隔符
            sqlldrDir = sqlldrDir + File.separator;
        }
        File dataFile = new File(sqlldrDir + "readAttaData_"+ currentTime + "_" + ".dat");
        if (dataFile.exists()) {
            dataFile.delete();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(dataFile);
            dataFile.createNewFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("创建数据文件:" + dataFile.getAbsolutePath());
        boolean flag = false;

        Iterator<ReadAttaData> iter = readAttaDataList.iterator();
        StringBuilder ctlSql = new StringBuilder();
        String insert_time="";
        insert_time = sdf.format(new Date());
        String create_time = sdfDay.format(new Date());
        int num = 0;
        while (iter.hasNext()) {
            ReadAttaData readAttaData = iter.next();
            if (readAttaData == null) {
                logger.info("对象为空");
                continue;
            }
            ctlSql = new StringBuilder();
            StringBuilder sbSql = new StringBuilder();
            StringBuilder sbValues = new StringBuilder();
            sbSql.append("(RECEIPT_ID,ATTACHMENT_ID," +
                    "INT_ID,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TME" +
                    ")");
            ctlSql.append("(RECEIPT_ID,ATTACHMENT_ID," +
                    "CREATE_USER,CREATE_TIME \"to_date(:CREATE_TIME,'yyyy-mm-dd')\",UPDATE_USER,UPDATE_TME \"to_date(:UPDATE_TME,'yyyy-mm-dd hh24:mi:ss')\"" +
                    ",INT_ID \"SEQ_A.NEXTVAL\")");
            sbValues.append(readAttaData.getRECEIPT_ID()).append(separator);
            sbValues.append(readAttaData.getATTACHMENT_ID()).append(separator);
//            sbValues.append(num++).append(separator);
            sbValues.append("系统创建").append(separator);
            sbValues.append(create_time).append(separator);
            sbValues.append("系统创建").append(separator);
            sbValues.append(insert_time);

            if (!flag) {
                flag = true;
                pw.println(sbSql.toString());

            }
            pw.println(sbValues.toString());
            iter.remove();
        }
        pw.close();
        return dataFile.getAbsolutePath() + "￥" + ctlSql.toString();
    }
    public static String createDataFileinfoData(){
        DateFormat fileTimeDateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = fileTimeDateFormat.format(System.currentTimeMillis());
        if (!sqlldrDir.endsWith(File.separator)) {// 返回文件的分隔符
            sqlldrDir = sqlldrDir + File.separator;
        }
        File dataFile = new File(sqlldrDir + "infoData_"+ currentTime + "_" + ".dat");
        if (dataFile.exists()) {
            dataFile.delete();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(dataFile);
            dataFile.createNewFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("创建数据文件:" + dataFile.getAbsolutePath());
        boolean flag = false;

        Iterator<InfoData> iter = infoDataList.iterator();
        StringBuilder ctlSql = new StringBuilder();
        String insert_time="";
        insert_time = sdf.format(new Date());
        String create_time = sdfDay.format(new Date());
        int num = 0;
        while (iter.hasNext()) {
            InfoData infoData = iter.next();
            if (infoData == null) {
                logger.info("对象为空");
                continue;
            }
            ctlSql = new StringBuilder();
            StringBuilder sbSql = new StringBuilder();
            StringBuilder sbValues = new StringBuilder();
            sbSql.append("(CITY_NAME,COUNTY_NAME,SITE_NAME,SITE_NO,METER_NUM,MONTH_ID,READING_START,READING_END,MONTHLY_POWER,READING_START_TIME," +
                    "READING_END_TIME,AVG_POWER,CONTRACT_NO,ELEC_PRICE,ELEC_AMT,AVG_AMT,ATTACHMENT_ID,ATTACHMENT_ID_RECEIPT,ELEC_LOSE," +
                    "ELEC_TOTAL,RECEIPT_ID,SETTLE_POWER,PROVINCE,OWNER_CUST_CODE," +
                    "INT_ID,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TME" +
                    ")");
            ctlSql.append("(CITY_NAME,COUNTY_NAME,SITE_NAME,SITE_NO,METER_NUM,MONTH_ID,READING_START,READING_END,MONTHLY_POWER,READING_START_TIME \"to_date(:READING_START_TIME,'yyyy-mm-dd')\",READING_END_TIME \"to_date(:READING_END_TIME,'yyyy-mm-dd')\",AVG_POWER,CONTRACT_NO,ELEC_PRICE,ELEC_AMT,AVG_AMT,ATTACHMENT_ID,ATTACHMENT_ID_RECEIPT,ELEC_LOSE,ELEC_TOTAL,RECEIPT_ID,SETTLE_POWER,PROVINCE,OWNER_CUST_CODE," +
                    "CREATE_USER,CREATE_TIME \"to_date(:CREATE_TIME,'yyyy-mm-dd')\",UPDATE_USER,UPDATE_TME \"to_date(:UPDATE_TME,'yyyy-mm-dd hh24:mi:ss')\"" +
                    ",INT_ID \"SEQ_B.NEXTVAL\")");
//            if(receiptMap.containsKey(infoData.getRECEIPT_ID())){
//                sbValues.append(infoData.getCITY_NAME()).append(separator);
//                sbValues.append(infoData.getCOUNTY_NAME()).append(separator);
//                sbValues.append(infoData.getSITE_NAME()).append(separator);
//                sbValues.append(infoData.getSITE_NO()).append(separator);
//                sbValues.append(infoData.getMETER_NUM()).append(separator);
//                sbValues.append(infoData.getMONTH_ID()).append(separator);
//                sbValues.append(infoData.getREADING_START()).append(separator);
//                sbValues.append(infoData.getREADING_END()).append(separator);
//                sbValues.append(infoData.getMONTHLY_POWER()).append(separator);
//                sbValues.append(infoData.getREADING_START_TIME()).append(separator);//2020-03-01
//                sbValues.append(infoData.getREADING_END_TIME()).append(separator);//2020-03-01
//                sbValues.append(infoData.getAVG_POWER()).append(separator);
//                sbValues.append(infoData.getCONTRACT_NO()).append(separator);
//                sbValues.append(infoData.getELEC_PRICE()).append(separator);
//                sbValues.append(infoData.getELEC_AMT()).append(separator);
//                sbValues.append(infoData.getAVG_AMT()).append(separator);
//                sbValues.append(infoData.getATTACHMENT_ID()).append(separator);
//                sbValues.append(infoData.getATTACHMENT_ID_RECEIPT()).append(separator);
//                sbValues.append(infoData.getELEC_LOSE()).append(separator);
//                sbValues.append(infoData.getELEC_TOTAL()).append(separator);
//                sbValues.append(infoData.getRECEIPT_ID()).append(separator);
//                sbValues.append(infoData.getSETTLE_POWER()).append(separator);
//                sbValues.append(infoData.getPROVINCE()).append(separator);
//                sbValues.append(infoData.getOWNER_CUST_CODE()).append(separator);
////                sbValues.append(num).append(separator);
//                sbValues.append("系统创建").append(separator);
//                sbValues.append(create_time).append(separator);
//                sbValues.append("系统创建").append(separator);
//                sbValues.append(insert_time);
//            }else {
            sbValues.append(infoData.getCITY_NAME()).append(separator);
            sbValues.append(infoData.getCOUNTY_NAME()).append(separator);
            sbValues.append(infoData.getSITE_NAME()).append(separator);
            sbValues.append(infoData.getSITE_NO()).append(separator);
            sbValues.append(infoData.getMETER_NUM()).append(separator);
            sbValues.append(infoData.getMONTH_ID()).append(separator);
            sbValues.append(infoData.getREADING_START() == null ? "" : infoData.getREADING_START()).append(separator);
            sbValues.append(infoData.getREADING_END()== null ? "" :infoData.getREADING_END()).append(separator);
            sbValues.append(infoData.getMONTHLY_POWER()== null ? "" :infoData.getMONTHLY_POWER()).append(separator);
            sbValues.append(sdfDay.format(infoData.getREADING_START_TIME())).append(separator);//2020-03-01
            sbValues.append(sdfDay.format(infoData.getREADING_END_TIME())).append(separator);//2020-03-01
            sbValues.append(infoData.getAVG_POWER() == null ? "" : infoData.getAVG_POWER()).append(separator);
            sbValues.append(infoData.getCONTRACT_NO()).append(separator);
            sbValues.append(infoData.getELEC_PRICE() == null ? "" : infoData.getELEC_PRICE()).append(separator);
            sbValues.append(infoData.getELEC_AMT() == null ? "" : infoData.getELEC_AMT()).append(separator);
            sbValues.append(infoData.getAVG_AMT() == null ? "" : infoData.getAVG_AMT()).append(separator);
            sbValues.append(infoData.getATTACHMENT_ID()== null ? "" :infoData.getATTACHMENT_ID()).append(separator);
            sbValues.append(infoData.getATTACHMENT_ID_RECEIPT()== null ? "" :infoData.getATTACHMENT_ID_RECEIPT()).append(separator);
            sbValues.append(infoData.getELEC_LOSE()== null ? "" :infoData.getELEC_LOSE()).append(separator);
            sbValues.append(infoData.getELEC_TOTAL()== null ? "" :infoData.getELEC_TOTAL()).append(separator);
            sbValues.append(infoData.getRECEIPT_ID()).append(separator);
            sbValues.append(infoData.getSETTLE_POWER() == null ? "" : infoData.getSETTLE_POWER()).append(separator);
            sbValues.append(infoData.getPROVINCE()).append(separator);
            sbValues.append(infoData.getOWNER_CUST_CODE()).append(separator);
//                sbValues.append(num).append(separator);
            sbValues.append("系统创建").append(separator);
            sbValues.append(sdfDay.format(infoData.getCREATE_TIME())).append(separator);
            sbValues.append(infoData.getUPDATE_USER()).append(separator);
            sbValues.append(infoData.getUPDATE_TME() == null ? "" : fileTimeDateFormat.format(infoData.getUPDATE_TME()));
//            }
            //CITY_NAME	COUNTY_NAME	SITE_NAME	SITE_NO	METER_NUM	MONTH_ID	READING_START	READING_END	MONTHLY_POWER	READING_START_TIME	READING_END_TIME	AVG_POWER	CONTRACT_NO	ELEC_PRICE	ELEC_AMT	AVG_AMT	ATTACHMENT_ID	ATTACHMENT_ID_RECEIPT	ELEC_LOSE	ELEC_TOTAL	RECEIPT_ID	SETTLE_POWER	PROVINCE	OWNER_CUST_CODE	INT_ID	CREATE_USER	CREATE_TIME	UPDATE_USER	UPDATE_TME)

            num++;
            if (!flag) {
                flag = true;
                pw.println(sbSql.toString());

            }
            pw.println(sbValues.toString());
            iter.remove();
        }
        pw.close();
        return dataFile.getAbsolutePath() + "￥" + ctlSql.toString();
    }

    public static String createDataFilAttaData(){
        DateFormat fileTimeDateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = fileTimeDateFormat.format(System.currentTimeMillis());
        if (!sqlldrDir.endsWith(File.separator)) {// 返回文件的分隔符
            sqlldrDir = sqlldrDir + File.separator;
        }
        File dataFile = new File(sqlldrDir + "attaData_"+ currentTime + "_" + ".dat");
        if (dataFile.exists()) {
            dataFile.delete();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(dataFile);
            dataFile.createNewFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("创建数据文件:" + dataFile.getAbsolutePath());
        boolean flag = false;

        Iterator<AttaData> iter = attaDataList.iterator();
        StringBuilder ctlSql = new StringBuilder();
        String insert_time="";
        insert_time = sdf.format(new Date());
        String create_time = sdfDay.format(new Date());
        int num = 0;
        while (iter.hasNext()) {
            AttaData attaData = iter.next();
            if (attaData == null) {
                logger.info("对象为空");
                continue;
            }
            ctlSql = new StringBuilder();
            StringBuilder sbSql = new StringBuilder();
            StringBuilder sbValues = new StringBuilder();
            sbSql.append("(ATTACHMENT_ID,ATTACHMENT_REAL_NAME,ATTACHMENT_SAVE_NAME,ATTACHMENT_PATH,FILE_SIZE," +
                    "INT_ID,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TME" +
                    ")");
            ctlSql.append("(ATTACHMENT_ID,ATTACHMENT_REAL_NAME,ATTACHMENT_SAVE_NAME,ATTACHMENT_PATH,FILE_SIZE," +
                    "CREATE_USER,CREATE_TIME \"to_date(:CREATE_TIME,'yyyy-mm-dd')\",UPDATE_USER,UPDATE_TME \"to_date(:UPDATE_TME,'yyyy-mm-dd hh24:mi:ss')\"" +
                    ",INT_ID \"seq_t_sys_attachment.NEXTVAL\")");//SEQ_C更换为新序列的值 seq_t_sys_attachment 2020年5月27日11:26:50
            sbValues.append(attaData.getATTACHMENT_ID()).append(separator);
            sbValues.append(attaData.getATTACHMENT_REAL_NAME()).append(separator);
            sbValues.append(attaData.getATTACHMENT_SAVE_NAME()).append(separator);
            sbValues.append(attaData.getATTACHMENT_PATH()).append(separator);
            sbValues.append(attaData.getFILE_SIZE()).append(separator);
//            sbValues.append(num++).append(separator);
            sbValues.append("系统创建").append(separator);
            sbValues.append(create_time).append(separator);
            sbValues.append("系统创建").append(separator);
            sbValues.append(insert_time);

            if (!flag) {
                flag = true;
                pw.println(sbSql.toString());

            }
            pw.println(sbValues.toString());
            iter.remove();
        }
        pw.close();
        return dataFile.getAbsolutePath() + "￥" + ctlSql.toString();
    }
    /**
     * * 写控制文件.ctl
     * @param fileRoute 数据文件地址路径
     * @param fileName 数据文件名
     * @param tableName 表名
     * @param fieldName 要写入表的字段
     * @param ctlfileName 控制文件名
     */
    public static void stlFileWriter(String fileRoute,String fileName,String tableName,String fieldName,String ctlfileName)
    {
        FileWriter fw = null;
        String strctl = "OPTIONS (skip=0)" +
                " LOAD DATA CHARACTERSET 'UTF8' INFILE '"+""+fileName+"'" +
                " APPEND INTO TABLE "+tableName+"" +
                " FIELDS TERMINATED BY '~'" +
                " OPTIONALLY  ENCLOSED BY \"'\"" +
                " TRAILING NULLCOLS "+fieldName+"";
        File f = new File(fileRoute);
        if(!f.exists()){
            logger.info("目录不存在，开始创建。。。");
            f.mkdirs();
            logger.info("目录创建完毕");
        }
        try {
            fw = new FileWriter(fileRoute+""+ctlfileName);
            fw.write(strctl);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            try
            {
                fw.flush();
                fw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }
    static void init() throws Exception {
        Properties properties = new Properties();
//        BufferedReader bufferedReader = new BufferedReader(new FileReader("E:\\IDE\\IDEA\\sftpzipcxv\\src\\main\\resources\\config.properties"));
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/app/program_appuser/conf/config.properties"));
        properties.load(bufferedReader);
        localPath = properties.getProperty("localPath");
        driver = properties.getProperty("driver");
        url = properties.getProperty("url");
        username = properties.getProperty("username");
        password = properties.getProperty("password");
        sqlldrDir = properties.getProperty("sqlldrDir");

        in = Resources.getResourceAsStream("SqlMapConfig.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        sqlSession = factory.openSession();
        iInfoDataDao = sqlSession.getMapper(IInfoDataDao.class);

        String sftp_ip = properties.getProperty("sftp_ip");
        String sftp_username = properties.getProperty("sftp_username");
        String sftp_password = properties.getProperty("sftp_password");
        int sftp_port = Integer.parseInt(properties.getProperty("sftp_port"));
        String sftp_path = properties.getProperty("sftp_path");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat formatMonth = new SimpleDateFormat("yyyyMM");

//        String dataDir = format.format(new Date());
        //获取前一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,-1);
        Date date = calendar.getTime();
        String dataDir = format.format(date);
        String monthDir = formatMonth.format(date);

        //2020年4月4日16:37:56 测试
//        dataDir = "20200403";
        sftp_path = sftp_path + File.separator + dataDir;
        localPath = localPath + File.separator + dataDir;
        String readPath = "/app/fileData/ftpData/meterData" + File.separator + monthDir;
        String receiptPath = "/app/fileData/ftpData/receiptData" + File.separator + monthDir;
        File file = new File(localPath);
        File readFile = new File(readPath);
        File receiptFile = new File(receiptPath);

        if(!file.exists()){
            file.mkdirs();
        }
        if(!readFile.exists()){
            logger.info("该路径：" +readFile.getAbsolutePath() + "不存在，进行创建！");
            file.mkdirs();
            logger.info("创建完毕！");
        }
        if(!receiptFile.exists()){
            logger.info("该路径："+ receiptFile.getAbsolutePath() + "不存在，进行创建！");
            file.mkdirs();
            logger.info("创建完毕！");
        }



//        SFTP.downloadSftpFile(sftp_ip,sftp_username,sftp_password,sftp_port,sftp_path,localPath,"infoData_20200403.csv");
//        SFTP.downloadSftpFile(sftp_ip,sftp_username,sftp_password,sftp_port,sftp_path,localPath,"attaData_20200403.csv");
//        SFTP.downloadSftpFile(sftp_ip,sftp_username,sftp_password,sftp_port,sftp_path,localPath,"readAttaData_20200403.csv");
        SftpUploadTest sftpUploadTest = new SftpUploadTest(sftp_username,sftp_password,sftp_ip,sftp_port);

        sftpUploadTest.downloadFile(sftp_path + File.separator + "infoData_"+dataDir+".csv",localPath+ File.separator);
        sftpUploadTest.downloadFile(sftp_path + File.separator + "attaData_"+dataDir+".csv",localPath+ File.separator);
        sftpUploadTest.downloadFile(sftp_path + File.separator + "readAttaData_"+dataDir+".csv",localPath+ File.separator);
        sftpUploadTest.downloadFile(sftp_path + File.separator + "read_"+dataDir+".zip",localPath+ File.separator);
        sftpUploadTest.downloadFile(sftp_path + File.separator + "receipt_"+dataDir+".zip",localPath+ File.separator);
        copy(localPath + File.separator + "infoData_"+dataDir+".csv", "/app/fileData/ftpData/infoData"+ File.separator + "infoData_"+dataDir+".csv");
        copy(localPath + File.separator + "attaData_"+dataDir+".csv", "/app/fileData/ftpData/attData"+ File.separator + "attaData_"+dataDir+".csv");
        copy(localPath + File.separator + "readAttaData_"+dataDir+".csv", "/app/fileData/ftpData/attData"+ File.separator + "readAttaData_"+dataDir+".csv");
//        copy(localPath + File.separator + "read_"+dataDir+".zip", "/app/fileData/ftpData/meterData"+ File.separator + "read_"+dataDir+".zip");
//        copy(localPath + File.separator + "receipt_"+dataDir+".zip", "/app/fileData/ftpData/receiptData"+ File.separator + "receipt_"+dataDir+".zip");
        zipUncompress(localPath + File.separator + "read_"+dataDir+".zip" , readPath);
        zipUncompress(localPath + File.separator + "receipt_"+dataDir+".zip", receiptPath);
    }
    private static void copy(String src,String target) {
        //创建源文件，和目标文件
        File srcFile = new File(src);
        File targetFile = new File(target);
        //创建输入输出流
        InputStream in =  null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(targetFile);
            byte[] bytes = new byte[1024];
            int len = -1;
            while((len = in.read(bytes))!=-1) {
                out.write(bytes,0,len);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(in != null) in.close();
                if(out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void close() throws IOException {
        sqlSession.commit();
        sqlSession.close();
        in.close();
    }

    static void ParaProcess(String[] fileNames){
        for (int i = 0; i < fileNames.length; i++) {
            //attaData_20200329.csv
            if(fileNames[i].contains("attaData") && fileNames[i].endsWith("csv")){
                logger.info("解析attaData文件："+ fileNames[i]);
                paraAttaCsv(localPath + File.separator + fileNames[i]);
                logger.info("attaData文件解析完毕，共" + attaDataList.size() + "条");
//                for(AttaData attaData : attaDataList){
//                    logger.info(attaData);
//                }
            }
            if(fileNames[i].contains("infoData") && fileNames[i].endsWith("csv")){
                logger.info("解析infoData文件："+ fileNames[i]);
                paraInfoCsv(localPath + File.separator + fileNames[i]);
                logger.info("infoData文件解析完毕，共" + infoDataList.size() + "条");
//                for(InfoData infoData : infoDataList){
//                    logger.info(infoData);
//                }
            }
            if(fileNames[i].contains("readAttaData") && fileNames[i].endsWith("csv")){
                logger.info("解析readAttaData文件："+ fileNames[i]);
                paraReadCsv(localPath + File.separator + fileNames[i]);
                logger.info("readAttaData文件解析完毕，共" + readAttaDataList.size() + "条");
//                for(ReadAttaData readAttaData : readAttaDataList){
//                    logger.info(readAttaData);
//                }
            }
        }
    }
    static void paraReadCsv(String fileName){
        String[] headNames = null; // 每列列头的名字
        String[] datas; // 每行数据
        BufferedReader reader = null;
        try {
            // reader = new BufferedReader(new FileReader(new File(fileName)));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), vendorFileEncoding));
            int i = 0;// 判断是否是第一行数据
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (i == 0) {
                    headNames = line.split("\\|", -1);// 列头赋值
                    if (headNames.length == 1) {
                        headNames = headNames[0].split("\\^\\^", -1);
                    }
                } else {
                    datas = line.split("\\|", -1);// 行数据赋值
                    if (datas.length == 1) {
                        datas = datas[0].split("\\^\\^", -1);
//                        datas = StringTool.splitCSV(datas[0]);
                    }
                    if (headNames.length == datas.length) {
                        //ATTACHMENT_ID^^ATTACHMENT_REAL_NAME^^ATTACHMENT_SAVE_NAME^^ATTACHMENT_PATH^^FILE_SIZE
                        ReadAttaData readAttaData = new ReadAttaData();
                        for (int j = 0; j < headNames.length; j++) {
                            if (("RECEIPT_ID").equalsIgnoreCase(headNames[j])) {
                                readAttaData.setRECEIPT_ID(datas[j].trim());
                            } else if("ATTACHMENT_ID".equalsIgnoreCase(headNames[j])){
                                readAttaData.setATTACHMENT_ID(datas[j].trim());
                            }
                        }
                        readAttaDataList.add(readAttaData);
                    }
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static void paraInfoCsv(String fileName){
        String[] headNames = null; // 每列列头的名字
        String[] datas; // 每行数据
        BufferedReader reader = null;
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // reader = new BufferedReader(new FileReader(new File(fileName)));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), vendorFileEncoding));
            int i = 0;// 判断是否是第一行数据
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (i == 0) {
                    headNames = line.split("\\|", -1);// 列头赋值
                    if (headNames.length == 1) {
                        headNames = headNames[0].split("\\^\\^", -1);
                    }
                } else {
                    datas = line.split("\\|", -1);// 行数据赋值
                    if (datas.length == 1) {
                        datas = datas[0].split("\\^\\^", -1);
//                        datas = StringTool.splitCSV(datas[0]);
                    }
                    if (headNames.length == datas.length) {
                        //ATTACHMENT_ID^^ATTACHMENT_REAL_NAME^^ATTACHMENT_SAVE_NAME^^ATTACHMENT_PATH^^FILE_SIZE
                        InfoData infoData = new InfoData();
                        for (int j = 0; j < headNames.length; j++) {
                            if (("CITY_NAME").equalsIgnoreCase(headNames[j])) {
                                infoData.setCITY_NAME(datas[j].trim());
                            } else if("COUNTY_NAME".equalsIgnoreCase(headNames[j])){
                                infoData.setCOUNTY_NAME(datas[j].trim());
                            } else if("SITE_NAME".equalsIgnoreCase(headNames[j])){
                                infoData.setSITE_NAME(datas[j].trim());
                            } else if("SITE_NO".equalsIgnoreCase(headNames[j])){
                                infoData.setSITE_NO(datas[j].trim());
                            } else if("METER_NUM".equalsIgnoreCase(headNames[j])){
                                infoData.setMETER_NUM(datas[j].trim());
                            } else if("MONTH_ID".equalsIgnoreCase(headNames[j])){
                                infoData.setMONTH_ID(datas[j].trim());
                            } else if("READING_START".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setREADING_START(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("READING_END".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setREADING_END(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("MONTHLY_POWER".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setMONTHLY_POWER(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("READING_START_TIME".equalsIgnoreCase(headNames[j])){
                                Date date = sdfDay.parse(datas[j].trim());
                                infoData.setREADING_START_TIME(date);
                            } else if("READING_END_TIME".equalsIgnoreCase(headNames[j])){
                                Date date = sdfDay.parse(datas[j].trim());
                                infoData.setREADING_END_TIME(date);
                            } else if("AVG_POWER".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setAVG_POWER(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("CONTRACT_NO".equalsIgnoreCase(headNames[j])){
                                infoData.setCONTRACT_NO(datas[j].trim());
                            } else if("ELEC_PRICE".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setELEC_PRICE(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("ELEC_AMT".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setELEC_AMT(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("AVG_AMT".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setAVG_AMT(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("ATTACHMENT_ID".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setATTACHMENT_ID(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("ATTACHMENT_ID_RECEIPT".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setATTACHMENT_ID_RECEIPT(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {
                                }
                            } else if("ELEC_LOSE".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setELEC_LOSE(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {
                                }
                            } else if("ELEC_TOTAL".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setELEC_TOTAL(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("RECEIPT_ID".equalsIgnoreCase(headNames[j])){
                                infoData.setRECEIPT_ID(datas[j].trim());
                            } else if("SETTLE_POWER".equalsIgnoreCase(headNames[j])){
                                try {
                                    infoData.setSETTLE_POWER(new BigDecimal(datas[j].trim()));
                                } catch (Exception e) {

                                }
                            } else if("PROVINCE".equalsIgnoreCase(headNames[j])){
                                infoData.setPROVINCE(datas[j].trim());
                            } else if("OWNER_CUST_CODE".equalsIgnoreCase(headNames[j])){
                                infoData.setOWNER_CUST_CODE(datas[j].trim());
                            }
                        }
                        Date date = new Date();
                        infoData.setCREATE_USER("系统创建");
                        infoData.setCREATE_TIME(date);
//                        infoData.setUPDATE_USER("系统创建");
//                        infoData.setUPDATE_TME(date);
                        infoData.setUPDATE_USER("");
                        infoData.setUPDATE_TME(null);
                        infoDataList.add(infoData);
                    }
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static void paraAttaCsv(String fileName){
        String[] headNames = null; // 每列列头的名字
        String[] datas; // 每行数据
        BufferedReader reader = null;
        try {
            // reader = new BufferedReader(new FileReader(new File(fileName)));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), vendorFileEncoding));
            int i = 0;// 判断是否是第一行数据
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (i == 0) {
                    headNames = line.split("\\|", -1);// 列头赋值
                    if (headNames.length == 1) {
                        headNames = headNames[0].split("\\^\\^", -1);
                    }
                } else {
                    datas = line.split("\\|", -1);// 行数据赋值
                    if (datas.length == 1) {
                        datas = datas[0].split("\\^\\^", -1);
//                        datas = StringTool.splitCSV(datas[0]);
                    }
                    if (headNames.length == datas.length) {
                        //ATTACHMENT_ID^^ATTACHMENT_REAL_NAME^^ATTACHMENT_SAVE_NAME^^ATTACHMENT_PATH^^FILE_SIZE
                        AttaData attaData = new AttaData();
                        for (int j = 0; j < headNames.length; j++) {
                            if (("ATTACHMENT_ID").equalsIgnoreCase(headNames[j])) {
                                attaData.setATTACHMENT_ID(datas[j].trim());
                            } else if("ATTACHMENT_REAL_NAME".equalsIgnoreCase(headNames[j])){
                                attaData.setATTACHMENT_REAL_NAME(datas[j].trim());
                            } else if("ATTACHMENT_SAVE_NAME".equalsIgnoreCase(headNames[j])) {
                                attaData.setATTACHMENT_SAVE_NAME(datas[j].trim());
                            } else if("ATTACHMENT_PATH".equalsIgnoreCase(headNames[j])){
                                attaData.setATTACHMENT_PATH(datas[j].trim());
                            } else if("FILE_SIZE".equalsIgnoreCase(headNames[j])){
                                attaData.setFILE_SIZE(datas[j].trim());
                            }
                        }
                        attaDataList.add(attaData);
                    }
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void zipUncompress(String inputFile,String destDirPath) throws Exception {
        File srcFile = new File(inputFile);//获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        ZipFile zipFile = new ZipFile(srcFile);//创建压缩文件对象
        //开始解压
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            // 如果是文件夹，就创建个文件夹
            if (entry.isDirectory()) {
                String dirPath = destDirPath + "/" + entry.getName();
//                srcFile.mkdirs();
                (new File(dirPath)).mkdirs();
            } else {
                // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                File targetFile = new File(destDirPath + "/" + entry.getName());
                // 保证这个文件的父文件夹必须要存在
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                targetFile.createNewFile();
                // 将压缩文件内容写入到这个文件中
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(targetFile);
                int len;
                byte[] buf = new byte[1024];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                // 关流顺序，先打开的后关闭
                fos.close();
                is.close();
            }
        }
    }
    public static void handBadFile(File badFile,String tableName) {
        BufferedReader ctlBufferedReader;
        BufferedReader bufferedReader;
        FileReader ctlFileReader;
        FileReader fileReader;
        String ctlFilePath = badFile.getAbsolutePath();
        logger.info("badFilePath:" + ctlFilePath);
        ctlFilePath = ctlFilePath.replace(".bad", ".ctl").replace(".BAD", ".ctl");
        File ctlFile = new File(ctlFilePath);
        if (ctlFile.exists()) {
            try {
                ctlFileReader = new FileReader(ctlFile);
                ctlBufferedReader = new BufferedReader(ctlFileReader);
                String str = null;
                String terminal = null;
                StringBuffer sb = new StringBuffer();
                String tmpColumn = null;
                while ((str = ctlBufferedReader.readLine()) != null) {
                    if (str.contains("fields terminated by") || str.contains("FIELDS TERMINATED BY")) {
                        int a = str.indexOf("\"");
                        int b = str.lastIndexOf("\"");
                        terminal = str.substring(a + 1, b);
                        logger.info("terminal:" + terminal);
                        while ((str = ctlBufferedReader.readLine()) != null) {
                            sb.append(str);
                        }
                    }
                }
                tmpColumn = sb.toString().replace("\"to_date(:time_stamp,'yyyy-mm-dd')\"", "")
                        .replace("\"to_date(:insert_time,'yyyy-mm-dd hh24:mi:ss')\"", "").replace(" ", "");
                logger.info("tmpColumn: " + tmpColumn);
                String column = tmpColumn.replace("(", "").replace(")", "");
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection conn = DriverManager.getConnection(url, username, password);
//                Connection conn = dbDriver.getConn();
                StringBuffer sqlSb = new StringBuffer();
                sqlSb.append("insert into "+tableName+"(");
                sqlSb.append(column);
                sqlSb.append(") values(");

                String[] columns = column.split(",");

                fileReader = new FileReader(badFile);
                bufferedReader = new BufferedReader(fileReader);
                String line;
                int count = 1;
                while ((line = bufferedReader.readLine()) != null) {
                    logger.info("开始插入第" + count + "条数据");
                    if (line.endsWith(terminal)) {
                        line = line + " ";
                    }
                    String values[] = line.split(terminal);
                    StringBuffer valueBuffer = new StringBuffer();
                    if (values.length == columns.length) {
                        for (int i = 0; i < columns.length; i++) {
                            String value = values[i];
                            valueBuffer.append("'");
                            valueBuffer.append(value);
                            valueBuffer.append("'");
                            if (i < columns.length - 1) {
                                valueBuffer.append(",");
                            }
                        }

                        String insertSql = sb.toString().concat(valueBuffer.toString()).concat(")");
                        logger.info("SQL:" + insertSql);
                        Statement stm = conn.createStatement();
                        stm.executeUpdate(insertSql);
                        stm.close();
                    }
                }
            } catch (FileNotFoundException e) {
                logger.info("文件没有找到"+e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    public static void Executive(String user,String psw,String Database,String fileRoute,String ctlfileName,String logfileName,String fileName,String tableName)
    {
        InputStream ins = null;
        //要执行的DOS命令
        String dos="sqlldr "+user+"/"+psw+"@"+Database+" skip=1 control="+fileRoute+""+ctlfileName+" log="+fileRoute+""+logfileName +" errors=9999 rows=5000 bindsize=20971520 readsize=20971520";
        String dosOut="sqlldr  skip=1 control="+fileRoute+""+ctlfileName+" log="+fileRoute+""+logfileName +" errors=9999 rows=5000 bindsize=20971520 readsize=20971520";

        logger.info(dosOut);
        //Linux环境下注释掉不需要CMD 直接执行DOS就可以
        //String[] cmd = new String[]
        //{ "cmd.exe", "/C", dos }; // Windows环境 命令
        try
        {
            Process process = Runtime.getRuntime().exec(dos);

//            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "Error");
//            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "Output");
//            errorGobbler.start();
//            outputGobbler.start();
//            process.waitFor();
            ins = process.getInputStream(); // 获取执行cmd命令后的信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                String msg = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                logger.info(msg); // 输出
            }
            int exitValue = process.waitFor();
            if(exitValue==0)
            {
                logger.info("返回值：" + exitValue+"\n数据导入成功");

            }else
            {
                logger.info("返回值：" + exitValue+"\n数据导入失败");

            }

            process.getOutputStream().close(); // 关闭
        }

        catch (Exception e)
        {
            logger.info("Executive="+e.getMessage());
        }
        //处理错误文件
        if(fileName.contains(".dat")){
            fileName = fileName.replace(".dat", ".bad");
            File badFile = new File(fileName);
            if (badFile.exists()) {
                logger.info(badFile.getAbsolutePath() + "存在,开始处理问题数据");
                handBadFile(badFile,tableName);
            } else {
                logger.info(badFile.getAbsolutePath() + "不存在");
            }
        }else{
            logger.info("文件错误");
        }
    }

}
