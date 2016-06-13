# puppy-io
puppy-io is a java framework.It provides easy way for developing reactive microservice systems on the jvm.

puppy-io contain single thread web server and you can perform  non-blocking I/O operations withing the server. You can deploy micro services easily with  puppy-io and you can communicate with each services easily. puppy-io have inbuilt Clustering and Load-Balancing mechanism , It make sure the fault tolerance and increase the scalability of your applications.

![puppy-io](https://github.com/loviworld/puppy-io/blob/master/puppy-io.png)

How threading works inside puppy-io
![puppy-io how threading works](https://github.com/loviworld/puppy-io/blob/master/puppy-io-t.png)

puppy-io non blocking web server
![puppy-io non blocking web server](https://github.com/loviworld/puppy-io/blob/master/puppy-io-nb.png)

### Prerequisities
  * JDK 1.8.X
  * Maven 3.3.X

### Getting Started
 * Download the [puppy-io](https://github.com/loviworld/puppy-io) from GitHub
 * Install puppy-io dependency using **mvn install**
 
```xml
	<dependency>
		<groupId>com.lovi.puppy</groupId>
		<artifactId>puppy-io</artifactId>
		<version>0.0.1-SNAPSHOT</version>
	</dependency>
```
 
### Sample application
 * Download the [user-manager](https://github.com/loviworld/puppy-io-samples) application from GitHub
 * Build the application using **mvn package**
 * Run the application using **java -jar target\user-manager-0.0.1-SNAPSHOT.jar**
 * Consume web app from **localhost:9000**

##Starting the application
```java
@SpringBootApplication
public class App 
{
    public static void main( String[] args )
    {
        PuppyApp.create(App.class, "user-mgr", args).run(9000);
    }
}
```
* ```PuppyApp.run()``` default port is 8080
* ```PuppyApp.run(int webAppPort)```
* ```PuppyApp.runWebApp()``` run only the web application
* ```PuppyApp.runServiceApp()``` run only the service application
* Sample application is based on spring-boot.you also can use puppy-io without spring-boot

##Web App
```java
@Controller
@RequestMapping("/users")
public class UserController {

	@Autowired
	private ServiceCaller serviceCaller;
	
	@ResponseBody
	@RequestMapping(produce="application/json")
	public void findAll(HttpResponseResult responseResult) throws ServiceCallerException{
		
		Result<List<User>> result = Result.create();
		FailResult failResult = FailResult.create();
		
		serviceCaller.call("UserService.findAll", result);
		
		result.process(r->{
			responseResult.complete(new ResponseMessage(1, r));
		}, failResult);
		
		failResult.setHandler(fail->{
			responseResult.complete(new ResponseMessage(-1, fail.getMessage()),500);
		});
	}
	
	@ResponseBody
	@RequestMapping(method=HttpMethod.POST, produce="application/json")
	public void insert(@ModelAttribute User user, HttpResponseResult responseResult) throws ServiceCallerException{
		
		serviceCaller.call("UserService.insert", user);
		responseResult.complete(new ResponseMessage(1, "do insert"),200);
	
	}
	....
}
```
####@Controller
* Use ```com.lovi.puppy.annotation.Controller```
* Implementation of the controllers are similar to the spring-mvc but remember internal architecture of the puppy-io is totally different from spring-mvc

####@RequestMapping
* value = The primary mapping expressed by this annotation
* method = The HTTP request methods
* consumes = The consumable media types of the mapped request
* produce = The producible media types of the mapped request

####HttpResponseResult
* ```HttpResponseResult.complete(Object value)``` set response value
* ```HttpResponseResult.complete(Object value, int statusCode)``` set response value with statusCode
* If you put ```@ResponseBody``` annonation with the method, then return the value of object as response. otherwise response is redirect to  a template or another route.
* ```HttpResponseResult.complete("{template}")```
* ```HttpResponseResult.complete("/{route}")```
* puppy-io use Thymeleaf template engine for genarating templates

####ServiceCaller
* ServiceCaller is used to call service method
* ```ServiceCaller.call(String serviceMethod, Object... inputParameters)```
* ```ServiceCaller.call(String serviceMethod, Result<U> result, Object... inputParameters)```. if your service method has a return value, get the return value by using ```Result```
* ```ServiceCaller.call(String appName, String serviceMethod, Result<U> result, Object... inputParameters)```. if you want to call serivce method from a another application, you can call with the appName

####Result
* ```Result<T>``` is used to catch the return value of the service method which is called by ```ServiceCaller```

####FailResult 
* ```FailResult``` is used to catch the failure within the service method call which is called by ```ServiceCaller```

####ViewAttribute
```java
@Controller
public class IndexController {

	@Autowired
	private ServiceCaller serviceCaller;
	
	@RequestMapping
	public void loadIndexView(Session session, ViewAttribute viewAttribute, HttpResponseResult responseResult){
		User loggedUser = session.get("user", User.class);
		if(loggedUser != null){
			viewAttribute.put("loggedUser", loggedUser);
			responseResult.complete("users-dashboard");
		}else
			responseResult.complete("index");
	}
	....
}
```
* Use ```com.lovi.puppy.web.ViewAttribute```
* ```viewAttribute.put("loggedUser", loggedUser);```
* ```viewAttribute.get("loggedUser", User.class);```
* In the template ```${loggedUser.userId}```
* ViewAttribute is used to maintain any data that you want to share between handlers or share between views

####Session
```java
@Controller
public class IndexController {

	@RequestMapping(method=HttpMethod.POST)
	public void signIn(@RequestParm("userId") String userId, @RequestParm("password") String password,
					Session session,
						HttpResponseResult responseResult) throws ServiceCallerException{
		
		Result<User> result = Result.create();
		FailResult failResult = FailResult.create();
		
		serviceCaller.call("UserService.findByUserIdAndPassword", result, userId, password);
		
		result.process(user->{
			if(user != null)
				session.put("user", user);
			
			responseResult.complete("/");
			
		}, failResult);
		
		failResult.setHandler(fail->{
			responseResult.complete("/");
		});
		
	}
}
```
* Use ```com.lovi.puppy.web.Session```
* ```session.put("user", user);```
* ```User loggedUser = session.get("user", User.class);```

##Service App
```java
@Service("userService")
public class UserService{

	@Autowired
	private UserRepository userRepository;
	
	@ServiceFunction
	public void insert(User user){
		userRepository.insert(user);
	}
	
	@ServiceFunction("_findAll")
	public List<User> findAll(){
		return userRepository.findAll();
	}
	....
}
```
####@Service
* Use ```com.lovi.puppy.annotation.Service```
* Class is marked as a service by using ```@Service```

####@ServiceFunction
* Use ```com.lovi.puppy.annotation.ServiceFunction```
* Method is marked as a service method by using ```@ServiceFunction```

##UI Service
```java
@UIService
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@UIServiceFunction(listenerAddress="li_findAllUsers", delay=1)
	public List<User> findAll(){
		return userRepository.findAll();
	}
}
```
* UI Services are used to make call from server side to clients side

####@UIService
* Use ```com.lovi.puppy.annotation.UIService```
* Class is marked as a ui service by using ```@UIService```

####@UIServiceFunction
* Use ```com.lovi.puppy.annotation.UIServiceFunction```
* Method is marked as a ui service method by using ```@UIServiceFunction```
* Fires every time a specified period of time has passed
* listenerAddress = address of the listener
* delay = time interval in minutes

####UICaller
* ```UICaller``` publish data to connected sockjs clients
* ```UICaller.call(String listenerAddress,Object message)```

####puppy-io-angular.js
```html
<body ng-app="myApp">
	<h1>User Manager App</h1>
	
	<span ui="val in li_usersCount">Count {{val}}</span>
	
	<ul>
		<li ui-repeat="user in li_findAllUsers">
			{{user.userId}} | {{user.name}}
		</li>
	</ul>
	....
</body>
```
```javascript
var app = angular.module("myApp", ['puppy-io']);
	   		
app.config(function(WebServerProvider){
	WebServerProvider.config('user-mgr',9000);
});
```
* [puppy-io-angular.js](https://github.com/loviworld/puppy-io-samples/blob/master/src/main/resources/web/static/js/puppy-io-angular.js)
* import 'puppy-io' module into your angular application
* 'puppy-io' module contain ```ui``` and ```ui-repeat``` directives which provides easy way to listen UIService calls

####puppy-io.js
```javascript
var serviceListener = new ServiceListener(appName, port);
serviceListener.onopen(function() {
	serviceListener.listen(listenerAddress, function(error, message) {
		....
	});
});
```
* [puppy-io.js](https://github.com/loviworld/puppy-io-samples/blob/master/src/main/resources/web/static/js/puppy-io.js)

###Technologies used in puppy-io
* Vert.x
* Spring Boot
* Hazelcast

###Authors
* Tharanga Thennakoon - tharanganilupul@gmail.com 
* [Linkedin](https://lk.linkedin.com/in/tharanga-thennakoon-036b2083)

###License
This project is licensed under the Apache Licensed V2
