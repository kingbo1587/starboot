package com.wh.starboot.bean;

/**
 * Created by  on 2016/7/16.
 */
public enum TaskStatus {

    /*登入状态*/
    LOGIN_BEGIN("0000", "开始登入"),
    LOGIN_SUCCESS("0001", "登入成功"),
    LOGIN_ERROR("0002", "登入异常"),
    LOGIN_ACCOUNT_ERROR("0003", "账号格式错误"),
    LOGIN_USRPASS_ERROR("0004", "账号密码错误"),
    LOGIN_MISKEY_ERROR("0005", "请求参数错误"),
    LOGIN_LOCK_ERROR("0006", "账号锁定"),
    LOGIN_NEED_SEPRPWD("0007", "QQ邮箱需要独立密码登录"),
    LOGIN_SEPRPWD_ERROR("0008", "QQ邮箱独立密码登录失败"),
    LOGIN_NEED_CAPTCHA("0009", "需要验证码"),
    LOGIN_CAPTCHA_ERROR("0010", "验证码错误"),
    /*爬取邮件状态*/
    CRAWL_SUCCESS("1000", "爬取邮件成功"),
    CRAWL_NOMAIL("1001", "无匹配邮件"),
    CRAWL_ERROR("1002", "爬取邮件失败"),
    CRAWL_PROGRESS("1003", "爬取邮件中"),
    /*解析账单状态*/
    PARSE_SUCCESS("2000", "获取账单成功"),
    PARSE_NOBILL("2001", "无账单信息"),
    PARSE_ERROR("2002", "解析账单异常"),
    PARSE_PROGRESS("2003", "解析账单中"),
    /* 系统异常 */
    SYSTEM_TASK_REPEAT("9995", "该邮箱正在处理，勿重复请求"),
    SYSTEM_TASKID_ERROR("9996", "taskid不存在"),
    SYSTEM_MAILTYPE_NOTSUPPORT("9998", "不支持邮箱类型"),
    SYSTEM_ERROR("9999", "系统异常");

    private String key;

    private String value;

    TaskStatus(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String value() {
        return value;
    }

    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static String getValueByKey(String key) {
        for (TaskStatus reportStatus : TaskStatus.values()) {
            if (reportStatus.key.equals(key)) {
                return reportStatus.value;
            }
        }
        return null;
    }

    public static TaskStatus match(String key) {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (taskStatus.key.equals(key)) {
                return taskStatus;
            }
        }
        return LOGIN_ERROR;
    }
}
