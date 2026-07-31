package com.msa4meerkatgramv2auth.global.error.custom.business;

import com.msa4meerkatgramv2auth.global.error.custom.BusinessException;
import com.msa4meerkatgramv2auth.global.response.constant.CustomResponseCode;

public class FileManagedException extends BusinessException {
    public FileManagedException(String message) {
        super(CustomResponseCode.FILE_MANAGED_ERROR, message);
    }
}
