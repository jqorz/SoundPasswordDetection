# 芝麻开门 

--by jqorz

## 功能

- 声纹识别
- 语音识别
- 语音合成



## 使用API

- 讯飞语音API

## 使用框架

- [EventBus (事件总线框架)](https://www.baidu.com/s?tn=mswin_oem_dg&ie=utf-16&word=eventbus)
- [ButterKnife (注解框架)](http://jakewharton.github.io/butterknife/)
- [AndPermission (动态权限申请框架)](https://github.com/yanzhenjie/AndPermission/blob/master/README-CN.md)
- [SlideLayout (支持上滑打开的抽屉)](https://github.com/rey5137/SlideLayout)
- [VoiceLine (声音波纹动画)](https://github.com/ws123/VoiceLine)

## 介绍

### 未注册

点击按钮，说5次“芝麻开门”进行注册

### 已注册

点击按钮，说“芝麻开门”进行验证，验证通过后，通过语音识别下达命令
其中，置信度小于60会提示验证失败。
### 内置的指令包括
1. “打开手电筒”“打开闪光灯”
2. “关闭手电筒”“关闭闪光灯”
3. “打开QQ”
4. “打开微信”
5. “打电话给XXX(通讯录联系人的姓名)”
