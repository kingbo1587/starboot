package com.wh.starboot.bean.mail163;

import java.util.List;

/**
 * Created by  on 2016/7/22.
 */
public class Mail163ListResp {

    /**
     * 结果码
     */
    private String code;
    /**
     * 邮件信息
     */
    private List<Mail163Info> var;
    /**
     * 总记录数
     */
    private int total;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Mail163Info> getVar() {
        return var;
    }

    public void setVar(List<Mail163Info> var) {
        this.var = var;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Mail163ListResp{" +
                "code='" + code + '\'' +
                ", var(size)=" + (var == null ? null : var.size()) +
                ", total=" + total +
                '}';
    }
}
