/**
 * Copyright (c) 2016-2020, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.rpc.dubbo;


import io.jboot.app.config.JbootConfigManager;
import io.jboot.app.config.JbootConfigUtil;
import io.jboot.components.rpc.JbootrpcReferenceConfig;
import io.jboot.components.rpc.JbootrpcServiceConfig;
import io.jboot.utils.StrUtil;
import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author michael yang (fuhai999@gmail.com)
 * @Date: 2020/3/19
 */
class DubboUtil {

    private static Map<String, ProtocolConfig> protocolConfigMap = new ConcurrentHashMap<>();
    private static Map<String, RegistryConfig> registryConfigMap = new ConcurrentHashMap<>();
    private static Map<String, ProviderConfig> providerConfigMap = new ConcurrentHashMap<>();
    private static Map<String, ConsumerConfig> consumerConfigMap = new ConcurrentHashMap<>();

    private static Map<String, MethodConfig> methodConfigMap = new ConcurrentHashMap<>();
    private static Map<String, ArgumentConfig> argumentConfigMap = new ConcurrentHashMap<>();


    public static void initDubbo() {
        DubboBootstrap dubboBootstrap = DubboBootstrap.getInstance();

        //application 配置
        ApplicationConfig applicationConfig = config(ApplicationConfig.class, "jboot.rpc.dubbo.application");
        dubboBootstrap.application(applicationConfig);


        //ssl 配置
        SslConfig sslConfig = config(SslConfig.class, "jboot.rpc.dubbo.ssl");
        dubboBootstrap.ssl(sslConfig);


        //monitor 配置
        MonitorConfig monitorConfig = config(MonitorConfig.class, "jboot.rpc.dubbo.monitor");
        dubboBootstrap.monitor(monitorConfig);


        //monitor 配置
        MetricsConfig metricsConfig = config(MetricsConfig.class, "jboot.rpc.dubbo.metrics");
        dubboBootstrap.metrics(metricsConfig);


        //module 配置
        ModuleConfig moduleConfig = config(ModuleConfig.class, "jboot.rpc.dubbo.module");
        dubboBootstrap.module(moduleConfig);


        //protocol 配置
        Map<String, ProtocolConfig> protocolConfigs = configs(ProtocolConfig.class, "jboot.rpc.dubbo.protocol");
        if (protocolConfigs != null && !protocolConfigs.isEmpty()) {
            protocolConfigMap.putAll(protocolConfigs);
            if (protocolConfigs.size() == 1) {
                dubboBootstrap.protocol(getDefault(protocolConfigs));
            } else {
                dubboBootstrap.protocols((List<ProtocolConfig>) toList(protocolConfigs));
            }
        }

        //registry 配置
        Map<String, RegistryConfig> registryConfigs = configs(RegistryConfig.class, "jboot.rpc.dubbo.registry");
        if (registryConfigs != null && !registryConfigs.isEmpty()) {
            registryConfigMap.putAll(registryConfigs);
            if (registryConfigs.size() == 1) {
                dubboBootstrap.registry(getDefault(registryConfigs));
            } else {
                dubboBootstrap.registries((List<RegistryConfig>) toList(registryConfigs));
            }
        }


        //provider 配置
        Map<String, ProviderConfig> providerConfigs = configs(ProviderConfig.class, "jboot.rpc.dubbo.provider");
        if (providerConfigs != null && !providerConfigs.isEmpty()) {
            providerConfigMap.putAll(providerConfigs);
            if (providerConfigs.size() == 1) {
                dubboBootstrap.provider(getDefault(providerConfigs));
            } else {
                dubboBootstrap.providers((List<ProviderConfig>) toList(providerConfigs));
            }
        }


        //consumer 配置
        Map<String, ConsumerConfig> consumerConfigs = configs(ConsumerConfig.class, "jboot.rpc.dubbo.consumer");
        if (consumerConfigs != null && !consumerConfigs.isEmpty()) {
            consumerConfigMap.putAll(consumerConfigs);
            if (consumerConfigs.size() == 1) {
                dubboBootstrap.consumer(getDefault(consumerConfigs));
            } else {
                dubboBootstrap.consumers((List<ConsumerConfig>) toList(consumerConfigs));
            }
        }

        //methodConfig 配置
        Map<String, MethodConfig> methodConfigs = configs(MethodConfig.class, "jboot.rpc.dubbo.method");
        if (methodConfigs != null && !methodConfigs.isEmpty()) {
            methodConfigMap.putAll(methodConfigs);
        }


        //argumentConfig 配置
        Map<String, ArgumentConfig> argumentConfigs = configs(ArgumentConfig.class, "jboot.rpc.dubbo.argument");
        if (argumentConfigs != null && !argumentConfigs.isEmpty()) {
            argumentConfigMap.putAll(argumentConfigs);
        }
    }


    public static ReferenceConfig toReferenceConfig(JbootrpcReferenceConfig rc){
        ReferenceConfig referenceConfig = new ReferenceConfig();
        Field[] fields = rc.getClass().getDeclaredFields();
        for (Field field : fields){
            try {
                Method method = ReferenceConfig.class.getDeclaredMethod("set"+StrUtil.firstCharToUpperCase(field.getName()),field.getType());
                field.setAccessible(true);
                method.invoke(referenceConfig,field.get(rc));
            } catch (Exception e) {
               // ignore
            }
        }
        return referenceConfig;
    }


    public static ServiceConfig toServiceConfig(JbootrpcServiceConfig sc){
        ServiceConfig serviceConfig = new ServiceConfig();
        Field[] fields = sc.getClass().getDeclaredFields();
        for (Field field : fields){
            try {
                Method method = ServiceConfig.class.getDeclaredMethod("set"+StrUtil.firstCharToUpperCase(field.getName()),field.getType());
                field.setAccessible(true);
                method.invoke(serviceConfig,field.get(sc));
            } catch (Exception e) {
                // ignore
            }
        }
        return serviceConfig;
    }



    private static <T> T config(Class<T> clazz, String prefix) {
        return JbootConfigManager.me().get(clazz, prefix, null);
    }


    private static <T> Map<String, T> configs(Class<T> clazz, String prefix) {
        return JbootConfigUtil.getConfigModels(clazz, prefix);
    }

    private static <T> T getDefault(Map<String, T> map) {
        return map.get("default");
    }

    private static List<? extends AbstractConfig> toList(Map<String, ? extends AbstractConfig> map) {
        List<AbstractConfig> arrayList = new ArrayList<>(map.size());
        for (Map.Entry<String, ? extends AbstractConfig> entry : map.entrySet()) {
            AbstractConfig config = entry.getValue();
            if (StrUtil.isBlank(config.getId())) {
                config.setId(entry.getKey());
            }
            arrayList.add(config);
        }
        return arrayList;
    }


}