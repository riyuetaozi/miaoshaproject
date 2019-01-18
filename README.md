# miaoshaproject
Spring Boot + Mybatis 项目模拟商品秒杀功能

项目环境：
IntellijIdea、 Mysql5.6、JDK1.8 、 Maven3.5
使用Druid数据库连接池
使用Mybatis自动生成器生成Mapper、MapperXml、Do相关

项目处理：
1、返回统一的数据格式、
2、controller通用异常处理，控制器必须继承BaseController，使用@ExceptionHandler(Exception.class 可自定义异常类)解决未被controller层吸收处理的exception、
3、校验规则通过注解校验传入参数（待优化：还需要将ValidatorImpl通过aop的方式进行注解加入到对象中）、
4、跨域请求：控制器类上需要加入 @CrossOrigin(origins = "*", allowCredentials = "true") 或者 @CrossOrigin(allowedHeaders = "*", allowCredentials = "true")
 * @CrossOrigin 注解是解决前端跨越请求，
 * 注意：方法上需要注明RequestMethod的值；
 * session共享问题,@CrossOrigin注解需要加入(allowedHeaders = "*", allowCredentials = "true") 或者 (origins = "*", allowCredentials = "true")
 * 说明：DEFAULT_ALLOWED_HEADERS ：允许跨域传输所有的header参数，将用于使用token放入header域做session共享的跨域请求
         DEFAULT_ALLOW_CREDENTIALS = true ：需配合前端设置xhrFields授信后使得跨域session共享（也就是前端所有ajax需要加入 xhrFields:{withCredentials:true} 参数）
5、事务传递机制（有待专研）：
 * Spring遇到嵌套事务时，当被嵌套的事务被定义为“PROPAGATION_REQUIRES_NEW”时，
 * 内层Service的方法被调用时，外层方法的事务被挂起；内层事务相对于外层事务是完全独立的，有独立的隔离性等等。
 * 但是要使被嵌套方法PROPAGATION_REQUIRES_NEW事务有效，A和B两个方法不要在同一个类中，在A所在的类中注入B方法所在的类，然后A方法再调用B方法

完成功能：
用户登录：/user/login
获取验证码：/user/getotp
用户注册：/user/register
根据用户id获取用户：/user/get

创建商品：/item/createItem
商品列表：/item/itemList
根据商品id获取商品详情：/item/getItem

下单：/order/createOrder

项目采用前后端分离，后端数据分层：
dataobject（*Do属性和数据库字段保持一致）
service的model（*Model用于组合从数据库获取字段，可能组合多个*Do）
controller的viewobject（*Vo提供给前端展示属性，可能组合多个*Model）

