# Crawler 一个简易的爬虫框架#
---
现已加入maven中央仓库
使用  
现已加入maven中央仓库（search.maven.org）
添加maven依赖：  

    `<dependency>
        groupId>com.arloor</groupId>
        <artifactId>EasyCrawler</artifactId>
        <version>1.0.1</version>
      </dependency>`

## 功能 ##
 :)这个readme针对第一版本 。               
 项目目前封装了httpclient和jsoup。            
 支持https，针对一般的反爬虫做了一些处理。                               
 专门为模拟登陆做过优化，在项目中有两个demo，分别是模拟登陆moontell.cn(自己的网站)和github.com。             
 只需要将根目录下的jar/EasyCrawler.jar放进项目的lib即可使用
 
## 关键类说明： ##

 Graber
 ------
 这个类封装了HttpCLient，负责发送请求,接收响应。首先说一下Graber的各个属性。       

 RequestEntity：请求的实体。对要请求的网页的一个封装，包含请求的URI，请求的Method，如果是POST,则还包含postData（POST传递的数据）。这个类下面有详细介绍                                                                                                                                                                                                                                                                                                                                                                         

 requestEntityList：请求实体的列表。一个requestEntityList对应一个Grab任务。一次爬取任务首先要调用`Graber.add(RequestEntity requestEntity)`增加实体。一次爬取人物的执行Graber.grab()将会链式地对requestEntityList的RequestEntity进行请求，并且返回最后一个请求的响应的HttpEntity。这个HttpEntity可以以后交给Parser进行解析。如果需要多次使用HttpEntity，则需要使用`entity=new BufferedHttpEntity(entity);`来缓存（Entity其实是一个流）。爬取结束之后可以选择执行`graber.clearRequestList();`来清空爬取人物列表，以便开始下一次爬取任务。                          

 再说一下使用到的各个方法：              
 创建方法：使用一个MyHttpClient类的示例。这个MyHttpClient在下面将会进行介绍。PS：用户可以选择创建MyHttpClient的子类达到定制的目的         
 addCookie：增加cookie。在MyHttpClient中执行请求时，每爬取一个网页，都将自动的增加网站返回的cookie。对于使用者来说最最重要的是可以手动调用`Graber.addCoookie(String key,String value)`来增加在Graber范围内有效的cookie，直到调用`Graber.clearCookie()`方法。          

 add(RequestEntity):增加Grab任务中要请求的实体。实体一旦增加，将会在Graber范围内有效，直到调用`Graber.clearRequestList()`。                 

 grab():开始一次Grab任务。       
 stop():所有Grab任务完成后调用，作用是关闭client和response。重要：在所有任务的最后必须要调用此方法。             
 getClient():获得MyHttpClient，进而可以直接使用MyHttpClient的方法。一般不需要是用，因为Graber类已经封装了大部分。      

RequestEntity
-------------
RequestEntity需要说明的一些方法：        
创建：有两个方法。方法一只有一个URI参数，默认方法为GET;方法二有两个参数`(URI uri, Map<String, String> postData)`，默认方法为POST        
setMethod:设置请求的方式          
addPostData：增加POST传递的键值对，这是比较有用的一个方法，嗯。                    

MyHttpClient
-------------
创建方法：可使用的参数：waitTime，retryTime：分别是为减慢爬取速度的等待时间，爬取失败的重试次数。创建方法中还实现了一种简单粗暴的https支持，这个不好意思说，不说。          
cookieMap：储存cookie键值对的map                
response：CloseableHttpResponse的实例，将在调用Graber.stop()时关闭。                        
其他方法：是进行请求的实现。说明：调用get或者post会发起get或者Post请求。过程中会增加cookie。返回一个HttpEntity（response的封装）。如果不需要定制MyHttpClient，不需要深入研究。             

Parser
-----
是一个抽象类，用于解析页面，获取网页内容。其中的getCommit()方法用于获取网页中的第一个form中input标签的节点，即表单需要填写的内容。            
在使用中需要创建这个类的子类，并且添加自定义的方法进行解析。            
看代码之后你就会发现他很简陋（其实是Jsoup已经很强大了，都不需要怎么封装）。            
 
 
 
## demo：
以GithubDemo为例        

唉，不想写了                   

  
 
 


  [1]: https://github.com/arloor/Crawler/tree/bc9a00ad55cb9643dbb137892193ae400f01beaf
