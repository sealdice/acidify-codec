# acidify-codec

[LagrangeCodec](https://github.com/LagrangeDev/LagrangeCodec) 的 Kotlin 绑定，支持 JVM 和 Native (Windows / macOS / Linux / Android arm64) 平台。

## 用法

此项目提供了一些对图像、音频和视频进行识别和编码的 API：

- `getImageInfo` - 获取图像的格式、尺寸信息
- `audioToPcm` - 将音频数据解码为 PCM 格式
- `silkEncode` 和 `silkDecode` - Silk 音频和 PCM 的相互转换
- `calculatePcmDuration` - 计算 PCM 音频的时长
- `getVideoInfo` - 获取视频的尺寸、时长信息
- `getVideoFirstFrameJpg` - 以 JPEG 格式获取视频的第一帧图像

这些功能在编写 QQ 协议端时非常有用。

## 项目实现

在 JVM 平台，该模块使用 JNA 调用 LagrangeCodec 的动态链接库；在 Native 平台，该模块通过 Kotlin C Interop 静态链接 LagrangeCodec 的静态库。JVM 平台的 jar 文件已经将动态库文件包含在内。

项目的 `src/jvmMain/resources` 目录下包含了各个平台的动态库文件；`src/nativeInterop/lib` 目录下包含了各个平台的静态库文件，其中包含 `androidArm64` 目录下的 Android arm64 静态库。静态库文件编译自 [Wesley-Young/LagrangeCodec](https://github.com/Wesley-Young/LagrangeCodec) 仓库，对原仓库的构建逻辑进行了一些调整以适应 Kotlin/Native 链接静态库的需求。

## 已知问题

在链接 `mingwX64` 目标的应用程序时，**必须**指定如下的 `linkerOpts`：

```
-Wl,-Bstatic -lstdc++ -lgcc -Wl,-Bdynamic
```

否则程序启动时会报错，提示缺失 `libstdc++-6.dll`。
