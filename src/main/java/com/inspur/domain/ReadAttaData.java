package com.inspur.domain;

import java.io.Serializable;

/**
 * @author Li-Xiaoxu
 * @version 1.0
 * @date 2020/4/2 11:56
 */
public class ReadAttaData implements Serializable {
    String RECEIPT_ID;
    String ATTACHMENT_ID;

    public String getRECEIPT_ID() {
        return RECEIPT_ID;
    }

    public void setRECEIPT_ID(String RECEIPT_ID) {
        this.RECEIPT_ID = RECEIPT_ID;
    }

    public String getATTACHMENT_ID() {
        return ATTACHMENT_ID;
    }

    public void setATTACHMENT_ID(String ATTACHMENT_ID) {
        this.ATTACHMENT_ID = ATTACHMENT_ID;
    }

    @Override
    public String toString() {
        return "ReadAttaData{" +
                "RECEIPT_ID='" + RECEIPT_ID + '\'' +
                ", ATTACHMENT_ID='" + ATTACHMENT_ID + '\'' +
                '}';
    }
}
