<div align="center">

# 哔哩终端

轻量的第三方B站Android客户端

[官方 Gitee 仓库](https://gitee.com/RobinNotBad/BiliClient) | [Github 仓库 (由huanli233 fork)](https://github.com/huanli233/BiliClient) | [Action Build (Github)](https://github.com/huanli233/action_build_biliclient/actions)

[安装包下载](https://gitee.com/RobinNotBad/BiliClient/releases)

</div>

# 介绍
这是一个**极其轻量级**的**B站客户端**，名字来源于原神中的“虚空终端”。使用远古技术 `java` + `xml`开发，最低支持**安卓4.0.4**。（部分4.0.4设备无法运行，但也无法解决）~~这么古老的设备解码视频都费劲，要不还是留作收藏吧（~~

本项目借鉴了 [WearBili](https://github.com/SpaceXC/WearBili) 和 [腕上哔哩](https://github.com/luern0313/WristBilibili) 的部分开源代码和它们收集的部分 API 。
界面曾使用 [WearBili](https://github.com/SpaceXC/WearBili) 的布局（现已重做）。
**除此之外本项目与 其他第三方B站浏览软件 无任何关系**。
**谢绝在无关评论区提及终端，谢谢喵**。

播放视频可选择使用内置播放器、小电视播放器或凉腕播放器，内置播放器会优先支持部分功能。

1. 我们会尽量保证软件的轻量，不在其中塞入太多东西，优先保证**可用性**与**流畅性**，字体之类的怎么说也不会放进去40M的（
2. ~~我们尽量把代码写得好看了~~现在的代码已经是一个庞大的屎山了
3. ~~项目的API解析逻辑十分甚至九分清晰~~（指直接一层一层拆json）
4. 依赖库少，~~可以快速嫁接到其他工程里~~（大嘘

>**品鉴此项目代码前请注意：此工程的某些部分存在复用以及有一些奇怪的写法以及可能存在暗病和屎山！**
>
>#### 本项目可能包含：
>
> 大哥上楼梯：
> ```
> if (all.has("xxx")) {
>     JSONObject data = all.getJSONObject("xxx");
>     if (data.has("xxx")){
>         JSONObject data2 = data.getJSONObject("xxx");
>         if (data2.has("xxx")){
>             JSONObject data3 = data2.getJSONObject("xxx");
>             if (data3.has("items_lists")){
>```
>
> 神秘逻辑：
> ```
> if (data.getInt("aaa") == 1 ? true : false)
> if (data.getInt("bbb") == 1 ? true : false)
> if (data.getInt("ccc") == 1 ? true : false)
>```
>
> 以上问题正在逐渐改善，大概（QAQ
>
> 很多结构相同的页面（如`稍后再看`、`收藏`等只有一个 `RecyclerView`的页面）都直接使用了共用的一套界面布局。动态和视频的 `Adapter` 和 `Holder` 并没有按照常规套路来写，而是将 `Holder` 独立出来。因为有些页面如搜索页、个人信息页也用到了相同的代码，我就选择了把这些共用代码统一放在同一个类里。这可以减小一部分资源浪费，也易于整体修改。
>
> 布局里 `CardView` 和 `Button` 都设置了**统一的 style** 。

### 为啥不是在那两位前辈的基础上改？

- [腕上哔哩](https://github.com/luern0313/WristBilibili) 的开源代码**不完整**，它的数据处理部分多处用到 luern 自己的 **Lson** 库，然而 Github 上的版本似乎不管用。
- [WearBili](https://github.com/SpaceXC/WearBili) 的界面确实好看，但是体积大、在许多手表上卡顿严重，而且**仅支持安卓7.1**以上，~~最重要的是 Robin 看不懂 kotlin~~。

### 其他

> 此项目正在持续更新中，若有问题和建议欢迎提issue或加群反馈。
>
> 开发组都是学生，上学期间不能更新，请勿催更，因为催了也大概率没用（

> 友情链接：**WearBili** 现已推出**重制版**：[Re:WearBili](https://github.com/SpaceXC/Re-WearBili)，全新UI和动效，流畅度也有所改善，欢迎前往搜索与体验！

# 联系
 
- 唯一官网：[biliterminal.cn](https://biliterminal.cn)
- QQ交流群
> 交流一群：482091687
>
> 交流二群：656364457
>
> 测试群：745414928

# 开发

`clone`本项目，导入到你的IDE中进行开发、构建

> `develop`分支用于在线开发，获取到的为最新源码，但可能会存在未修复的问题。
> 欢迎提交 pr （

## 部分问题的解决方法

### ~~当前似乎没有什么大问题~~