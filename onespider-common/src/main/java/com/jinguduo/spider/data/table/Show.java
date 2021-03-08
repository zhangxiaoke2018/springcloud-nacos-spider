package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jinguduo.spider.cluster.model.Job;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;

@Entity
@Table(name = "shows", indexes = {@Index(name = "code_idx",  columnList="code", unique = false)})
@Data
public class Show implements Serializable {

	private static final long serialVersionUID = 6124394024887369619L;

	@Id
	@GeneratedValue
	private Integer id;
	@Column(nullable = false)
	private Integer parentId = 0;
	@Column(nullable = false)
	private Integer depth = 1;
	@Column(nullable = false)
	private Boolean deleted = false;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date releaseDate = null;
	private Date offlineDate = null;
	@Column(length = 64, nullable = false)
	@NotBlank
	private String name;
	@Column(length = 100)
	private String code;
	private Integer platformId;
	@Column(length = 64)
	private String category;
	private Integer linkedId;
	private Integer episode;

	@Column
	private Integer checkedStatus = 0;  // 审核状态: init:0  passed:1  ignored:2
	@Column
	private Boolean onBillboard = false;

	@Column(updatable = false)
	@CreatedDate
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
	@Column(updatable = false)
	@LastModifiedDate
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
	
	private String url;

	private String parentCode;

	public Show() {
	}

	public Show(String name, String code, Integer platformId,Integer parentId) {
		this.parentId = parentId;
		this.name = name;
		this.code = code;
		this.platformId = platformId;
	}

	@Transient
	private List<Job> jobs;
	@Transient
	private String platformCn;

	public Show(Integer id, Integer parentId, Integer depth, Boolean deleted, Date releaseDate, Date offlineDate, String name, String code, Integer platformId, String category, Integer linkedId, Integer episode, Integer checkedStatus, Boolean onBillboard, Timestamp createdAt, Timestamp updatedAt, String url, String parentCode, List<Job> jobs, String platformCn, Integer source) {
		this.parentId = parentId;
		this.name = name;
		this.code = code;
		this.platformId = platformId;
		this.depth = depth;
		this.deleted = deleted;
		this.releaseDate = releaseDate;
		this.offlineDate = offlineDate;
		this.platformId = platformId;
		this.name = name;
		this.category = category;
		this.linkedId = linkedId;
		this.episode = episode;
		this.episode = episode;
		this.onBillboard = onBillboard;
		this.checkedStatus = checkedStatus;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.url = url;
		this.jobs = jobs;
		this.parentCode = parentCode;
		this.source = source;
		this.platformCn = platformCn;
	}

	@Transient
	public Boolean isCheckInit() {
		return checkedStatus == 0;
	}

	@Transient
	public Boolean isCheckPassed() {
		return checkedStatus == 1;
	}

	@Transient
	public void checkPassed(){
		this.checkedStatus = 1;
	}

	@Transient
	public Boolean isCheckIgnored() {
		return checkedStatus == 2;
	}

	public void checkIgnored(){
		this.checkedStatus = 2;
	}

	/** 来源：  1: dashboard;  2: spider  3:autofind*/
	@Transient
	private Integer source = 0;
	@Transient
	public boolean isFromDashboard() {
	    return source == 1;
	}
	@Transient
    public boolean isFromAutoFind() {
        return source == 3;
    }

	public static ShowBuilder builder(){
		return new ShowBuilder();
	}

	@EqualsAndHashCode
	public static class ShowBuilder {
		private Integer id;
		private Integer parentId;
		private Integer depth;
		private Boolean deleted;
		private Date releaseDate;
		private Date offlineDate;
		private String name;
		private String code;
		private Integer platformId;
		private String category;
		private Integer linkedId;
		private Integer episode;
		private Integer checkedStatus;
		private Boolean onBillboard;
		private Timestamp createdAt;
		private Timestamp updatedAt;
		private String url;
		private String parentCode;
		private List<Job> jobs;
		private String platformCn;
		private Integer source;

		ShowBuilder() {
		}

		public Show.ShowBuilder id(Integer id) {
			this.id = id;
			return this;
		}

		public Show.ShowBuilder parentId(Integer parentId) {
			this.parentId = parentId;
			return this;
		}

		public Show.ShowBuilder depth(Integer depth) {
			this.depth = depth;
			return this;
		}

		public Show.ShowBuilder deleted(Boolean deleted) {
			this.deleted = deleted;
			return this;
		}

		public Show.ShowBuilder releaseDate(Date releaseDate) {
			this.releaseDate = releaseDate;
			return this;
		}

		public Show.ShowBuilder offlineDate(Date offlineDate) {
			this.offlineDate = offlineDate;
			return this;
		}

		public Show.ShowBuilder name(String name) {
			this.name = name;
			return this;
		}

		public Show.ShowBuilder code(String code) {
			this.code = code;
			return this;
		}

		public Show.ShowBuilder platformId(Integer platformId) {
			this.platformId = platformId;
			return this;
		}

		public Show.ShowBuilder category(String category) {
			this.category = category;
			return this;
		}

		public Show.ShowBuilder linkedId(Integer linkedId) {
			this.linkedId = linkedId;
			return this;
		}

		public Show.ShowBuilder episode(Integer episode) {
			this.episode = episode;
			return this;
		}

		public Show.ShowBuilder checkedStatus(Integer checkedStatus) {
			this.checkedStatus = checkedStatus;
			return this;
		}

		public Show.ShowBuilder onBillboard(Boolean onBillboard) {
			this.onBillboard = onBillboard;
			return this;
		}

		public Show.ShowBuilder createdAt(Timestamp createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Show.ShowBuilder updatedAt(Timestamp updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public Show.ShowBuilder url(String url) {
			this.url = url;
			return this;
		}

		public Show.ShowBuilder parentCode(String parentCode) {
			this.parentCode = parentCode;
			return this;
		}

		public Show.ShowBuilder jobs(List<Job> jobs) {
			this.jobs = jobs;
			return this;
		}

		public Show.ShowBuilder platformCn(String platformCn) {
			this.platformCn = platformCn;
			return this;
		}

		public Show.ShowBuilder source(Integer source) {
			this.source = source;
			return this;
		}

		public Show build() {
			return new Show(this.id, this.parentId, this.depth, this.deleted, this.releaseDate, this.offlineDate, this.name, this.code, this.platformId, this.category, this.linkedId, this.episode, this.checkedStatus, this.onBillboard, this.createdAt, this.updatedAt, this.url, this.parentCode, this.jobs, this.platformCn, this.source);
		}

		public String toString() {
			return "Show.ShowBuilder(id=" + this.id + ", parentId=" + this.parentId + ", depth=" + this.depth + ", deleted=" + this.deleted + ", releaseDate=" + this.releaseDate + ", offlineDate=" + this.offlineDate + ", name=" + this.name + ", code=" + this.code + ", platformId=" + this.platformId + ", category=" + this.category + ", linkedId=" + this.linkedId + ", episode=" + this.episode + ", checkedStatus=" + this.checkedStatus + ", onBillboard=" + this.onBillboard + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", url=" + this.url + ", parentCode=" + this.parentCode + ", jobs=" + this.jobs + ", platformCn=" + this.platformCn + ", source=" + this.source + ")";
		}
	}
}