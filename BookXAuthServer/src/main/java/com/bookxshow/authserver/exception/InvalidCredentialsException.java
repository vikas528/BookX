/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.exception;

import com.bookxshow.authserver.common.Constants;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super(Constants.ERR_INVALID_CREDENTIALS);
    }
}
