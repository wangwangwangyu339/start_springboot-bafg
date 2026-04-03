package com.example.demo.config;

import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SpanStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Sentry 接口性能追踪 AOP
 * <p>
 * 对所有 Controller 方法自动创建 Sentry Transaction，
 * 追踪接口响应时间，异常自动上报。
 * </p>
 */
@Slf4j
@Aspect
@Component
public class SentryTracingAspect {

    /**
     * 拦截所有 Controller 层方法进行性能追踪
     */
    @Around("execution(* com.example.demo.controller..*(..))")
    public Object traceController(ProceedingJoinPoint pjp) throws Throwable {
        String className  = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        String opName     = className + "." + methodName;

        ITransaction transaction = Sentry.startTransaction(opName, "http.server");
        try {
            Object result = pjp.proceed();
            transaction.setStatus(SpanStatus.OK);
            return result;
        } catch (Throwable t) {
            transaction.setStatus(SpanStatus.INTERNAL_ERROR);
            transaction.setThrowable(t);
            throw t;
        } finally {
            transaction.finish();
        }
    }
}
