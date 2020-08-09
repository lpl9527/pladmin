package com.lpl.aspect;

import com.lpl.domain.Log;
import com.lpl.service.LogService;
import com.lpl.utils.RequestHolder;
import com.lpl.utils.SecurityUtils;
import com.lpl.utils.StringUtils;
import com.lpl.utils.ThrowableUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lpl
 * 日志切面，用于记录用户操作日志
 */
@Component
@Aspect
@Slf4j
public class LogAscept {

    private final LogService logService;

    ThreadLocal<Long> threadLocal = new ThreadLocal<>();    //用于记录当前系统时间的本地线程，防止出现线程安全问题

    public LogAscept(LogService logService) {
        this.logService = logService;
    }

    /**
     * 配置切入点，标注了@Log注解的方法均为切面的切入点
     */
    @Pointcut("@annotation(com.lpl.annotation.Log)")
    public void logPointcut() {

    }

    /**
     * 配置环绕通知，使用logPointcut方法上注冊的切入点（当请求@Log注解标注的目标方法时，实际上是执行了此方法）
     *
     * @param joinPoint     我们可以通过 ProceedingJoinPoint连接点对象获取被代理对象（此时为方法）的一些信息，或执行目标对象方法
     * @return  方法执行结果
     * @throws Throwable
     */
    @Around("logPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result;
        //将当前系统时间放入本地线程
        threadLocal.set(System.currentTimeMillis());
        //通过反射执行目标对象（即被代理对象）的连接点（即标注了切入点注解@Log）处的方法，并将方法执行结果返回
        result = joinPoint.proceed();
        //通过日志类型和请求连接点方法时间构造Log对象
        Log log = new Log("INFO", System.currentTimeMillis() - threadLocal.get());
        //将当前时间从本地线程中移除
        threadLocal.remove();

        HttpServletRequest request = RequestHolder.getHttpServletRequest();
        //保存用户操作信息到数据库
        logService.save(getUsername(), StringUtils.getBrowser(request), StringUtils.getIp(request), joinPoint, log);

        //返回方法执行结果
        return result;
    }

    /**
     * 配置异常通知，在切入点方法发生异常时执行
     * @param joinPoint 连接点对象
     * @param e 异常
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Log log = new Log("ERROR", System.currentTimeMillis() - threadLocal.get());
        threadLocal.remove();
        log.setExceptionDetail(ThrowableUtil.getStackTrace(e).getBytes());
        HttpServletRequest request = RequestHolder.getHttpServletRequest();

        //保存用户操作日志
        logService.save(getUsername(), StringUtils.getBrowser(request), StringUtils.getIp(request), (ProceedingJoinPoint) joinPoint, log);
    }

    /**
     * 获取当前登录的用户名，未登录时就为 ""
     */
    public String getUsername() {
        try {
            return SecurityUtils.getCurrentUsername();
        }catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
