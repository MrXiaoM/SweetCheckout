package com.alipay.api.internal.util;

import com.alipay.api.AlipayConstants;
import com.alipay.api.AlipayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 客户端日志 通讯错误格式：time^_^api^_^app^_^ip^_^os^_^sdk^_^url^responseCode 业务错误格式：time^_^response
 */
public class AlipayLogger {

    private static final Logger clog = LoggerFactory.getLogger("sdk.comm.err");
    private static final Logger blog = LoggerFactory.getLogger("sdk.biz.err");
    private static final Logger ilog = LoggerFactory.getLogger("sdk.biz.info");

    private static String osName = System.getProperties().getProperty("os.name");
    private static String ip = null;
    private static boolean needEnableLogger = false;

    public static void setNeedEnableLogger(boolean needEnableLogger) {
        AlipayLogger.needEnableLogger = needEnableLogger;
    }

    public static String getIp() {
        if (ip == null) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                clog.warn("getIp", e);
            }
        }
        return ip;
    }

    public static void setIp(String ip) {
        AlipayLogger.ip = ip;
    }

    /**
     * 通讯错误日志
     */
    public static void logCommError(Exception e, HttpURLConnection conn, String appKey,
                                    String method, byte[] content) {
        if (!needEnableLogger) {
            return;
        }
        String contentString;
        try {
            contentString = new String(content, StandardCharsets.UTF_8);
            logCommError(e, conn, appKey, method, contentString);
        } catch (Exception e1) {
            clog.warn("logCommError", e1);
        }
    }

    /**
     * 通讯错误日志
     */
    public static void logCommError(Exception e, String url, String appKey, String method,
                                    byte[] content) {
        if (!needEnableLogger) {
            return;
        }
        String contentString;
        contentString = new String(content, StandardCharsets.UTF_8);
        logCommError(e, url, appKey, method, contentString);
    }

    /**
     * 通讯错误日志
     */
    public static void logCommError(Exception e, HttpURLConnection conn, String appKey,
                                    String method, Map<String, String> params) {
        if (!needEnableLogger) {
            return;
        }
        _logCommError(e, conn, null, appKey, method, params);
    }

    public static void logCommError(Exception e, String url, String appKey, String method,
                                    Map<String, String> params) {
        if (!needEnableLogger) {
            return;
        }
        _logCommError(e, null, url, appKey, method, params);
    }

    /**
     * 通讯错误日志
     */
    private static void logCommError(Exception e, HttpURLConnection conn, String appKey,
                                     String method, String content) {
        Map<String, String> params = parseParam(content);
        _logCommError(e, conn, null, appKey, method, params);
    }

    /**
     * 通讯错误日志
     */
    private static void logCommError(Exception e, String url, String appKey, String method,
                                     String content) {
        Map<String, String> params = parseParam(content);
        _logCommError(e, null, url, appKey, method, params);
    }

    /**
     * 通讯错误日志
     */
    private static void _logCommError(Exception e, HttpURLConnection conn, String url,
                                      String appKey, String method, Map<String, String> params) {
        DateFormat df = new SimpleDateFormat(AlipayConstants.DATE_TIME_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone(AlipayConstants.DATE_TIMEZONE));
        String sdkName = AlipayConstants.SDK_VERSION;
        String urlStr;
        //rspCode不再获取状态码，原因：https://baiyan.alipay.com/task/173959?bqlKey=8837cee
        String rspCode = "";
        if (conn != null) {
            urlStr = conn.getURL().toString();
        } else {
            urlStr = url;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date()));// 时间
        sb.append("^_^");
        sb.append(method);// API
        sb.append("^_^");
        sb.append(appKey);// APP
        sb.append("^_^");
        sb.append(getIp());// IP地址
        sb.append("^_^");
        sb.append(osName);// 操作系统
        sb.append("^_^");
        sb.append(sdkName);// SDK名字,这是例子，请换成其他名字
        sb.append("^_^");
        sb.append(urlStr);// 请求URL
        sb.append("^_^");
        sb.append(rspCode);
        sb.append("^_^");
        sb.append((e.getMessage()).replaceAll("\r\n", " "));
        clog.error(sb.toString());
    }

    private static Map<String, String> parseParam(String contentString) {
        Map<String, String> params = new HashMap<>();
        if (contentString == null || contentString.trim().isEmpty()) {
            return params;
        }
        String[] paramsArray = contentString.split("&");
        for (String param : paramsArray) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    /**
     * 业务/系统错误日志
     */
    public static void logBizDebug(String rsp) {
        if (!needEnableLogger) {
            return;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(AlipayConstants.DATE_TIMEZONE));
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date()));
        sb.append("^_^");
        sb.append(rsp);

        if (blog.isDebugEnabled()) {
            blog.debug(sb.toString());
        }
    }

    /**
     * 业务/系统错误日志
     */
    public static void logBizError(String rsp) {
        if (!needEnableLogger) {
            return;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(AlipayConstants.DATE_TIMEZONE));
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date()));
        sb.append("^_^");
        sb.append(rsp);
        blog.error(sb.toString());
    }

    /**
     * 业务/系统错误日志
     */
    public static void logBizError(String rsp, Map<String, Long> costTimeMap, Map<String, Object> rt) {
        if (!needEnableLogger) {
            return;
        }
        rt = rt == null ? new HashMap<>() : rt;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(AlipayConstants.DATE_TIMEZONE));
        StringBuilder sb = new StringBuilder();
        sb.append(df.format(new Date()));
        sb.append("^_^");
        sb.append(rsp);
        sb.append("^_^");
        sb.append(costTimeMap.get("prepareCostTime"));
        sb.append("ms,");
        sb.append(costTimeMap.get("requestCostTime"));
        sb.append("ms,");
        sb.append(costTimeMap.get("postCostTime"));
        sb.append("ms");
        if (rt.containsKey("trace_id")) {
            sb.append("^_^");
            sb.append("trace_id:");
            sb.append(rt.get("trace_id"));
        }
        blog.error(sb.toString());
    }

    /**
     * 业务/系统错误日志
     */
    public static void logBizError(Throwable t) {
        if (!needEnableLogger) {
            return;
        }
        blog.error("业务/系统错误", t);
    }

    /**
     * 发生特别错误时记录完整错误现场
     */
    public static void logErrorScene(Map<String, Object> rt, AlipayResponse tRsp,
                                     String appSecret) {
        if (!needEnableLogger) {
            return;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(AlipayConstants.DATE_TIMEZONE));
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorScene");
        sb.append("^_^");
        sb.append(tRsp.getCode());
        sb.append("^_^");
        sb.append(tRsp.getSubCode());
        sb.append("^_^");
        sb.append(ip);
        sb.append("^_^");
        sb.append(osName);
        sb.append("^_^");
        sb.append(df.format(new Date()));
        sb.append("^_^");
        sb.append("ProtocalMustParams:");
        appendLog((AlipayHashMap) rt.get("protocalMustParams"), sb);
        sb.append("^_^");
        sb.append("ProtocalOptParams:");
        appendLog((AlipayHashMap) rt.get("protocalOptParams"), sb);
        sb.append("^_^");
        sb.append("ApplicationParams:");
        appendLog((AlipayHashMap) rt.get("textParams"), sb);
        sb.append("^_^");
        sb.append("Body:");
        sb.append((String) rt.get("rsp"));
        blog.error(sb.toString());
    }

    /**
     * 发生特别错误时记录完整错误现场
     */
    public static void logErrorScene(Map<String, Object> rt, AlipayResponse tRsp,
                                     String appSecret, Map<String, Long> costTimeMap) {
        if (!needEnableLogger) {
            return;
        }
        rt = rt == null ? new HashMap<>() : rt;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(AlipayConstants.DATE_TIMEZONE));
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorScene");
        sb.append("^_^");
        sb.append(tRsp.getCode());
        sb.append("^_^");
        sb.append(tRsp.getSubCode());
        sb.append("^_^");
        sb.append(ip);
        sb.append("^_^");
        sb.append(osName);
        sb.append("^_^");
        sb.append(df.format(new Date()));
        sb.append("^_^");
        sb.append("ProtocalMustParams:");
        appendLog((AlipayHashMap) rt.get("protocalMustParams"), sb);
        sb.append("^_^");
        sb.append("ProtocalOptParams:");
        appendLog((AlipayHashMap) rt.get("protocalOptParams"), sb);
        sb.append("^_^");
        sb.append("ApplicationParams:");
        appendLog((AlipayHashMap) rt.get("textParams"), sb);
        sb.append("^_^");
        sb.append("Body:");
        sb.append((String) rt.get("rsp"));
        sb.append("^_^");
        sb.append(costTimeMap.get("prepareCostTime"));
        sb.append("ms,");
        sb.append(costTimeMap.get("requestCostTime"));
        sb.append("ms,");
        sb.append(costTimeMap.get("postCostTime"));
        sb.append("ms");
        if (rt.containsKey("trace_id")) {
            sb.append("^_^");
            sb.append("trace_id:");
            sb.append(rt.get("trace_id"));
        }
        blog.error(sb.toString());
    }

    /**
     * 发生特别错误时记录完整错误现场
     */
    public static void logBizSummary(Map<String, Object> rt, AlipayResponse tRsp,
                                     Map<String, Long> costTimeMap) {
        if (!needEnableLogger) {
            return;
        }
        rt = rt == null ? new HashMap<>() : rt;
        StringBuilder sb = new StringBuilder();
        sb.append("Summary");
        sb.append("^_^");
        sb.append(tRsp.getCode());
        sb.append("^_^");
        sb.append(tRsp.getSubCode());
        sb.append("^_^");
        sb.append("ProtocalMustParams:");
        appendLog((AlipayHashMap) rt.get("protocalMustParams"), sb);
        sb.append("^_^");
        sb.append("ProtocalOptParams:");
        appendLog((AlipayHashMap) rt.get("protocalOptParams"), sb);
        sb.append("^_^");
        sb.append("ApplicationParams:");
        appendLog((AlipayHashMap) rt.get("textParams"), sb);
        sb.append("^_^");
        sb.append(costTimeMap.get("prepareCostTime"));
        sb.append("ms,");
        sb.append(costTimeMap.get("requestCostTime"));
        sb.append("ms,");
        sb.append(costTimeMap.get("postCostTime"));
        sb.append("ms");
        if (rt.containsKey("trace_id")) {
            sb.append("^_^");
            sb.append("trace_id:");
            sb.append(rt.get("trace_id"));
        }
        ilog.info(sb.toString());
    }

    private static void appendLog(AlipayHashMap map, StringBuilder sb) {
        boolean first = true;
        Set<Map.Entry<String, String>> set = map.entrySet();
        for (Map.Entry<String, String> entry : set) {
            if (!first) {
                sb.append("&");
            } else {
                first = false;
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
    }

    public static Boolean isBizDebugEnabled() {
        return blog.isDebugEnabled();
    }
}
