
package io.shardingcat.config.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.shardingcat.util.StringUtil;

/**
 * @author shardingcat
 */
public class ParameterMapping {
    private static final Logger                              LOGGER      = LoggerFactory
                                                                             .getLogger(ParameterMapping.class);
    private static final Map<Class<?>, PropertyDescriptor[]> descriptors = new HashMap<Class<?>, PropertyDescriptor[]>();

    /**
     * 将property键值对赋值组装到object中
     * @param object 目标反射对象
     * @param parameter property的键值对
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void mapping(Object object, Map<String, ? extends Object> parameter) throws IllegalAccessException,
            InvocationTargetException {
        //获取用于导出clazz这个JavaBean的所有属性的PropertyDescriptor
        PropertyDescriptor[] pds = getDescriptors(object.getClass());
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];
            Object obj = parameter.get(pd.getName());
            Object value = obj;
            Class<?> cls = pd.getPropertyType();
            //类型转换
            if (obj instanceof String) {
                String string = (String) obj;
                if (!StringUtil.isEmpty(string)) {
                    string = ConfigUtil.filter(string);
                }
                if (isPrimitiveType(cls)) {
                    value = convert(cls, string);
                }
            } else if (obj instanceof BeanConfig) {
                value = createBean((BeanConfig) obj);
            } else if (obj instanceof BeanConfig[]) {
                List<Object> list = new ArrayList<Object>();
                for (BeanConfig beanconfig : (BeanConfig[]) obj) {
                    list.add(createBean(beanconfig));
                }
                value = list.toArray();
            }
            //赋值
            if (cls != null
                    && value != null) {
                    Method method = pd.getWriteMethod();
                    if (method != null) {
                        method.invoke(object, new Object[] { value });
                    }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Object createBean(BeanConfig config) throws IllegalAccessException, InvocationTargetException {
        Object bean = config.create(true);
        if (bean instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) bean;
            for (Map.Entry<String, Object> entry : config.getParams().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof BeanConfig) {
                    BeanConfig mapBeanConfig = (BeanConfig) entry.getValue();
                    value = mapBeanConfig.create(true);
                    mapping(value, mapBeanConfig.getParams());
                }
                map.put(key, value);
            }
        } else if (bean instanceof List) {
        } else {
            mapping(bean, config.getParams());
        }
        return bean;
    }

    /**
     * 用于导出clazz这个JavaBean的所有属性的PropertyDescriptor
     * @param clazz
     * @return
     */
    private static PropertyDescriptor[] getDescriptors(Class<?> clazz) {
        //PropertyDescriptor类表示JavaBean类通过存储器导出一个属性
        PropertyDescriptor[] pds;
        List<PropertyDescriptor> list;
        PropertyDescriptor[] pds2 = descriptors.get(clazz);
        //该clazz是否第一次加载
        if (null == pds2) {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                pds = beanInfo.getPropertyDescriptors();
                list = new ArrayList<PropertyDescriptor>();
                //加载每一个类型不为空的property
                for (int i = 0; i < pds.length; i++) {
                    if (null != pds[i].getPropertyType()) {
                        list.add(pds[i]);
                    }
                }
                pds2 = new PropertyDescriptor[list.size()];
                list.toArray(pds2);
            } catch (IntrospectionException ie) {
                LOGGER.error("ParameterMappingError", ie);
                pds2 = new PropertyDescriptor[0];
            }
        }
        descriptors.put(clazz, pds2);
        return (pds2);
    }

    private static Object convert(Class<?> cls, String string) {
        Method method = null;
        Object value = null;
        if (cls.equals(String.class)) {
            value = string;
        } else if (cls.equals(Boolean.TYPE)) {
            value = Boolean.valueOf(string);
        } else if (cls.equals(Byte.TYPE)) {
            value = Byte.valueOf(string);
        } else if (cls.equals(Short.TYPE)) {
            value = Short.valueOf(string);
        } else if (cls.equals(Integer.TYPE)) {
            value = Integer.valueOf(string);
        } else if (cls.equals(Long.TYPE)) {
            value = Long.valueOf(string);
        } else if (cls.equals(Double.TYPE)) {
            value = Double.valueOf(string);
        } else if (cls.equals(Float.TYPE)) {
            value = Float.valueOf(string);
        } else if ((cls.equals(Boolean.class)) || (cls.equals(Byte.class)) || (cls.equals(Short.class))
                || (cls.equals(Integer.class)) || (cls.equals(Long.class)) || (cls.equals(Float.class))
                || (cls.equals(Double.class))) {
            try {
                method = cls.getMethod("valueOf", new Class[] { String.class });
                value = method.invoke(null, new Object[] { string });
            } catch (Exception t) {
                LOGGER.error("valueofError", t);
                value = null;
            }
        } else if (cls.equals(Class.class)) {
            try {
                value = Class.forName(string);
            } catch (ClassNotFoundException e) {
                throw new ConfigException(e);
            }
        } else {
            value = null;
        }
        return (value);
    }

    private static boolean isPrimitiveType(Class<?> cls) {
        if (cls.equals(String.class) || cls.equals(Boolean.TYPE) || cls.equals(Byte.TYPE) || cls.equals(Short.TYPE)
                || cls.equals(Integer.TYPE) || cls.equals(Long.TYPE) || cls.equals(Double.TYPE)
                || cls.equals(Float.TYPE) || cls.equals(Boolean.class) || cls.equals(Byte.class)
                || cls.equals(Short.class) || cls.equals(Integer.class) || cls.equals(Long.class)
                || cls.equals(Float.class) || cls.equals(Double.class) || cls.equals(Class.class)) {
            return true;
        } else {
            return false;
        }
    }

}