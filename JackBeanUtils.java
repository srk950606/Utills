package com.jack.utils;

import com.esotericsoftware.reflectasm.MethodAccess;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class JackBeanUtils {
    private static Map<Class, MethodAccess> methodMap = new HashMap<Class, MethodAccess>();
    private static Map<String, Integer> methodIndexMap = new HashMap<String, Integer>();
    private static Map<Class, List<String>> fieldMap = new HashMap<Class, List<String>>();

    public static void copyProperties(Object destination, Object origin) {
        if (Objects.isNull(destination) || Objects.isNull(origin)) {

        }
        MethodAccess destinationMethodAccess = methodMap.get(destination.getClass());
        if (destinationMethodAccess == null) {
            destinationMethodAccess = cache(destination);
        }
        MethodAccess orginMethodAccess = methodMap.get(origin.getClass());
        if (orginMethodAccess == null) {
            orginMethodAccess = cache(origin);
        }
        List<String> fieldList = fieldMap.get(origin.getClass());
        for (String field : fieldList) {
            String getKey = origin.getClass().getName() + "." + "get" + field;
            String setkey = destination.getClass().getName() + "." + "set" + field;
            Integer setIndex = methodIndexMap.get(setkey);
            if (setIndex != null) {
                int getIndex = methodIndexMap.get(getKey);
                // 参数一需要反射的对象
                // 参数二class.getDeclaredMethods 对应方法的index
                // 参数对三象集合
                destinationMethodAccess.invoke(destination, setIndex.intValue(),
                        orginMethodAccess.invoke(origin, getIndex));
            }
        }
    }

    private static MethodAccess cache(Object origin) {
        synchronized (origin.getClass()) {
            MethodAccess methodAccess = MethodAccess.get(origin.getClass());


            Class classzz = origin.getClass();
            List<Field> fieldList = new ArrayList<>();
            fieldList.addAll(Arrays.asList(classzz.getDeclaredFields()));
            while (classzz.getSuperclass() != Object.class) {
                fieldList.addAll(Arrays.asList(classzz.getSuperclass().getDeclaredFields()));
                classzz = classzz.getSuperclass();
            }


            List<String> filedNameList = new ArrayList<String>(fieldList.size());
            for (Field field : fieldList) {
                //非私有，非静态
                if (Modifier.isPrivate(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                    String fieldName = StringUtils.capitalize(field.getName());
                    int getIndex = methodAccess.getIndex("get" + fieldName);
                    int setIndex = methodAccess.getIndex("set" + fieldName);
                    methodIndexMap.put(origin.getClass().getName() + "." + "get" + fieldName, getIndex);// 将类名get方法名，方法下标注册到map中
                    methodIndexMap.put(origin.getClass().getName() + "." + "set" + fieldName, setIndex);// 将类名set方法名，方法下标注册到map中
                    filedNameList.add(fieldName);// 将属性名称放入集合里
                }
            }
            fieldMap.put(origin.getClass(), filedNameList); // 将类名，属性名称注册到map中
            methodMap.put(origin.getClass(), methodAccess);
            return methodAccess;
        }

    }
}
