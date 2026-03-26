package com.dream.change.aspect;

import com.dream.change.annotation.NoRepeatSubmit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交切面
 */
@Aspect
@Component
public class NoRepeatSubmitAspect {

    private static final Logger logger = LoggerFactory.getLogger(NoRepeatSubmitAspect.class);

    // 内存缓存，存储请求信息
    private final ConcurrentHashMap<String, Long> requestCache = new ConcurrentHashMap<>();

    /**
     * 环绕通知，处理防止重复提交逻辑
     */
    @Around("@annotation(com.dream.change.annotation.NoRepeatSubmit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解信息
        NoRepeatSubmit noRepeatSubmit = method.getAnnotation(NoRepeatSubmit.class);
        int interval = noRepeatSubmit.value();
        
        // 生成请求唯一标识
        String key = generateKey(request, method);
        logger.debug("防重复提交 key: {}", key);
        
        // 检查是否重复提交
        Long lastSubmitTime = requestCache.get(key);
        long currentTime = System.currentTimeMillis();
        
        if (lastSubmitTime != null && (currentTime - lastSubmitTime) < interval * 1000) {
            logger.warn("重复提交请求: {}", key);
            throw new Exception("请勿重复提交请求，请稍后再试");
        }
        
        // 存储请求时间
        requestCache.put(key, currentTime);
        
        // 执行方法
        try {
            return point.proceed();
        } finally {
            // 清理缓存（可选，根据业务需求）
            // requestCache.remove(key);
        }
    }

    /**
     * 生成请求唯一标识
     */
    private String generateKey(HttpServletRequest request, Method method) {
        // 可以根据实际情况生成更唯一的key
        // 例如：IP + 请求路径 + 方法名 + 请求参数
        String ip = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        String methodName = method.getName();
        
        return ip + ":" + requestURI + ":" + methodName;
    }
}