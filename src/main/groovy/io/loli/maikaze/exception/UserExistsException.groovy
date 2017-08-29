package io.loli.maikaze.exception

/**
 * @author yewenlin
 */
class UserExistsException extends RuntimeException {
    UserExistsException() {
    }

    UserExistsException(String var1) {
        super(var1)
    }

    UserExistsException(String var1, Throwable var2) {
        super(var1, var2)
    }

    UserExistsException(Throwable var1) {
        super(var1)
    }

    UserExistsException(String var1, Throwable var2, boolean var3, boolean var4) {
        super(var1, var2, var3, var4)
    }
}
