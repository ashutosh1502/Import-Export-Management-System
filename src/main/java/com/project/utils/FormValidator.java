package com.project.utils;

public class FormValidator {

    public static boolean validateSupplierId(String supplierId){
        return supplierId == null || supplierId.isEmpty();
    }

    public static boolean validateSupplierName(String supplierName){
        return supplierName == null || supplierName.isEmpty();
    }

    public static boolean validatePhoneNumber(String phoneNumber){
        return phoneNumber==null || phoneNumber.isEmpty() || phoneNumber.matches("^[1-9]\\d{9}$");
    }

    public static boolean validateEmail(String email){
        return email==null || email.isEmpty() || email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
