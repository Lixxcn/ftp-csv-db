package com.inspur.dao;

import com.inspur.domain.InfoData;

import java.util.List;

/**
 * @author Li-Xiaoxu
 * @version 1.0
 * @date 2020/4/3 15:38
 */
public interface IInfoDataDao {
    //存在则更新，不存在则插入
    void mergeInto(InfoData infoData);
    //查询所有
    List<InfoData> findAll();
    //删除存在的
    void deleteByRECEIPT_ID(String RECEIPT_ID);
    //删除存在的
    void deleteByINT_ID(Integer int_id);
}
