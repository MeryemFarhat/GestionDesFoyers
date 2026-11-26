package tn.esprit.gestionfoyer.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(0)
@Slf4j
public class ReservationTimingAspect {
    @Around("execution(* tn.esprit.gestionfoyer.services.ReservationServiceImpl.ajouterReservation(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            log.info("ajouterReservation execute en {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
    }
}
