/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.exception;

import com.bookxshow.authserver.common.Constants;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(Constants.ERR_EMAIL_EXISTS + email);
    }
}
