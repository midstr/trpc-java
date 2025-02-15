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

import com.tencent.trpc.core.common.ConfigManager;
import com.tencent.trpc.core.common.config.PluginConfig;
import com.tencent.trpc.core.extension.ExtensionLoader;
import com.tencent.trpc.core.filter.spi.Filter;
import com.tencent.trpc.core.rpc.GenericClient;
import com.tencent.trpc.core.rpc.Invoker;
import com.tencent.trpc.core.rpc.Request;
import com.tencent.trpc.core.rpc.Response;
import com.tencent.trpc.core.rpc.RpcClientContext;
import com.tencent.trpc.core.rpc.RpcInvocation;
import com.tencent.trpc.core.rpc.common.RpcMethodInfo;
import com.tencent.trpc.core.rpc.def.DefRequest;
import com.tencent.trpc.core.rpc.def.DefResponse;
import com.tencent.trpc.validation.protovalidate.HelloWorldProto.HelloWorld;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *  protovalidate Filter test class.
 */
public class ProtoValidateFilterTest {

    @Before
    public void before() {
        ConfigManager.stopTest();
        ConfigManager.startTest();
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("response_validation", false);
        PluginConfig pluginConfig = new PluginConfig("protovalidate", Filter.class,
                ProtoValidateFilter.class, config);
        ExtensionLoader.registerPlugin(pluginConfig);
    }

    @After
    public void after() {
        ConfigManager.stopTest();
    }

    @Test
    public void testProtoValidateFilterSucc() throws NoSuchMethodException, InterruptedException, ExecutionException {
        ExtensionLoader<?> extensionLoader = ExtensionLoader.getExtensionLoader(Filter.class);
        ProtoValidateFilter filter = (ProtoValidateFilter) extensionLoader.getExtension("protovalidate");
        Assert.assertNotNull(filter);
        HelloWorld request = HelloWorld.newBuilder().setMsg("3434334")
                .setEmail("simple@example.com")
                .build();
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArguments(new Object[]{request, "b"});
        rpcInvocation.setRpcMethodInfo(new RpcMethodInfo(GenericClient.class,
                GenericClient.class.getMethod("invoke", RpcClientContext.class, byte[].class)));
        DefRequest defRequest = new DefRequest();
        defRequest.setInvocation(rpcInvocation);
        CompletionStage<Response> response = filter.filter(new Invoker<Object>() {
            @Override
            public Class<Object> getInterface() {
                return Object.class;
            }

            @Override
            public CompletionStage<Response> invoke(Request request) {
                DefResponse defResponse = new DefResponse();
                HelloWorld response = HelloWorld.newBuilder().setMsg("hello world response")
                        .setEmail("xxx@example.com")
                        .build();
                defResponse.setValue(response);
                return CompletableFuture.completedFuture(defResponse);
            }
        }, defRequest);
        CompletableFuture<Response> resultFuture = response.toCompletableFuture();
        Assert.assertNull((resultFuture.get()).getException());
    }

    @Test
    public void testProtoValidateFilterFail() throws NoSuchMethodException, InterruptedException, ExecutionException {
        ExtensionLoader<?> extensionLoader = ExtensionLoader.getExtensionLoader(Filter.class);
        ProtoValidateFilter filter = (ProtoValidateFilter) extensionLoader.getExtension("protovalidate");
        Assert.assertNotNull(filter);
        HelloWorld request = HelloWorld.newBuilder().setMsg("a")
                .setEmail("abc")
                .build();
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArguments(new Object[]{request, "b"});
        rpcInvocation.setRpcMethodInfo(new RpcMethodInfo(GenericClient.class,
                GenericClient.class.getMethod("invoke", RpcClientContext.class, byte[].class)));
        DefRequest defRequest = new DefRequest();
        defRequest.setInvocation(rpcInvocation);

        CompletionStage<Response> response = filter.filter(new EmptyInvoker(), defRequest);
        CompletableFuture<Response> resultFuture = response.toCompletableFuture();
        try {
            Assert.assertNull(resultFuture.get());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    private static class EmptyInvoker implements Invoker<Object> {

        @Override
        public Class<Object> getInterface() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Response> invoke(Request request) {
            throw new UnsupportedOperationException();
        }
    }
}