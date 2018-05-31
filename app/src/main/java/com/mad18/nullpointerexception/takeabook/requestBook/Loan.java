package com.mad18.nullpointerexception.takeabook.requestBook;

import java.util.Date;

class Loan {

    String loanId;
    String ownerId;
    String applicantId;
    String ownerName;
    String applicantName;
    String bookTitle;
    String bookThumbnail;
    String bookId;
    String requestText;
    Boolean requestStatus; //A
    Boolean exchangedApplicant; //B
    Boolean exchangedOwner; //C
    Date startDate;
    Date endLoanOwner;
    Date endLoanApplicant;

    Loan() {}

    Loan(String ownerId, String applicantId, String ownerName, String applicantName,
         String bookTitle, String bookThumbnail, String bookId,
         String requestText, Date startDate, String loanId) {
        this.ownerId = ownerId;
        this.applicantId = applicantId;
        this.ownerName = ownerName;
        this.applicantName = applicantName;
        this.bookTitle = bookTitle;
        this.bookThumbnail = bookThumbnail;
        this.bookId = bookId;
        this.requestText = requestText;
        this.startDate = startDate;
        this.loanId = loanId;
        this.requestStatus = false;
        this.exchangedApplicant = false;
        this.exchangedOwner = false;
        this.endLoanApplicant = null;
        this.endLoanOwner = null;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookThumbnail() {
        return bookThumbnail;
    }

    public void setBookThumbnail(String bookThumbnail) {
        this.bookThumbnail = bookThumbnail;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public Boolean getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Boolean requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Boolean getExchangedApplicant() {
        return exchangedApplicant;
    }

    public void setExchangedApplicant(Boolean exchangedApplicant) {
        this.exchangedApplicant = exchangedApplicant;
    }

    public Boolean getExchangedOwner() {
        return exchangedOwner;
    }

    public void setExchangedOwner(Boolean exchangedOwner) {
        this.exchangedOwner = exchangedOwner;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndLoanOwner() {
        return endLoanOwner;
    }

    public void setEndLoanOwner(Date endLoanOwner) {
        this.endLoanOwner = endLoanOwner;
    }

    public Date getEndLoanApplicant() {
        return endLoanApplicant;
    }

    public void setEndLoanApplicant(Date endLoanApplicant) {
        this.endLoanApplicant = endLoanApplicant;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }
}
