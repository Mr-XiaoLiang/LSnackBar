### LSnackBar
-------------------
这是一个轻度定制的Snackbar。与系统Snackbar的区别有：
> 1. 支持在屏幕顶部显示。
> 2. 兼容处理沉浸状态栏问题。
> 3. 支持头部添加小图标。
> 4. 支持局部View内显示（需要FrameLayout）。
> 5. 兼容支持同样式的Toast。

效果预览：
[默认传统样式](https://github.com/Mr-XiaoLiang/LSnackBar/blob/master/gif/default.gif)
[局部显示样式](https://github.com/Mr-XiaoLiang/LSnackBar/blob/master/gif/local.gif)

使用方法：
``` java
//显示全局提示
LSnackBar.make(view,"这是测试数据",LSnackBar.LENGTH_SHORT).show();
//显示局部提示，会自动搜索最接近传入View的FrameLayout以显示
LSnackBar.makeLocal(view,"这是测试数据",LSnackBar.LENGTH_SHORT).show();
//显示Toast，可用参数基本与LSnackBar相同
LToast.makeText(getContext(),"这是测试数据",LToast.LENGTH_SHORT).show();
```