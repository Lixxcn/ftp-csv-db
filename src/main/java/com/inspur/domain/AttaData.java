package com.inspur.domain;

import java.io.Serializable;

/**
 * @author Li-Xiaoxu
 * @version 1.0
 * @date 2020/4/2 11:25
 */
public class AttaData implements Serializable {
    String ATTACHMENT_ID ;
    String ATTACHMENT_REAL_NAME;
    String ATTACHMENT_SAVE_NAME;
    String ATTACHMENT_PATH;
    String FILE_SIZE;

    public String getATTACHMENT_ID() {
        return ATTACHMENT_ID;
    }

    public void setATTACHMENT_ID(String ATTACHMENT_ID) {
        this.ATTACHMENT_ID = ATTACHMENT_ID;
    }

    public String getATTACHMENT_REAL_NAME() {
        return ATTACHMENT_REAL_NAME;
    }

    public void setATTACHMENT_REAL_NAME(String ATTACHMENT_REAL_NAME) {
        this.ATTACHMENT_REAL_NAME = ATTACHMENT_REAL_NAME;
    }

    public String getATTACHMENT_SAVE_NAME() {
        return ATTACHMENT_SAVE_NAME;
    }

    public void setATTACHMENT_SAVE_NAME(String ATTACHMENT_SAVE_NAME) {
        this.ATTACHMENT_SAVE_NAME = ATTACHMENT_SAVE_NAME;
    }

    public String getATTACHMENT_PATH() {
        return ATTACHMENT_PATH;
    }

    public void setATTACHMENT_PATH(String ATTACHMENT_PATH) {
        this.ATTACHMENT_PATH = ATTACHMENT_PATH;
    }

    public String getFILE_SIZE() {
        return FILE_SIZE;
    }

    public void setFILE_SIZE(String FILE_SIZE) {
        this.FILE_SIZE = FILE_SIZE;
    }

    @Override
    public String toString() {
        return "AttaData{" +
                "ATTACHMENT_ID='" + ATTACHMENT_ID + '\'' +
                ", ATTACHMENT_REAL_NAME='" + ATTACHMENT_REAL_NAME + '\'' +
                ", ATTACHMENT_SAVE_NAME='" + ATTACHMENT_SAVE_NAME + '\'' +
                ", ATTACHMENT_PATH='" + ATTACHMENT_PATH + '\'' +
                ", FILE_SIZE='" + FILE_SIZE + '\'' +
                '}';
    }
}
