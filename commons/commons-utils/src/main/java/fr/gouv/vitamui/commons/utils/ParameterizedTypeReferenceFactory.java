package fr.gouv.vitamui.commons.utils;

import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeReferenceFactory {

    /**
     * create ParameterizedTypeReference from two types
     * Ex: ResultDto<UserDto> => mainType: ResultDto, parameterType: UserDto
     * you can use multi parameter type
     * @param mainType the main type
     * @param parameterType the parameter type
     * @return
     */
    public static ParameterizedTypeReference create(Type mainType, Type ...parameterType) {
        Type resultDtoType = new ParameterizedType() {
            public Type getRawType() {
                return mainType;
            }

            public Type getOwnerType() {
                return null;
            }

            public Type[] getActualTypeArguments() {
                return parameterType;
            }
        };
        return ParameterizedTypeReference.forType(resultDtoType);
    }

    /**
     * create ParameterizedTypeReference from a type and an instance
     * Ex: ResultDto<D> => mainType: ResultDto, parentInstance: BaseItem...<D>
     * the parameterType is deduced from the first parameter type "D" of the instance
     * @param mainType the main type
     * @param parentInstance the instance from witch ll be deducted the parameter type
     * @return
     */
    public static ParameterizedTypeReference createFromInstance(Type mainType, Object parentInstance) {
        Type paramType = ReflectionUtils.getParametrizedClass(parentInstance.getClass().getGenericSuperclass());
        return create(mainType, paramType);
    }
}
