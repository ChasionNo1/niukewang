package com.chasion.niukewang.entity;

/**
 * @ClassName Page
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 16:48
 *
 * 封装分页相关的信息
 */
public class Page {
    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数（用于计算总页数）
    private int rows;
    // 查询路径（用来复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // 计算当前页的起始值
    public int getOffset(){
        // 当前页 * 每页个数 - 每页个数
        return (current - 1) * limit;
    }

    // 获取总页数
    public int getTotal(){
        if (rows % limit == 0){
            return rows / limit;
        }else {
            return rows / limit + 1;
        }
    }
    // 获取导航页码的起始位置
    public int getFrom(){
        int from = current - 2;
        return Math.max(from, 1);
    }

    // 获取结束页码
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return Math.min(to, total);
    }


}
