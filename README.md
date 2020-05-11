<style>
.post-container {
    img {
        margin: 10px 0;
    }
}

.container {
    max-width: 800px;
}

.post-img {
    display: block;
    margin: 10px 0px 20px;
    img {
        border: solid 1px #ccc;
        margin: 10px 0px 5px 0px;
    }
    .caption {
        display: block;
        text-align: center;
        font-size: 16px;
        line-height: 16px;
        margin: -10px 0px 0px;
    }
}
</style>

# java-catcher

Catcher is Java Cache Manager (multi request, async refresh, ..)

<br/>
<br/> 

<span class="post-img">![Position of Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Extended%20Cache%20Data.JPG)<span class="caption">Catcher</span></span>

  
<center>Catcher</center>

<br/>
<br/>

![Extended Cache Data](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Multi%20Request%20on%20Sync.JPG)

<br/>

![Single Request on Sync](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Single%20Request%20on%20Sync.JPG)
***
![Single Request with Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Single%20Request%20with%20Catcher.JPG)

<br/>
<br/>

![Multi Request on Sync](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Multi%20Request%20on%20Sync.JPG)
***
![Multi Request with Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Multi%20Request%20with%20Catcher.JPG)

<br/>
<br/>

#### **jmeter web response test**  
threads : 10000  
periods : 10 seconds  
elapsed time : 16-18 seconds  
ehcache cache setting :  
  - version = 2.10.6
  - timeToLiveSeconds = 3  
  - 1 sec delay (Thread.sleep)

catcher setting :  
 - refresh time = 3 seconds  
 - expire time = 5 seconds (also ehcache timeToLiveSeconds = 5)

<span style="color:#00f">RESULT</span>  
 - ehcache only :  
      cache creating occurred : **800**  
      response delay occurred : every cache creating  
 - eacache <span style="color:#f00">+ catcher</span>  
      cache creating occurred : **6**  
      response delay occurred : only first cache creating 
      
![Response time using Ehcache](etc/img/ehcache-Response Time Graph.jpg)
***
![Response time using Ehcache](etc/img/catcher-Response Time Graph.jpg)      
