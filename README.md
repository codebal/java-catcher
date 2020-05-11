# java-catcher

Catcher is Java Cache Manager (multi request, async refresh, ..)

<br/>
<br/> 

Catcher  
![Position of Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Position%20of%20Catcher.jpg)  

<br/>

Extended Cache Data  
![Extended Cache Data](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Extended%20Cache%20Data.JPG)

<br/>

Single Request on Sync  
![Single Request on Sync](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Single%20Request%20on%20Sync.JPG)
***
Single Request with **Catcher**  
![Single Request with Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Single%20Request%20with%20Catcher.JPG)

<br/>

Multi Request on Sync  
![Multi Request on Sync](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/Multi%20Request%20on%20Sync.JPG)
***
Multi Request with **Catcher**  
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

<br/>

##### RESULT  
 - ehcache only :  
      cache creating occurred : **800**  
      response delay occurred : every cache creating  
 - eacache **+ catcher**  
      cache creating occurred : **6**  
      response delay occurred : only first cache creating 

<br/>

Response time using Ehcache 
![Response time using Ehcache](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/ehcache-Response%20Time%20Graph.jpg)
***
Response time using Ehcache **+ Catcher** 
![Response time using Ehcache + Catcher](https://raw.githubusercontent.com/codebal/java-catcher/master/etc/img/catcher-Response%20Time%20Graph.jpg)      
