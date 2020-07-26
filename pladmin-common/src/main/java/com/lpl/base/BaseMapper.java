package com.lpl.base;

import java.util.List;

/**
 * @author lpl
 * 基础MapStruct映射
 */
public interface BaseMapper<D, E> {

    /**
     * Dto转Entity
     * @param dto
     */
    E toEntity(D dto);

    /**
     * Entity转Dto
     * @param entity
     */
    D toDto(E entity);

    /**
     * Dto集合转Entity集合
     * @param dtoList
     */
    List<E> toEntity(List<D> dtoList);

    /**
     * Entity集合转Dto集合
     * @param entityList
     */
    List<D> toDto(List<E> entityList);
}
