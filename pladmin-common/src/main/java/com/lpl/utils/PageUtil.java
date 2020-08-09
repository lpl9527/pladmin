package com.lpl.utils;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lpl
 * 分页工具类
 */
public class PageUtil extends cn.hutool.core.util.PageUtil{

    /**
     * List分页
     * @param page  页码
     * @param size  每页条数
     * @param list  分页数据列表
     */
    public static List toPage(int page, int size , List list) {
        int fromIndex = page * size;
        int toIndex = page * size + size;
        if(fromIndex > list.size()){
            return new ArrayList();
        } else if(toIndex >= list.size()) {
            return list.subList(fromIndex,list.size());
        } else {
            return list.subList(fromIndex,toIndex);
        }
    }

    /**
     * Page 数据处理，将数据放入Map，预防redis反序列化报错
     */
    public static Map<String,Object> toPage(Page page) {
        Map<String,Object> map = new LinkedHashMap<>(2);
        map.put("content",page.getContent());
        map.put("totalElements",page.getTotalElements());
        return map;
    }

    /**
     * 自定义分页，将数据放入Map
     */
    public static Map<String,Object> toPage(Object object, Object totalElements) {
        Map<String,Object> map = new LinkedHashMap<>(2);
        map.put("content",object);
        map.put("totalElements",totalElements);
        return map;
    }
}
