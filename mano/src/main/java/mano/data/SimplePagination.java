package mano.data;

import java.util.ArrayList;

/**
 * 简单的分页实现
 * @author junhwong
 * @param <T>
 */
public class SimplePagination <T> implements Pageable<T> {

    private int limit=20;
    private int total=0;
    private int pageNumber=1;
    private Iterable<T> data = new ArrayList<>();

    static int[] range(int total, int current, int size) {

        size = size <= 3 ? 3 : (size % 2 == 0 ? size + 1 : size);
        
        int[] result = new int[5];
        if (total <= 0) {
            result[0] = 1;
            result[1] = 0;
        } else if (total <= size) {
            result[0] = 1;
            result[1] = total;
        } else if (current >= total) {
            result[0] = Math.max(total + 1 - size, 1);
            result[1] = total;
        } else if (current - (size / 2) <= 0) {
            result[0] = 1;
            result[1] = Math.min(size, total);
        } else {
            result[1] = Math.min(current + (size / 2), total);
            result[0] = Math.max(current + 1 - (size - (result[1] - current)), 1);
        }
        //int tmp=result[0]-(size / 2);
        result[2]=result[0]>1?result[0]-1: -1;
        
        //tmp=result[1]+(size / 2);
        result[3]=result[1]<total?result[1]+1: -1;
        
        result[4]=total>size?1:0;
        return result;
    }
    
    public static void main(String [] args){
        int[] r=range(90,5,9);
        System.out.println(r[0]);
        System.out.println(r[1]);
        System.out.println(r[2]);
        System.out.println(r[3]);
        System.out.println(r[4]);
    }

    /**
     * @return the skip
     */
    @Override
    public int getOffset() {
        return getLimit() * (this.getPageNumber() - 1);
    }

    /**
     * @return the limit
     */
    @Override
    public int getLimit() {
        return limit<=0?1:limit;
    }

    /**
     * @return the total
     */
    @Override
    public int getTotal() {
        return total<=0?0:total;
    }

    @Override
    public int getPageNumber() {
        return pageNumber<=0?1:pageNumber;
    }

    @Override
    public final int getPageCount() {
        return getTotal()/getLimit() + (getTotal() % getLimit() != 0 ? 1 : 0);
    }

    @Override
    public int prev() {
        return getPageNumber()<=0?-1:getPageNumber()-1;
    }

    @Override
    public int next() {
        return getPageNumber()>=getPageCount()?-1:getPageNumber()+1;
    }

    @Override
    public int first() {
        return 1;
    }

    @Override
    public int last() {
        return getPageCount();
    }

    @Override
    public Pageable<T> setResults(int total, Iterable<T> result) {
        this.setTotal(total);
        this.setData(result);
        return this;
    }

    @Override
    public Iterable<T> getResult() {
        return getData();
    }

    @Override
    public int[] compute(int size) {
        return range(this.getPageCount(), this.getPageNumber(), size);
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * @return the data
     */
    public Iterable<T> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Iterable<T> data) {
        if(data==null){
            throw new java.lang.NullPointerException("data");
        }
        this.data = data;
    }
    
}