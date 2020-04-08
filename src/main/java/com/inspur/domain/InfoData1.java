package com.inspur.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Li-Xiaoxu
 * @version 1.0
 * @date 2020/4/2 11:39
 */
public class InfoData1 implements Serializable {
    String CITY_NAME;
    String COUNTY_NAME;
    String SITE_NAME;
    String SITE_NO;
    String METER_NUM;
    String MONTH_ID;
    Double READING_START;
    Double READING_END;
    Double MONTHLY_POWER;
    Date READING_START_TIME;
    Date READING_END_TIME;
    Double AVG_POWER;
    String CONTRACT_NO;
    Double ELEC_PRICE;
    Double ELEC_AMT;
    Double AVG_AMT;
    Double ATTACHMENT_ID;
    Double ATTACHMENT_ID_RECEIPT;
    Double ELEC_LOSE;
    Double ELEC_TOTAL;
    String RECEIPT_ID;
    Double SETTLE_POWER;
    String PROVINCE;
    String OWNER_CUST_CODE;
    int INT_ID;
    String CREATE_USER;
    Date CREATE_TIME;
    String UPDATE_USER;
    Date UPDATE_TME;

    public String getCITY_NAME() {
        return CITY_NAME;
    }

    public void setCITY_NAME(String CITY_NAME) {
        this.CITY_NAME = CITY_NAME;
    }

    public String getCOUNTY_NAME() {
        return COUNTY_NAME;
    }

    public void setCOUNTY_NAME(String COUNTY_NAME) {
        this.COUNTY_NAME = COUNTY_NAME;
    }

    public String getSITE_NAME() {
        return SITE_NAME;
    }

    public void setSITE_NAME(String SITE_NAME) {
        this.SITE_NAME = SITE_NAME;
    }

    public String getSITE_NO() {
        return SITE_NO;
    }

    public void setSITE_NO(String SITE_NO) {
        this.SITE_NO = SITE_NO;
    }

    public String getMETER_NUM() {
        return METER_NUM;
    }

    public void setMETER_NUM(String METER_NUM) {
        this.METER_NUM = METER_NUM;
    }

    public String getMONTH_ID() {
        return MONTH_ID;
    }

    public void setMONTH_ID(String MONTH_ID) {
        this.MONTH_ID = MONTH_ID;
    }

    public Double getREADING_START() {
        return READING_START;
    }

    public void setREADING_START(Double READING_START) {
        this.READING_START = READING_START;
    }

    public Double getREADING_END() {
        return READING_END;
    }

    public void setREADING_END(Double READING_END) {
        this.READING_END = READING_END;
    }

    public Double getMONTHLY_POWER() {
        return MONTHLY_POWER;
    }

    public void setMONTHLY_POWER(Double MONTHLY_POWER) {
        this.MONTHLY_POWER = MONTHLY_POWER;
    }

    public Date getREADING_START_TIME() {
        return READING_START_TIME;
    }

    public void setREADING_START_TIME(Date READING_START_TIME) {
        this.READING_START_TIME = READING_START_TIME;
    }

    public Date getREADING_END_TIME() {
        return READING_END_TIME;
    }

    public void setREADING_END_TIME(Date READING_END_TIME) {
        this.READING_END_TIME = READING_END_TIME;
    }

    public Double getAVG_POWER() {
        return AVG_POWER;
    }

    public void setAVG_POWER(Double AVG_POWER) {
        this.AVG_POWER = AVG_POWER;
    }

    public String getCONTRACT_NO() {
        return CONTRACT_NO;
    }

    public void setCONTRACT_NO(String CONTRACT_NO) {
        this.CONTRACT_NO = CONTRACT_NO;
    }

    public Double getELEC_PRICE() {
        return ELEC_PRICE;
    }

    public void setELEC_PRICE(Double ELEC_PRICE) {
        this.ELEC_PRICE = ELEC_PRICE;
    }

    public Double getELEC_AMT() {
        return ELEC_AMT;
    }

    public void setELEC_AMT(Double ELEC_AMT) {
        this.ELEC_AMT = ELEC_AMT;
    }

    public Double getAVG_AMT() {
        return AVG_AMT;
    }

    public void setAVG_AMT(Double AVG_AMT) {
        this.AVG_AMT = AVG_AMT;
    }

    public Double getATTACHMENT_ID() {
        return ATTACHMENT_ID;
    }

    public void setATTACHMENT_ID(Double ATTACHMENT_ID) {
        this.ATTACHMENT_ID = ATTACHMENT_ID;
    }

