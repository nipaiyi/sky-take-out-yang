package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

//自定义切面类 实现公共字段自动填充
@Aspect
@Slf4j
@Component
public class AutoFillAspect {

    //切点
    //前一半定义了要影响的范围 只在我的mapper下  后一半限制必须携带AutoFill注解的才可被生效 也就是触发切面
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillCut(){};  //这个方法仅仅是为了给切点起名字  比如在下面就用到了   其实完全可以在Before中直接写切点表达式 但是复用性不好 所以不选则哪种方法

    @Before("autoFillCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {  //其中的JoinPoint 只读信息 ProceedingJoinPoint 是可以修改的  不同业务逻辑
        log.info("AutoFillAspect.before 进行操作之前");
        //获取当前被拦截的方法上的数据库操作类型
        org.aspectj.lang.reflect.MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //方法签名对象
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);  //获得方法上的注解信息
        OperationType value = annotation.value();  //获得value 值

        //获取到当前被拦截的方法的参数  实体对象
        Object[] args = joinPoint.getArgs();  //获得所有参数
        if (args == null && args.length == 0) {
            return;
        }

        Object arg = args[0];  //默认第一个参数为实体 也就是存储我要新增或者更新的数据

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前不同的操作类型，为对应的属性通过反射来赋值
        if (value == OperationType.INSERT) {
            //为四个字段赋值 通过反射来赋值
            //通过反射拿到

//            Method setCreatTime = arg.getClass().getDeclaredMethod("setCreatTime", LocalDateTime.class);
//            Method setCreatUser = arg.getClass().getDeclaredMethod("setCreatUser", Log.class);
//            Method setUpdateTime = arg.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
//            Method setUpdateUser = arg.getClass().getDeclaredMethod("setUpdateUser", Log.class);

            //用上面的方法不会错 但是直接写setCreatTime 这种容易报错  所以使用了定义的常量去代替
            Method setCreatTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreatUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射为对象赋值
            setCreatTime.invoke(arg,now);
            setCreatUser.invoke(arg,currentId);
            setUpdateTime.invoke(arg,now);
            setUpdateUser.invoke(arg,currentId);


        }else {
            //为两个字段赋值即可
            Method setUpdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            setUpdateTime.invoke(arg,now);
            setUpdateUser.invoke(arg,currentId);
        }


    }



}
