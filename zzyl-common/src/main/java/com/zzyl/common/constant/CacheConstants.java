package com.zzyl.common.constant;

/**
 * 缓存的key 常量
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
    /**
     *缓存护理等级
     */
    public static final String NURSING_LEVEL_ALL_KEY ="nursing_level:all" ;
    /**
     *缓存护理计划
     */
    public static final String NURSING_PLAN_ALL_KEY = "nursing_plan:all";
    /**
     *缓存所有护理项目
     */
    public static final String NURSING_PROJECT_ALL_KEY ="nursing_project:all" ;
    /**
     *体检报告缓存
     */
    public static final String HEALTH_REPORT = "healthReport";
}
