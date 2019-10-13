package com.udayasreesoft.businesslibrary.models;

public class PaymentModelMain {
    private String uniqueKey;
    private PaymentModel paymentModel;

    public PaymentModelMain() {
    }

    public PaymentModelMain(String uniqueKey, PaymentModel paymentModel) {
        this.uniqueKey = uniqueKey;
        this.paymentModel = paymentModel;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public PaymentModel getPaymentModel() {
        return paymentModel;
    }

    public void setPaymentModel(PaymentModel paymentModel) {
        this.paymentModel = paymentModel;
    }
}
