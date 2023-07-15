package org.xyp.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.xyp.demo.domain.DummyUser;

@Component
@Aspect
@Slf4j
public class AopDaoAspect {
    @Pointcut("execution(public * org.xyp.demo.domain.UserDao.save(..))")
    public void validationPointcut() {}


    @Around("validationPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        var user = (DummyUser) joinPoint.getArgs()[0];
        var user2 = (DummyUser) joinPoint.getArgs()[0];
        log.info("{}", System.identityHashCode(user));
        log.info("{}", System.identityHashCode(user2));
        log.info("In Around Aspect {}", user);
        var args =  joinPoint.getArgs();
        args[0] = new DummyUser(100L, "changed");
        ((DummyUser) joinPoint.getArgs()[0]).setName("changed2");
        log.info("In Around Aspect after change user {}", joinPoint.getArgs()[0]);
        return joinPoint.proceed(args);
    }
}
