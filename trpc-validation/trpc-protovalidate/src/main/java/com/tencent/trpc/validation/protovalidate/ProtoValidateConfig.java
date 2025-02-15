/*
 * Tencent is pleased to support the open source community by making tRPC available.
 *
 * Copyright (C) 2023 THL A29 Limited, a Tencent company.
 * All rights reserved.
 *
 * If you have downloaded a copy of the tRPC source code from Tencent,
 * please note that tRPC source code is licensed under the Apache 2.0 License,
 * A copy of the Apache 2.0 License can be found in the LICENSE file.
 */

package com.tencent.trpc.validation.protovalidate;

import com.tencent.trpc.core.common.config.PluginConfig;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;

/**
 * protovalidate related plugin configuration.
 */
public class ProtoValidateConfig {

    /**
     * Configuration key for whether the response body needs data validation.
     */
    private static final String ENABLE_RESPONSE_VALIDATION_KEY = "response_validation";

    /**
     * Whether the response body needs data validation, default is false.
     */
    private boolean enableResponseValidation = Boolean.FALSE;

    /**
     * Parse validation plugin configuration.
     *
     * @param pluginConfig validation plugin TRPC framework configuration.
     * @return protovalidate configuration.
     */
    public static ProtoValidateConfig parse(PluginConfig pluginConfig) {
        ProtoValidateConfig config = new ProtoValidateConfig();
        Map<String, Object> extMap = pluginConfig.getProperties();
        config.setEnableResponseValidation(
                MapUtils.getBooleanValue(extMap, ENABLE_RESPONSE_VALIDATION_KEY, Boolean.FALSE));
        return config;
    }

    public boolean isEnableResponseValidation() {
        return enableResponseValidation;
    }

    public void setEnableResponseValidation(boolean enableResponseValidation) {
        this.enableResponseValidation = enableResponseValidation;
    }
}
