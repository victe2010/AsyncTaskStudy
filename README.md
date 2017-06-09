1、简介
====
## `android`网络通信`AsyncTask<Params,Progress,Result>`（异步任务）
#### 该类是个抽象类，这个类可以让你进行后台操作并发布在UI线程上的结果
，而无需操作线程和或处理程序。便于更新UI操作，
#### <Params,Progress,Result>参数的含义
* `Params` 执行任务之前的参数类型
* `Progress` 更新进度条的类型
* `Result` 执行任务后结果集的类型
> 如无需任何类型及参数，请使用`Void`

2、任务执行过程
======

### 执行异步任务经过四个步骤如下：

##### `onPreExecute` ：
在执行后台操作之前调用（一般作为进度条的显示）
##### `doInBackground`:
执行在后台（即子线程中）
##### `onProgressUpdate`：
在`doInBackgroung()`方法中调用publishProgress（）更新进度条（即运行在ui线程）
##### `onPostExecute`:
后台任务执行完成后回调，更新ui操作（进度条的隐藏）

3、取消任务
======
##### 任务可以在任何时候取消任务，调用cancel（boolean）方法调用此方法会导致后续调用isCancelled()返回true。调用此方法后，onCancelled(Object)而不是 onPostExecute(Object)将被调用后doInBackground(Object[]) 返回。为了确保任务是尽快取消，您应经常检查返回值isCancelled()从定期 doInBackground(Object[])，如果可能的话（例如一个循环中。）

4、线程规则
======
有迹象表明，必须遵循此类才能正常工作的几个线程规则：
* 该`AsyncTask`类必须在UI线程加载
* 任务实例必须在UI线程上创建
* `execute()`必须在UI线程调用
* 该任务只执行一次

5、代码实例
============

 (1)、继承`AsyncTask`抽象类
 ```
  private class MyAsy extends AsyncTask<String, Integer, Bitmap> {

         @Override
         protected void onPreExecute() {
             super.onPreExecute();
         }

         @Override
         protected Bitmap doInBackground(String... params) {
             Bitmap bitmap = null;
             try {
                 URL url = new URL(params[0]);
                 HttpURLConnection httpURLConnection =
                         (HttpURLConnection) url.openConnection();
                 httpURLConnection.setRequestMethod("GET");
                 if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                     InputStream is = httpURLConnection.getInputStream();
                     bitmap = BitmapFactory.decodeStream(is);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return bitmap;
         }

         @Override
         protected void onProgressUpdate(Integer... values) {
             super.onProgressUpdate(values);

         }

         @Override
         protected void onPostExecute(Bitmap bitmap) {
             super.onPostExecute(bitmap);
             if (bitmap != null)
              iv.setImageBitmap(bitmap);
         }
     }
 ```



 (2)、执行异步任务
  ```
  String urlpath = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3985607856,3414601056&fm=26&gp=0.jpg";
  new MyAsy().execute(urlpath);
  ```

6、源码分析
======
```
 private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));

 private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);
```
`AsyncTask`内部采用handler与线程池的结合使用，核心线程数为2-4个，
即最大并发数为4个，缓存线程池承载的线程数为128个，超额则抛出异常

7、缺点
=====
* 线程池中已经有128个线程，缓冲队列已满，如果此时向线程提交任务，将会抛出RejectedExecutionException。过多的线程会引起大量消耗系统资源和导致应用FC的风险。
* `AsyncTask`不会随着`Activity`的销毁而销毁，直到`doInBackground()`方法执行完毕。如果我们的Activity销毁之前，没有取消 AsyncTask，这有可能让我们的AsyncTask崩溃(crash)。因为它想要处理的view已经不存在了。所以，我们总是必须确保在销毁活动之前取消任务。如果在`doInBackgroud`里有一个不可中断的操作，比如`BitmapFactory.decodeStream()`，调用了`cancle()` 也未必能真正地取消任务。关于这个问题，在4.4后的AsyncTask中，都有判断是取消的方法isCancelled(),可能参考的这些作者都分析较早的版本，当然，这是笔者落后的原因。
* 如果`AsyncTask`被声明为`Activity`的非静态的内部类，那么`AsyncTask`会保留一个对创建了AsyncTask的Activity的引用。如果Activity已经被销毁，AsyncTask的后台线程还在执行，它将继续在内存里保留这个引用，导致Activity无法被回收，引起内存泄露
* 屏幕旋转或`Activity`在后台被系统杀掉等情况会导致`Activity`的重新创建，之前运行的AsyncTask会持有一个之前Activity的引用，这个引用已经无效，这时调用onPostExecute()再去更新界面将不再生效。