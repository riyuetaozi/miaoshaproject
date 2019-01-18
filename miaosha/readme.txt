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
6、不同环境配置（一般是 正式、测试、开发 三个环境的配置）：
    Spring Boot 的 Profile可以实现不同环境的配置，配置文件建议使用application.yml的格式
    （上线不同环境简单的更改application.yml配置文件中 profile的active属性值即可）
    说明：spring boot相关配置文件加载顺序，application.proerties 比 application.yml先加载
7、加入了logback日志框架，配置文件为resources下的logback-spring.xml
    日志框架最好是 门面日志框架 + 具体实现日志框架相结合 （本项目slf4j + logback），有利于维护和各个类的处理方式统一。
    说明：自定义对外抛出的错误日志信息，不用再打印一遍

项目中注意的细节：
1、Spring Boot启动类上记得加入 @MapperScan("com.miaoshaproject.dao") ，扫描dao、
2、控制器必须继承BaseController，使用@ExceptionHandler(Exception.class 可自定义异常类)解决未被controller层吸收处理的exception、
3、控制器类上需要加入 @CrossOrigin(origins = "*", allowCredentials = "true")，方法上需要注明RequestMethod的值，前端所有ajax需要加入 xhrFields:{withCredentials:true} 参数
4、Mapper接口需要加入 @Repository 注解

项目中细节知识：
1、控制器中使用httpServletRequest
    /**
     * 这里使用spring的bean的方式将HttpServletRequest注入进来（spring的bean注入，是单例模式，也就是说这里的HttpServletRequest是单例的模式）
     *
     * 单例的模式怎么可以支持一个request，使多个用户的并发访问呢？
     * 其实这个通过spring Bean包装的HttpServletRequest，它的本质是一个proxy(代理)，它的内部拥有ThreadLocal方式的Map，让用户在每个线程当中处理它自己对应的request，并且由ThreadLocal清除的机制
     */
    @Autowired
    private HttpServletRequest httpServletRequest;
2、使用JDK1.8的stream api功能，以及Lambda表达式（JDK8的新功能有待专研）
    List<ItemDo> itemDoList = itemDoMapper.selectItemList();
    //使用jdk8的新功能 List集合的stream() api功能，将List集合中的 ItemDo和ItemStockDo 转化为 ItemModel
    List<ItemModel> itemModelList = itemDoList.stream().map(itemDo -> {
        ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());
        ItemModel itemModel = this.convertItemModelFromItemDOAndStockDo(itemDo, itemStockDo);
        return itemModel;
    }).collect(Collectors.toList());
3、Mybatis传入多参数
    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
4、短信验证码的存取方式：
    * 将OTP验证码与对应的用户手机号关联
    * （分布式中，存放在redis中，redis能存储key-value的方式，并且可以简单的控制时间有效性）
    * 这里我们还没有涉及到分布式，所以使用HTTPSession的方式绑定他的手机号与otpcode
5、对象属性的复制
    BeanUtils.copyProperties(itemModel, itemVo);//需要属性类型和名称一致
6、金额类型建议使用BigDecimal类型，不要使用Double
    因为Double得到的值会有很多小数位，如10.9999999999999...，使用BigDecimal类型是保留两位小数，金额就精确到分了
7、时间类型建议使用joda-time的DateTime，不使用java.util.DateTime的
    或者使用spring的@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") 待测试

后续扩展：
后续使用本地缓存和集中式缓存解决性能相关的问题
本地缓存和集中式缓存也可以嵌入到对应的数据层当中
使用数据相关接入的模型的方式去接入我们的一个dataObject

思考题：
多商品、多库存、多活动模型怎么实现？

项目遗留问题：
如何支撑亿级秒杀流量？
如何发现容量问题？
如何使得系统水平扩展？
查询效率低下？
活动开始前页面被疯狂刷新？
库存行锁问题？
下单操作多，缓慢？
浪涌流量如何解决？
