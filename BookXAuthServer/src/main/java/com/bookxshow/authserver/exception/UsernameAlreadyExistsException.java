/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.exception;

import com.bookxshow.authserver.common.Constants;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super(Constants.ERR_USERNAME_EXISTS + username);
    }
}
