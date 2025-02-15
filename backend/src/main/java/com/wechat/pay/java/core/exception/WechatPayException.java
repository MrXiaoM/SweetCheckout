package com.wechat.pay.java.core.exception;

import java.io.Serial;

/** 微信支付异常基类 */
public abstract class WechatPayException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -5896431877288268263L;

  public WechatPayException(String message) {
    super(message);
  }

  public WechatPayException(String message, Throwable cause) {
    super(message, cause);
  }
}
