/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data;


/**
 * 表示数据分页信息。
 * @author junhwong
 */
public interface Pageable {
    /**
     * @return 返回用于获取数据的起始偏移，从 0 开始。
     */
    int getOffset();
    
    /**
     * @return 返回每次获取数据的条数。
     */
    int getLimit();
    
    /**
     * @return 返回本次查询的数据总条数。
     */
    int getTotal();
    
    /**
     * 返回当前页码。
     * @return 
     */
    int getPageNumber();
    
    /**
     * 获取总页数。
     * @return 
     */
    int getPageCount();
    
    /**
     * 返回上页的页码，-1 表示无。
     * @return 
     */
    int prev();
    
    /**
     * 返回下页的页码，-1 表示无。
     * @return 
     */
    int next();
    
    /**
     * 获取首页页码。
     * @return 
     */
    int first();
    
    /**
     * 获取最后页码。
     * @return 
     */
    int last();
    
    /**
     * 设置查询结果。
     * @param total 总数据条数。
     * @param result 当前数据结果。
     * @return 
     */
    Pageable setResults(int total,Iterable result);
    
    /**
     * 获取查询的数据结果。
     * @return 
     */
    Iterable getResult();
    
    /**
     * 计算数字批量分页。
     * @param size 显示个数，必须是 3 的倍数。
     * @return 结果数组：
     * <br>索引 0 ：当前开始页码；
     * <br>索引 1 ：当前结始页码；
     * <br>索引 2 ：上一批次页码，无则返回 -1；
     * <br>索引 3 ：下一批次页码，无则返回 -1。
     */
    int[] compute(int size);
}
