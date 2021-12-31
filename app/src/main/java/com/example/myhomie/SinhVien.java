package com.example.myhomie;

public class SinhVien {
    public String NHIETDO;
    public String DOAM;
    public String APSUAT;

    public SinhVien() {

        // mac dinh cua firebase de khi nhan du lieu ve can 1 constructor rong
    }

    public SinhVien(String temp, String humid, String presure) {
        NHIETDO = temp;
        DOAM = humid;
        APSUAT = presure;
    }
}
