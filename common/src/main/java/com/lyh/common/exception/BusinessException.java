package com.lyh.common.exception;

import com.lyh.common.result.CodeMsg;
import lombok.Data;

@Data
public class BusinessException extends RuntimeException {
    private CodeMsg codeMsg;

    public BusinessException(CodeMsg codeMsg) {
        super(codeMsg.getMsg());
        this.codeMsg = codeMsg;
    }
}
