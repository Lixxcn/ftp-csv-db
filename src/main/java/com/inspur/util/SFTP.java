package com.inspur.util;

import java.io.File;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SFTP {

    public static void main (String[] args) {
        /*
         * 下载文件
         *
         * @param sftp_ip SFTP IP地址
         * @param sftp_username SFTP 用户名
         * @param sftp_password SFTP用户名密码
         * @param sftp_port SFTP端口
         * @param sftp_path SFTP服务器中文件所在路径
         * @param localPath 下载到本地的路径
         * @param fileName 下载文件的名称
         */
        String sftp_ip = "10.10.93.78";
        String sftp_username = "digi";
        String sftp_password = "123456";
        int sftp_port = 22;
        String sftp_path = "/";
        String localPath = "D:/";
        String fileName = "135.txt";
        try {
            downloadSftpFile(sftp_ip, sftp_username, sftp_password, sftp_port, sftp_path, localPath, fileName);
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public static void downloadSftpFile(String sftp_ip, String sftp_username, String sftp_password, int sftp_port, String sftp_path, String localPath, String fileName) throws JSchException {

        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();
        session = jsch.getSession(sftp_username, sftp_ip, sftp_port);
        session.setPassword(sftp_password);
        session.setTimeout(1000);
        Properties config = new Properties();
        //设置不用检查HostKey，设成yes，一旦计算机的密匙发生变化，就拒绝连接。
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp chSftp = (ChannelSftp) channel;

        String localFilePath = localPath + File.separator;

        try {
            //使用ChannelSftp的get(文件名，本地路径{包含文件名})方法下载文件
            chSftp.get(fileName, localFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("download error.");
        } finally {
            chSftp.quit();
            channel.disconnect();
            session.disconnect();
        }
    }
}