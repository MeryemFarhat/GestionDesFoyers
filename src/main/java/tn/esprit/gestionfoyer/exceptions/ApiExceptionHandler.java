package tn.esprit.gestionfoyer.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(FoyerException.class)
    public ProblemDetail handleDomainError(FoyerException exception) {
        return ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleConstraint() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Valeur unique deja utilisee ou relation incompatible");
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleLock() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Modification simultanee, veuillez reessayer");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(ConstraintViolationException exception) {
        String details = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);
    }

    @Override
    protected org.springframework.http.ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, org.springframework.http.HttpHeaders headers,
            org.springframework.http.HttpStatusCode status, org.springframework.web.context.request.WebRequest request) {
        exception.getBody().setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage()).toList());
        return super.handleMethodArgumentNotValid(exception, headers, status, request);
    }
}
