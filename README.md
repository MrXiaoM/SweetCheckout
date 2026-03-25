# SweetCheckout

自部署支付系统，无费率，免费，开源。

![Minecraft versions](https://img.shields.io/badge/minecraft-1.7.10--26.1-blue) ![Bukkit WS Java Compatible](https://img.shields.io/badge/bukkit--ws-Java_8-purple) ![Bukkit with Backend Java Compatible](https://img.shields.io/badge/bukkit--with--backend-Java_8-purple) ![Backend Java Compatible](https://img.shields.io/badge/backend--cli-Java_17-purple)

<details>
    <summary>免责声明</summary>
    <p>仅供学习研究与技术交流，请勿用于非法用途，后果自负。</p>
    <p>本项目作者与贡献者不对本项目的有效性、可靠性、安全性等作任何明示或暗示的保证，也不对使用或滥用本项目造成的任何直接或间接的损失、责任、索赔、要求或诉讼承担任何责任。</p>
    <p>本项目源代码或二进制文件的使用者应当遵守相关法律法规，尊重 Tencent 公司和阿里巴巴集团的版权与隐私，不得侵犯其与其它第三方的合法权益，不得从事任何违法或违反道德的行为。</p>
    <p>使用本程序的源代码或二进制文件的任何部分即代表你同意此条款，如有异议，请立即停止使用并删除所有相关文件。</p>
    <p>项目简介中的“无费率”指的是，本项目不额外收取手续费。例如当面付/订单码支付接口，支付宝官方收取<code>0.6%</code>手续费，那么费率就是<code>0.6%</code>，无额外的中间商抽成。</p>
</details>

## 简介

先说缺点，部分场景只能用金额来关联用户，无法承受高并发需求，这对于 Minecraft 服务器来说已基本足够，可以凑合着用。

```
SweetCheckout 目录结构
  ├─ backend: 支付系统后端
  ├─ packets: 网络包结构
  ├─ plugin: Bukkit插件
  └─ wechat-hook: 微信Hook软件
```

+ `支付系统后端`: 负责中转和管理插件请求与支付接口请求，储存支付排队序列
+ `网络包结构`: 后端与插件进行通讯的网络包结构 (json) 处理模块
+ `Bukkit插件`: 与玩家交互的逻辑
+ `微信Hook软件`: 接收微信收款消息，转发给后端处理

## 使用方法

将后端部署在服务器上，使用 `java -jar backend-x.x.x.jar` 的方式启动后端，编辑 `config.json`，输入各支付方案的参数，保存并重载后端。

在 [MCIO Plugins](https://plugins.mcio.dev/docs/checkout/install/backend) 文档中有一些支付方案的配置教程，自行查阅。

后端部署完成后，在 Minecraft 服务端上安装 Bukkit 插件，安装后在 `config.yml` 中配置后端地址，使得插件可以与后端通讯。使用 `/cz points` 命令来测试是否可以下单、收款即可。

## 支付方案

|   | 平台　　   | 方案　　                                                                                     | 说明                                                                      |
|---|:-------|:-----------------------------------------------------------------------------------------|:------------------------------------------------------------------------|
| ✅ | 支付宝    | [订单码支付](https://b.alipay.com/page/product-workspace/product-detail/I1080300001000068149) | 官方接口。订单码支付是指商家按支付宝的支付协议生成订单`二维码`，用户使用支付宝“扫一扫”即可完成付款。                    |
| ✅ | 支付宝    | Hook                                                                                     | 第三方接口。商家在后端配置各金额的支付`二维码`，付款时将二维码展示给用户，Hook截取开放平台的卖出交易查询信息，由后端进行确认付款的模式。 |
| ✅ | 微信     | [Native](https://pay.weixin.qq.com/static/product/product_intro.shtml?name=native)       | 官方接口。Native支付是指商户系统按微信支付协议生成支付`二维码`，用户再用微信“扫一扫”完成支付的模式。                 |
| ✅ | 微信     | Hook                                                                                     | 第三方接口。商家在后端配置各金额的支付`二维码`，付款时将二维码展示给用户，Hook截取微信PC版收款信息，由后端进行确认付款的模式。     |
| ❔ | PayPal | [PayPal REST API](https://developer.paypal.com/docs/api/orders/v2/)                      | 官方接口。通过第三方的API包装库 `payper`，向官方API发送请求，用户再通过返回的链接包装成的二维码完成支付的模式。         |

+ ✅ 代表 此方案可用，已测试通过。
+ ❔ 代表 此方案已实现，但由于开发者未申请相关接口等原因，未进行测试。
+ ❌ 代表 此方案暂不可用，暂无实现方法。

## 命令 (Bukkit)

根命令 `/sweetcheckout`，别名为 `/checkout` 或 `/cz`。  
`<>`包裹的为必选参数，`[]`包裹的为可选参数。  

| 命令                                     | 描述                                                       | 权限                           |
|----------------------------------------|----------------------------------------------------------|------------------------------|
| `/checkout points <类型> <金额>`           | 通过微信(wechat)或支付宝(alipay)下单指定金额的点券                        | `sweet.checkout.points`      |
| `/checkout buy <商品ID> <类型>`            | 通过微信(wechat)或支付宝(alipay)下单指定商品                           | 在商品配置定义                      |
| `/checkout rank`                       | 查看充值排行榜                                                  | `sweet.checkout.rank`        |
| `/checkout stats <起始时间> [结束时间]`        | 查看指定时间段内的交易统计信息                                          | `sweet.checkout.stats`       |
|                                        | (其时间格式可用 `月`, `年-月`, `年-月-日` 三种格式)                       |                              |
| `/checkout check`                      | 查看自己的充值记录                                                | `sweet.checkout.check`       |
| `/checkout check [玩家]`                 | 查看自己或某人的充值记录                                             | `sweet.checkout.check.other` |
| `/checkout qrcode <二维码内容>`             | 根据输入内容生成二维码，并通过地图展示。此命令用于测试地图以及二维码生成器的可用性                | OP                           |
| `/checkout map [文件名]`                  | 不输入文件名时，将手中的地图保存到`output.map`文件；输入文件名时，通过地图预览文件以测试文件是否正常 | OP                           |
| `/checkout log <玩家> <类型> <金额> <原因...>` | 手动添加充值记录。类型可以是任意字符串。                                     | OP/控制台                       |
| `/checkout reload database`            | 重新连接数据库                                                  | OP/控制台                       |
| `/checkout reload`                     | 重载配置文件                                                   | OP/控制台                       |

## 变量 (PAPI)

```
%sweetcheckout_rank_<第几名>_name% 充值排行榜第几名的玩家名
%sweetcheckout_rank_<第几名>_money% 充值排行榜第几名的金额
%sweetcheckout_points_money_<价格>% 获取原价点券数量
%sweetcheckout_money_shop_<商品ID>% 获取商品原价
%sweetcheckout_modified_money_<价格>% 获取修饰后的点券价格
%sweetcheckout_modified_points_<价格>% 获取修饰后的点券数量
%sweetcheckout_shop_modified_money_<商品ID>% 获取修饰后的商品价格
```

## 鸣谢

+ [alipay/alipay-sdk-java-all](https://github.com/alipay/alipay-sdk-java-all): 支付宝官方SDK(v2) —— Apache-2.0 License
+ [wechatpay-apiv3/wechatpay-java](https://github.com/wechatpay-apiv3/wechatpay-java): 微信支付官方SDK —— Apache-2.0 License
+ [eealba/payper](https://github.com/eealba/payper) PayPal 官方接口 第三方SDK (v2) —— Apache-2.0 License

## 开发者

对于想要添加支付方案支持者，请参见 `:backend:common` 模块的 [top.mrxiaom.sweet.checkout.backend.payment](https://github.com/MrXiaoM/SweetCheckout/tree/main/backend/common/src/main/java/top/mrxiaom/sweet/checkout/backend/payment) 包。支付订单下单逻辑、支付成功事件广播逻辑、取消支付逻辑等等，都在这里，请按自身需求进行增改。  
基本上，经过这一层接口的抽象，我们需要传递的信息大约只有：商品名（可选）、金额、订单号、支付二维码。

注意：由于本项目的目标是尽可能减少中间商，会造成**增加中间商**的拉取请求将被**拒绝**。

[![jitpack badge](https://jitpack.io/v/top.mrxiaom/SweetCheckout.svg)](https://jitpack.io/#top.mrxiaom/SweetCheckout)

```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    compileOnly("top.mrxiaom.SweetCheckout:shared:$VERSION")
    compileOnly("top.mrxiaom:qrcode-encoder:1.0.0") // 1.0.6 起需要添加
}
```

对接本插件的开发文档，请见 [MCIO Plugins](https://plugins.mcio.dev/docs/checkout/api/)。
