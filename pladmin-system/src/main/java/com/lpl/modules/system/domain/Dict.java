package com.lpl.modules.system.domain;

import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author lpl
 * 数据字典实体类
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dict")
public class Dict extends BaseEntity {

    @Id
    @Column(name = "dict_id")
    @NotNull(groups = Update.class)
    @ApiModelProperty(value = "字典ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "dict", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<DictDetail> dictDetails;

    @NotBlank
    @ApiModelProperty(value = "字典名称")
    private String name;

    @ApiModelProperty(value = "字典描述")
    private String description;
}
