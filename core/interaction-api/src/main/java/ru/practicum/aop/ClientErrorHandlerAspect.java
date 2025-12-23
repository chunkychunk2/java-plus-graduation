package ru.practicum.aop;

import feign.FeignException;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

@Aspect
@Component
public class ClientErrorHandlerAspect {

    @Pointcut("@annotation(ru.practicum.aop.ClientErrorHandler)")
    public void isAnnotatedWithClientErrorHandler() {

    }

    @AfterThrowing(value = "isAnnotatedWithClientErrorHandler()", throwing = "ex")
    public void handleFeignException(Throwable ex) throws Throwable {
        if (ex instanceof FeignException feignException) {
            int status = feignException.status();
            switch (status) {
                case 400 -> throw new BadRequestException(feignException.contentUTF8());
                case 403 -> throw new ForbiddenException(feignException.contentUTF8());
                case 404 -> throw new NotFoundException(feignException.contentUTF8());
                case 409 -> throw new ConflictException(feignException.contentUTF8());
                default -> throw feignException;
            }
        } else {
            throw ex;
        }
    }

}
