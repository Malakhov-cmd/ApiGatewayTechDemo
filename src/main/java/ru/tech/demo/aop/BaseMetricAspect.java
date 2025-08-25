package ru.tech.demo.aop;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.tech.demo.util.CountingHttpServletResponseWrapperUtil;

import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class BaseMetricAspect {
    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Counter> successCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> requestBytesCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> responseBytesCounters = new ConcurrentHashMap<>();

    public BaseMetricAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(BaseMetrics)")
    public Object count(ProceedingJoinPoint pjp, BaseMetrics countedMetric) throws Throwable {
        String metricName = countedMetric.value();
        if (metricName.isEmpty()) {
            metricName = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
        }

        Counter successCounter = successCounters.computeIfAbsent(
                metricName,
                name -> meterRegistry.counter("controller.calls.total", "method", name)
        );
        Counter errorCounter = errorCounters.computeIfAbsent(
                metricName,
                name -> meterRegistry.counter("controller.calls.errors", "method", name)
        );
        Counter requestBytesCounter = requestBytesCounters.computeIfAbsent(
                metricName,
                name -> meterRegistry.counter("controller.request.bytes", "method", name)
        );
        Counter responseBytesCounter = responseBytesCounters.computeIfAbsent(
                metricName,
                name -> meterRegistry.counter("controller.response.bytes", "method", name)
        );

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        HttpServletResponse response = attrs != null ? attrs.getResponse() : null;

        long requestSize = request != null ? request.getContentLengthLong() : -1;
        CountingHttpServletResponseWrapperUtil responseWrapper = null;

        if (response != null) {
            responseWrapper = new CountingHttpServletResponseWrapperUtil(response);
        }

        try {
            Object result = pjp.proceed();

            successCounter.increment();

            if (requestSize > 0) {
                requestBytesCounter.increment(requestSize);
            }

            if (responseWrapper != null) {
                responseBytesCounter.increment(responseWrapper.getContentSize());
            }

            return result;
        } catch (Throwable ex) {
            errorCounter.increment();
            throw ex;
        }
    }
}
