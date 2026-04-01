package com.lyh.redis.constants;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

public class CommonRedisKey {
    @Getter
    public enum commonRedisKey {
        /**
         * 用户认证 token key
         */
        USER_TOKEN("userToken:", TimeUnit.MINUTES,30);

        commonRedisKey(String prefix, TimeUnit unit, int expireTime){
            this.prefix = prefix;
            this.unit = unit;
            this.expireTime = expireTime;
        }

        public String getRealKey(String key){
            return this.prefix+key;
        }

        private String prefix;
        private TimeUnit unit;
        private int expireTime;

    }
}
