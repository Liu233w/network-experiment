package com.liu233w.network.webservice.server.dto;

import com.liu233w.network.webservice.server.Item;

public class QueryResult extends TodoListResult {
    /**
     * 结果
     */
    private Item[] results;

    public QueryResult() {
    }

    /**
     * 使用基类的结果来初始化当前类
     *
     * @param sup
     */
    public QueryResult(TodoListResult sup) {
        this.setAs(sup);
        results = null;
    }

    /**
     * 表示查询成功的构造函数，保存了结果
     *
     * @param results
     */
    public QueryResult(Item[] results) {
        this.setAs(TodoListResult.success());
        this.results = results;
    }

    public Item[] getResults() {
        return results;
    }

    public void setResults(Item[] results) {
        this.results = results;
    }
}
