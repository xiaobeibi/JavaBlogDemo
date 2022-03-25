<center><h2>Java博客项目</h2></center>

项目效果：http://obgeqwh.top:8080

---

![20220308140932](https://tuyong.oss-cn-hangzhou.aliyuncs.com/img/202203081547166.jpg)



## 项目技术

后端：Springboot + MybatisPlus + Redis + MySQL

前端：Vue + Axios



### MySQL表结构

![20220308143419](https://tuyong.oss-cn-hangzhou.aliyuncs.com/img/202203081548615.jpg)



### 统一异常处理

对加了 @Controller 注解的方法进行拦截处理——AOP 的实现

```java
@ControllerAdvice
public class AllExceptionHandler {
    //进行异常处理，处理Exception.class的异常
    @ExceptionHandler(Exception.class)
    @ResponseBody //返回json数据
    public Result doException(Exception ex) {
        ex.printStackTrace();
        return Result.fail(-999,"系统异常");
    }
}
```



### 登录注册功能

使用JWT技术，生成加密的token，存入Redis中，设置过期时间。

注册功能加事务。

登录功能加拦截器。

使用 ThreadLocal 在 controller 中直接获取用户的信息。

```java
public class UserThreadLocal {

    private UserThreadLocal(){}

    private static final ThreadLocal<SysUser> LOCAL = new ThreadLocal<>();

    public static void put(SysUser sysUser){
        LOCAL.set(sysUser);
    }
    public static SysUser get(){
        return LOCAL.get();
    }
    public static void remove(){
        LOCAL.remove();
    }
}
```



### 线程池

更新阅读次数。



### 精度损失

```java
@JsonSerialize(using = ToStringSerializer.class)
```

防止前端精度损失，把分布式 id 转为 string



### AOP日志

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {

    String module() default "";

    String operation() default "";
}
```

```java
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Pointcut("@annotation(com.mszlu.blog.common.aop.LogAnnotation)")
    public void logPointCut() {
    }
    
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = point.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志
        recordLog(point, time);
        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);
        log.info("=====================log start================================");
        log.info("module:{}",logAnnotation.module());
        log.info("operation:{}",logAnnotation.operation());

        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        log.info("request method:{}",className + "." + methodName + "()");

//        //请求的参数
        Object[] args = joinPoint.getArgs();
        String params = JSON.toJSONString(args[0]);
        log.info("params:{}",params);

        //获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        log.info("ip:{}", IpUtils.getIpAddr(request));

        log.info("excute time : {} ms",time);
        log.info("=====================log end================================");
    }
}
```



### 七牛云图片上传

略



### 统一缓存处理

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    long expire() default 1 * 60 * 1000;

    String name() default "";

}
```

```java
@Aspect
@Component
@Slf4j
public class CacheAspect {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Pointcut("@annotation(com.mszlu.blog.common.cache.Cache)")
    public void pt(){}

    @Around("pt()")
    public Object around(ProceedingJoinPoint pjp){
        try {
            Signature signature = pjp.getSignature();
            //类名
            String className = pjp.getTarget().getClass().getSimpleName();
            //调用的方法名
            String methodName = signature.getName();
            Class[] parameterTypes = new Class[pjp.getArgs().length];
            Object[] args = pjp.getArgs();
            //参数
            String params = "";
            for(int i=0; i<args.length; i++) {
                if(args[i] != null) {
                    params += JSON.toJSONString(args[i]);
                    parameterTypes[i] = args[i].getClass();
                }else {
                    parameterTypes[i] = null;
                }
            }
            if (StringUtils.isNotEmpty(params)) {
                //加密 以防出现key过长以及字符转义获取不到的情况
                params = DigestUtils.md5Hex(params);
            }
            Method method = pjp.getSignature().getDeclaringType().getMethod(methodName, parameterTypes);
            //获取Cache注解
            Cache annotation = method.getAnnotation(Cache.class);
            //缓存过期时间
            long expire = annotation.expire();
            //缓存名称
            String name = annotation.name();
            //先从redis获取
            String redisKey = name + "::" + className+"::"+methodName+"::"+params;
            String redisValue = redisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotEmpty(redisValue)){
                log.info("走了缓存~~~,{},{}",className,methodName);
                return JSON.parseObject(redisValue, Result.class);
            }
            Object proceed = pjp.proceed();
            redisTemplate.opsForValue().set(redisKey,JSON.toJSONString(proceed), Duration.ofMillis(expire));
            log.info("存入缓存~~~ {},{}",className,methodName);
            return proceed;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return Result.fail(-999,"系统错误");
    }
}
```



## 界面效果

### 文章分类

![20220308141011](https://tuyong.oss-cn-hangzhou.aliyuncs.com/img/202203081548743.jpg)

### 文章标签

![20220308141030](https://tuyong.oss-cn-hangzhou.aliyuncs.com/img/202203081548399.jpg)

### 文章归档

![20220308141050](https://tuyong.oss-cn-hangzhou.aliyuncs.com/img/202203081548434.jpg)

### 写文章

![20220308141108](https://tuyong.oss-cn-hangzhou.aliyuncs.com/img/202203081548031.jpg)



## 项目来源说明

https://www.bilibili.com/video/BV1Gb4y1d7zb?share_source=copy_web

感谢码神之路up主的视频支持，获益匪浅。



## 本项目展示

http://obgeqwh.top:8080

