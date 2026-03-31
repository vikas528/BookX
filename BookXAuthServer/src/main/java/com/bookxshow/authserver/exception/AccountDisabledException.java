/*
 * BookXShow - Authorization Server
 * Copyright (c) 2026 BookXShow. All rights reserved.
 */
package com.bookxshow.authserver.exception;

import com.bookxshow.authserver.common.Constants;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String username) {
        super(Constants.ERR_ACCOUNT_DISABLED + " [" + username + "]");
    }
}
