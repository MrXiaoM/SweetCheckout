# 支付宝订单码支付配置

本教程默认你已完成「订单码支付」相关资质的申请，以及网页应用成功通过审核。
> 2024年11月1日起，「当面付」产品的扫码支付功能已移到「订单码支付」产品。  
> 产品申请流程和需要提供的资料与「当面付」基本一致。

## 1.进入开放平台

https://open.alipay.com/develop/manage

![picture](images/alipay-step1.png)

## 2. 设置接口加签方式

![picture](images/alipay-step2.png)  
![picture](images/alipay-step3.png)  
![picture](images/alipay-step4.png)  

按照页面中的提示下载密钥工具

## 3. 生成密钥，并填入后端配置

![picture](images/alipay-step5.png)
*错字:* `相当 -> 相对`
![picture](images/alipay-step6.png)
![picture](images/alipay-step7.png)
![picture](images/alipay-step8.png)

重启后端或在后端执行 `reload` 命令生效。
