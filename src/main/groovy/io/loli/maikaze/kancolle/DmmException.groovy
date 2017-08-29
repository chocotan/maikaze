package io.loli.maikaze.kancolle;

/**
 * Created by uzuma on 2017/8/24.
 */
public class DmmException extends RuntimeException{
    public DmmException() {
    }

    public DmmException(String message) {
        super(message);
    }

    public DmmException(String message, Throwable cause) {
        super(message, cause);
    }

    public DmmException(Throwable cause) {
        super(cause);
    }

    public DmmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
