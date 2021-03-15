package com.scb.paymentintegration.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.scb.paymentintegration.constants.ErrorConstants;
import com.scb.paymentintegration.util.PropertyUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private PropertyUtils propertyUtils;

    private static final String ERROR = "error";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.TYPE_MISMATCH_EX_MSG), ex.getValue(), ex.getPropertyName(), ex.getRequiredType());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.MISSING_PART_EX_MSG), ex.getRequestPartName());

        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.MISSING_PARAM_EX_MSG), ex.getParameterName());
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.ARGUMENT_MISMATCH_EX_MSG), ex.getName());

        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final String error = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.NO_HANDLER_EX_MSG), ex.getHttpMethod(), ex.getRequestURL());

        final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" " + propertyUtils.getProperty(ErrorConstants.NO_HTTP_METHOD_EX_MSG) + " ");
        Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
        if(!CollectionUtils.isEmpty(supportedMethods)) {
            supportedMethods.forEach(t -> builder.append(t + " "));
        }

        final ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage(), builder.toString());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" " + propertyUtils.getProperty(ErrorConstants.MEDIA_NOT_SUPPORT_EX_MSG) + " ");
        List<MediaType> supportedMediaTypes = ex.getSupportedMediaTypes();
        if(!CollectionUtils.isEmpty(supportedMediaTypes)) {
            supportedMediaTypes.forEach(t -> builder.append(t + " "));
        }

        final ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getLocalizedMessage(), builder.substring(0, builder.length() - 2));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.SERVER_ERROR_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({AmazonS3Exception.class})
    public ResponseEntity<Object> handleKeyNotFound(final Exception ex, final WebRequest request) {
      logger.info(ex.getClass().getName(), ex);
      final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.AWS_S3_EX_MSG));
      return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({CryptoException.class})
    public ResponseEntity<Object> handleCryptoException(final CryptoException ex, final WebRequest request) {
        logger.info(ex.getClass().getName(), ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.ENCRYPT_DECRYPT_FILE_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({InvalidDataException.class})
    public ResponseEntity<Object> handleInvalidDataException(final InvalidDataException ex, final WebRequest request) {
        logger.info(ex.getClass().getName(), ex);
        ex.getMessages().forEach(logger::info);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, propertyUtils.getProperty(ErrorConstants.INVALID_DATA_EX_MSG), ex.getMessages());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({HashProgramValidationException.class})
    public ResponseEntity<Object> handleHashProgramValidationException(final HashProgramValidationException ex, final WebRequest request) {
        logger.info(ex.getClass().getName(), ex);
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.HASH_VALIDATION_FAILED_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ExternalServiceInvocationException.class})
    public ResponseEntity<Object> handleExternalServiceInvocationException(final ExternalServiceInvocationException ex, final WebRequest request) {
        logger.info(ex.getClass().getName(), ex);
        final ApiError apiError = new ApiError(HttpStatus.FAILED_DEPENDENCY, ex.getMessage(), propertyUtils.getProperty(ErrorConstants.EXTERNAL_SERVICE_INVOCATION_FAILED_EX_MSG));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

}