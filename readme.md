<div align="center">

# 哔哩终端

轻量的第三方B站Android客户端

[Gitee 仓库](https://gitee.com/RobinNotBad/BiliClient) | [Github 仓库 (huanli233 fork的仓库)](https://github.com/huanli233/BiliClient) | [Action Build (Github)](https://github.com/huanli233/action_build_biliclient)

</div>

# 介绍
这是一个**极其轻量级**的**B站客户端**，名字来源于原神中的“虚空终端”。使用 java + xml，支持至安卓4.2，借鉴了 [WearBili](https://github.com/SpaceXC/WearBili) 和 [腕上哔哩](https://github.com/luern0313/WristBilibili) 的部分开源代码和它们项目中收集的 API ，程序逻辑和数据处理还是自己写的，界面曾使用 [WearBili](https://github.com/SpaceXC/WearBili) 的布局（现已重做）（咱俩不是一个系列也不是同一个开发者！）。播放视频可以使用内置播放器或小电视播放器、凉腕播放器。

1. 我尽量不往里面塞太多东西，优先保证**可用性**、**流畅性**，字体啥的我再怎么说也不会放进去40M（
2. 我也尽量把代码写得好看了，该分类的地方都有分类，重要部分都有注释。 自学的安卓开发，代码~~可能~~有很多**明病/暗病**，我尽力了，轻喷QwQ
3. ~~api逻辑十分甚至九分清晰~~（指直接一层一层拆json）（有在自己尝试写json拆解函数，后续版本可能会逐渐替换原有方式）（最终并没有）
4. 依赖库少，~~可以快速嫁接到其他工程里~~（大嘘

> **请注意：此工程的某些部分存在复用以及有一些奇怪的写法以及可能存在暗病和屎山！**
>
> 很多结构相同的页面（如`稍后再看`页面、`收藏`页面等，都是只有一个 `RecyclerView` ）我都直接使用了共用的一套界面布局。动态和视频的 `Adapter` 和 `Holder` 我并没有按照常规套路来写，而是将 `Holder` 独立出来。因为有些页面如搜索页、个人信息页也用到了相同的代码，我就选择了把这些共用代码统一放在同一个类里。这可以减小一部分资源浪费，也易于整体修改。
>
> 布局里 `CardView` 和 `Button` 都做了**统一的 style** 。

### 为啥不是在那两位前辈的基础上改？

- [腕上哔哩](https://github.com/luern0313/WristBilibili) 的开源代码**不完整**，它的数据处理部分多处用到 luern 自己的 **Lson** 库，然而 Github 上的版本似乎不管用。
- [WearBili](https://github.com/SpaceXC/WearBili) 的界面确实好看，但是体积大、在许多手表上卡顿严重，而且仅支持安卓7.1以上，~~最重要的是我看不懂 kotlin~~。

### 其他

> 此项目正在更新中，若有问题和建议欢迎提出。
>
> 作者事学生，上学期间不能更新，请勿催更，因为催了也大概率没用（

> 友情链接：**WearBili** 现已推出**重制版**：[Re:WearBili](https://github.com/SpaceXC/Re-WearBili)，全新UI和动效，流畅度也有所改善，欢迎前往搜索与体验！

# 联系

- Robin 的 QQ ：`1707106142` ，有任何疑问欢迎和我对线，我脾气真的很好（
- 交流群：`482091687`

# 开发

`clone`本项目，导入到你的 `IDE` 进行开发、构建

> `develop`分支用于在线开发，可获取最新源码，但可能会存在未修复的问题。

