package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table
@Data
public class Pulse implements Serializable {

	private static final long serialVersionUID = -1534823849260399935L;

	@Id
	@GeneratedValue
	private Integer id;

	@NotNull
	private Integer frequency ;

	@NotBlank
	private String url;

	@Column(updatable = false)
	@CreatedDate
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

	@Column(updatable = false)
	@LastModifiedDate
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

}
