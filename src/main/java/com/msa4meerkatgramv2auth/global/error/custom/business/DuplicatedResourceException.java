package com.msa4meerkatgramv2auth.global.error.custom.business;

import com.msa4meerkatgramv2auth.global.error.custom.BusinessException;
import com.msa4meerkatgramv2auth.global.response.constant.CustomResponseCode;

public class DuplicatedResourceException extends BusinessException {
    public DuplicatedResourceException(String message) {
        super(CustomResponseCode.DUPLICATED_RESOURCE_ERROR, message);
    }
}
