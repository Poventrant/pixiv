package models;

import play.db.jpa.Model;

import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class Pixiv extends Model{
    private Long id;
    private String name;
    private String size;
    private String masterPath;
    private String savePath;
    private Date createTime;
    private Double rate;
    private Boolean isdelete = false;
    private String originHref;
    private String author;
    private String picid;
    private Short picno = 1;        //默认是一张

    public Pixiv(){}
    public Pixiv(Pixiv other) {
        this.name = other.getName();
        this.size = other.getSize();
        this.masterPath = other.getMasterPath();
        this.savePath = other.getSavePath();
        this.createTime = other.getCreateTime();
        this.rate = other.getRate();
        this.isdelete = other.getIsdelete();
        this.originHref = other.getOriginHref();
        this.author = other.getAuthor();
        this.picid = other.getPicid();
        this.picno = other.getPicno();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMasterPath() {
        return masterPath;
    }

    public void setMasterPath(String masterPath) {
        this.masterPath = masterPath;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Boolean isdelete) {
        this.isdelete = isdelete;
    }

    public String getOriginHref() {
        return originHref;
    }

    public void setOriginHref(String originHref) {
        this.originHref = originHref;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getPicid() {
        return picid;
    }

    public void setPicid(String picid) {
        this.picid = picid;
    }

    public Short getPicno() {
        return picno;
    }

    public void setPicno(Short picno) {
        this.picno = picno;
    }
}
