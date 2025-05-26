package p8z9.jtuat.tmc.cfm.common;

import org.apache.commons.lang3.StringUtils;

public class BatchDownloadFileDto {
    private String url;
    private String fileName;
    private String billNumber;
    private String billPkId;
    private String fileType;
    private String contractName;//  合同名称
    private String contractNo;//    合同编号
    private long fileSize;

    public BatchDownloadFileDto() {
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBillNumber() {
        return this.billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getBillPkId() {
        return this.billPkId;
    }

    public void setBillPkId(String billPkId) {
        this.billPkId = billPkId;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getDirName() {
        return StringUtils.isBlank(this.billNumber) ? String.valueOf(this.billPkId) : String.format("%s_%s", this.billNumber, this.billPkId);
    }
}