    public Double getATTACHMENT_ID_RECEIPT() {
        return ATTACHMENT_ID_RECEIPT;
    }

    public void setATTACHMENT_ID_RECEIPT(Double ATTACHMENT_ID_RECEIPT) {
        this.ATTACHMENT_ID_RECEIPT = ATTACHMENT_ID_RECEIPT;
    }

    public Double getELEC_LOSE() {
        return ELEC_LOSE;
    }

    public void setELEC_LOSE(Double ELEC_LOSE) {
        this.ELEC_LOSE = ELEC_LOSE;
    }

    public Double getELEC_TOTAL() {
        return ELEC_TOTAL;
    }

    public void setELEC_TOTAL(Double ELEC_TOTAL) {
        this.ELEC_TOTAL = ELEC_TOTAL;
    }

    public String getRECEIPT_ID() {
        return RECEIPT_ID;
    }

    public void setRECEIPT_ID(String RECEIPT_ID) {
        this.RECEIPT_ID = RECEIPT_ID;
    }

    public Double getSETTLE_POWER() {
        return SETTLE_POWER;
    }

    public void setSETTLE_POWER(Double SETTLE_POWER) {
        this.SETTLE_POWER = SETTLE_POWER;
    }

    public String getPROVINCE() {
        return PROVINCE;
    }

    public void setPROVINCE(String PROVINCE) {
        this.PROVINCE = PROVINCE;
    }

    public String getOWNER_CUST_CODE() {
        return OWNER_CUST_CODE;
    }

    public void setOWNER_CUST_CODE(String OWNER_CUST_CODE) {
        this.OWNER_CUST_CODE = OWNER_CUST_CODE;
    }

    public int getINT_ID() {
        return INT_ID;
    }

    public void setINT_ID(int INT_ID) {
        this.INT_ID = INT_ID;
    }

    public String getCREATE_USER() {
        return CREATE_USER;
    }

    public void setCREATE_USER(String CREATE_USER) {
        this.CREATE_USER = CREATE_USER;
    }

    public Date getCREATE_TIME() {
        return CREATE_TIME;
    }

    public void setCREATE_TIME(Date CREATE_TIME) {
        this.CREATE_TIME = CREATE_TIME;
    }

    public String getUPDATE_USER() {
        return UPDATE_USER;
    }

    public void setUPDATE_USER(String UPDATE_USER) {
        this.UPDATE_USER = UPDATE_USER;
    }

    public Date getUPDATE_TME() {
        return UPDATE_TME;
    }

    public void setUPDATE_TME(Date UPDATE_TME) {
        this.UPDATE_TME = UPDATE_TME;
    }

    @Override
    public String toString() {
        return "InfoData{" +
                "CITY_NAME='" + CITY_NAME + '\'' +
                ", COUNTY_NAME='" + COUNTY_NAME + '\'' +
                ", SITE_NAME='" + SITE_NAME + '\'' +
                ", SITE_NO='" + SITE_NO + '\'' +
                ", METER_NUM='" + METER_NUM + '\'' +
                ", MONTH_ID='" + MONTH_ID + '\'' +
                ", READING_START=" + READING_START +
                ", READING_END=" + READING_END +
                ", MONTHLY_POWER=" + MONTHLY_POWER +
                ", READING_START_TIME=" + READING_START_TIME +
                ", READING_END_TIME=" + READING_END_TIME +
                ", AVG_POWER=" + AVG_POWER +
                ", CONTRACT_NO='" + CONTRACT_NO + '\'' +
                ", ELEC_PRICE=" + ELEC_PRICE +
                ", ELEC_AMT=" + ELEC_AMT +
                ", AVG_AMT=" + AVG_AMT +
                ", ATTACHMENT_ID=" + ATTACHMENT_ID +
                ", ATTACHMENT_ID_RECEIPT=" + ATTACHMENT_ID_RECEIPT +
                ", ELEC_LOSE=" + ELEC_LOSE +
                ", ELEC_TOTAL=" + ELEC_TOTAL +
                ", RECEIPT_ID='" + RECEIPT_ID + '\'' +
                ", SETTLE_POWER=" + SETTLE_POWER +
                ", PROVINCE='" + PROVINCE + '\'' +
                ", OWNER_CUST_CODE='" + OWNER_CUST_CODE + '\'' +
                ", INT_ID=" + INT_ID +
                ", CREATE_USER='" + CREATE_USER + '\'' +
                ", CREATE_TIME=" + CREATE_TIME +
                ", UPDATE_USER='" + UPDATE_USER + '\'' +
                ", UPDATE_TME=" + UPDATE_TME +
                '}';
    }
}
