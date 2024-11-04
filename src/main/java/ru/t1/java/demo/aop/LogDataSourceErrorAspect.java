package ru.t1.java.demo.aop;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.service.ErrorLogService;


import java.io.PrintWriter;
import java.io.StringWriter;


@Aspect
@Component
public class LogDataSourceErrorAspect {
    private final ErrorLogService errorLogService;

    public LogDataSourceErrorAspect(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @AfterThrowing(pointcut = "@annotation(LogDataSourceError)", throwing = "exception")
    public void logDataSourceError(JoinPoint joinPoint, Exception exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getName();
        String className = signature.getDeclaringTypeName();
        String methodSignature = className + "." + methodName + "()";

        StringWriter errorText = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorText));

        DataSourceErrorLog errorLog = new DataSourceErrorLog();
        errorLog.setStackTraceText(errorText.toString());
        errorLog.setStackTraceMessage(exception.getMessage());
        errorLog.setMethodSignature(methodSignature);

        errorLogService.saveErrorLog(errorLog);
    }
}