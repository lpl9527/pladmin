package com.lpl.modules.system.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.service.dto.UserDto;
import org.mapstruct.Mapper;

/**
 * @author lpl
 * 用户mapstruct接口。
 *
 * MapStruct可以将几种类型的对象映射为另一种数据类型，属性：
 *      componentModel：该属性用于指定实现类的类型：
 *          default：默认，不使用任何组建类型，可以通过Mappers.getMapper(Class) 方式获取实例对象。
 *          spring：在实现类上注解 @Component，可通过 @Autowired 方式注入。
 *          jsr330：实现类上添加@javax.inject.Named 和@Singleton注解，可以通过 @Inject注解获取。
 */
@Mapper(componentModel = "spring",
        uses = {
            RoleMapper.class, DeptMapper.class, JobMapper.class
        })
public interface UserMapper extends BaseMapper<UserDto, User> {

}
