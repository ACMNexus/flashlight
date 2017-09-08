# flashlight

&nbsp;&nbsp;&nbsp;&nbsp;由于最近需要做一个手电筒的应用也就有时间去更好的适配和研究该功能了，之前一直觉得该功能是非常简单
最开始的时候我就去网上找找别人的代码看看。后来发现什么鱼龙混杂的代码都有的，最关键的是功能适配机型不全的，这个一直让我非常的
苦恼，后来我发现Android系统的源代码中是有这个功能的，于是我就打开Source Insight导入了Android4.4源代码中的Package模块进
行查找，由于我导入的源代码是4.4的代码，但是我看的功能是在5.0上的，半天死活没有找到与flashlight相关的代码，后来我就使用
Everything进行电脑全局的搜索，偶然的一次关键字flashlight的时候使我发现了在android22/com/android/systemui/statusbar/policy
中发现了FlashlightCtroller.java，然后我在反编译竞品中过程发现了其代码跟系统的代码结构非常的相似，于是我把系统的代码修改，
然后增加了一些逻辑和对代码进行重构就变成了今天的这个样子。

&nbsp;&nbsp;&nbsp;&nbsp;这里我主要是说我在做的过程中主要问题就是打开手电筒死活没有竞品的速度这么快的；这个里面主要是分为两个点：
1、Android5.0以下打开速度比别人慢，2、Android5.0-6.0的手机比别人慢，6.0都是差不多的。下面我就把主要代码说下：

**Android5.0速度优化**

```
/**
 * 这个地方是一个非常关键的地方就是使用系统的api关闭手电筒，
 * 千万别去释放资源，如果你去释放资源的话，第二次打开的话速度不快的。
 */
if (mFlashlightRequest != null) {
    CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
    builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
    builder.addTarget(mSurface);
    CaptureRequest request = builder.build();
    mSession.capture(request, null, mHandler);
    mFlashlightRequest = request;
    mFlashlightEnabled = false;
}

private void teardown() {
    try {
        mCameraDevice = null;
        mSession = null;
        mFlashlightRequest = null;
        if (mSurface != null) {
            mSurface.release();
            mSurfaceTexture.release();
        }
        mSurface = null;
        mSurfaceTexture = null;
        mFlashlightEnabled = false;
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

&nbsp;&nbsp;&nbsp;&nbsp;我之前是在打开手电筒之后，关闭手电筒的话，则去释放资源了。其实我们可以直接调用系统的关闭手电筒的方法而不释放资源，
下次的话则打开速度是非常明显的，缺点就是会一直占用着Camera的这样子会导致别的应用打不开手电筒了

**Android5.0以下手机**

```
/**
* 快速开启手电筒
* TODO 如果我们调用了该方法的话，其打开手电筒的速度跟系统的打开手电筒的速度是一样的
* TODO 但是因为我们占用了系统相机的资源，所以必须要释放资源的，如果不释放资源的话，则其他的程序是不打不开的相机和手电筒的。
* @return
*/
private Camera quickStart() {
    if(mCamera == null) {
        mCamera = Camera.open();
    }

    if(mCamera == null) {
        return null;
    }
    if(mParameters == null) {
        mParameters = mCamera.getParameters();
    }

    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
    mCamera.setParameters(mParameters);
    return mCamera;
}

/**
* 关闭手电筒但是不释放资源
*/
private void closeNotRelease() {
    if(mParameters != null) {
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
    }
    if(mCamera != null) {
        mCamera.setParameters(mParameters);
    }
}
```

&nbsp;&nbsp;&nbsp;&nbsp;上面是我在Android5.0以下打开手电筒的优化方法，该代码最主要的是不会总是去创建Camera了，一直持有Camera，除非我们退出了程序
才去camera.release去释放资源的，这样子的当我们在界面上第二次打开手电筒的时候速度是非常的快的，这是优化了一下网上打开手电筒的方式的；我之前总是去
Camera.open()，之后又把这个camera去释放，这样子的话则会导致打开的速度稍微会慢点的，可以稍微感觉的出来，如果要做sos或者是不断的闪烁的时候，个人感觉还
是会感觉出来的。

&nbsp;&nbsp;&nbsp;&nbsp;上面是我在Android6.0以下手机上打开手电筒的优化方案，在Android6.0及以上的话暂时是不需要优化的，也不需要我们主动的去释放资
源了，系统已经帮我做了处理了，大家直接调用CameraManager.setTorchMode()就可以了。而且由于硬件不断的升级，打开手电筒的速度也是很快的，比以前老手机的速
度要快多了。不过上面两种的方式是会占用系统Camera的，如果不合理释放的话会导致其他的应用打不开camera的。所以我们需要在不同的场景进行不同打开方式的。

**博客地址**

CSDN地址：http://blog.csdn.net/u012417984/article/details/77542594</br>
体验地址：https://fir.im/h1kt?release_id=59afc379548b7a34400000a9

**效果图

![Example1](gif/flashlight_off.png)

![Example2](gif/flashlight_on.png)