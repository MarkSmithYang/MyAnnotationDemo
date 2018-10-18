package com.yb.annotation.handler;

import com.yb.annotation.anno.Age;
import com.yb.annotation.anno.Ages;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MemberSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.rmi.ServerError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yangbiao
 * @Description:注解处理
 * @date 2018/10/10
 */
@Aspect
@Component
public class AnnoHandler {
    public static final Logger log = LoggerFactory.getLogger(AnnoHandler.class);

    // 用@PointCut注解统一声明,然后在其它通知中引用该统一声明即可！
    //@Pointcut("@annotation(com.yb.annotation.anno.SetVaule)")
//    @Pointcut(value = "execution(* com.yb.annotation.*..*.*(..)) && @annotation(com.yb.annotation.anno.Age)")
    @Pointcut(value = "execution(* com.yb.annotation.*..*.*(..)) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
//    @Pointcut(value = "execution(* com.yb.annotation.*..*.*(..)) && @annotation(com.yb.annotation.anno.Age)")
    public void setVaulePointcut() {
    }

    @Before("setVaulePointcut()")
    public void beforeTest() {
        System.err.println("切之前");
    }

    @After("setVaulePointcut()")
    public void afterTest() {
        System.err.println("切之后--------");
    }

    @Around("setVaulePointcut()")
    public Object doSetVaule(ProceedingJoinPoint joinPoint) throws Throwable {
        Signature sig = joinPoint.getSignature();
        //强转(需要转成对应的Signature才能获取到对应的信息(字段,方法等)),需要根据Target来转换
        ///实测证明,想要获取参数的注解,必须要能注解到方法,不然不会生效
        if (!(sig instanceof MethodSignature)) {
            throw new NoSuchFieldError("签名不匹配");
        }
        MethodSignature methodSignature = (MethodSignature) sig;
        //获取方法的参数的注解
        Method method = methodSignature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        //获取方法的参数(值)
        Object[] args = joinPoint.getArgs();
        boolean notEmpty = ArrayUtils.isNotEmpty(parameterAnnotations);
        if (notEmpty) {
            //Annotation annotation = parameterAnnotations[0][0];//第一个参数根本没有注解,所以报错了,不能这样区
            //判断参数是否有注解
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (int j = 0; j < annotations.length; j++) {
                    Annotation annotation = annotations[j];
                    //获取参数上的指定注解(需要做如下判断,只获取需要的)---注意需要首先判断有没有重复注解的存在
                    //处理多个注解的部分
                    if(annotation instanceof Ages){
                        System.err.println("处理重复注解的逻辑");
                        //注解要么有默认值,要么必填,所以取出的数组必然不会为空
                        Age[] values = ((Ages) annotation).value();
                        //保险的做法还是判断所属关系,主要是担心@Ages里面有除了@Age的注解,而导致异常错误
                        for(Age age:values){
                            String message = age.message();
                            int value = age.value();
                            if (args[i] instanceof Integer) {
                                if ((Integer) args[i] < value) {
                                    log.info(message);
                                }
                            };
                        }
                    }
                    //处理单个注解的部分
                    if (annotation instanceof Age) {
                        System.err.println("处理单个注解的逻辑");
                        Age age = (Age) annotation;
                        Integer value = age.value();
                        String message = age.message();
                        if (args[i] instanceof Integer) {
                            if ((Integer) args[i] < value) {
                                log.info(message);
                            }
                        }
                    }
                }
            }
        }
        return joinPoint.proceed();
    }

    @AfterThrowing(value = "setVaulePointcut()", throwing = "ex")
    public void afterThrowing(Exception ex) {
        //(测试的是around)出现异常后执行
        System.err.println("之后抛异常");
    }
}
