package evisionos.plugin.server;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.lang.reflect.Modifier;

public class SpringUtils {

    public static boolean isSpringBeanClass(Class<?> cla){
        if(cla==null){
            return false;
        }
        //是否是接口
        if(cla.isInterface()){
            return false;
        }
        //是否是抽象类
        if( Modifier.isAbstract(cla.getModifiers())){
            return false;
        }
        if(cla.getAnnotation(Component.class)!=null){
            return true;
        }
        if(cla.getAnnotation(Repository.class)!=null){
            return true;
        }
        return cla.getAnnotation(Service.class) != null;
    }
}
