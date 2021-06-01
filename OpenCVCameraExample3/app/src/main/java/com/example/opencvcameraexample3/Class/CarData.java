package com.example.opencvcameraexample3.Class;

public class CarData {
    String car_no;
    String name;
    String phone;
    String address;
    String in_time;
    String out_time;

    public CarData(String car_no, String name, String phone, String address) {
        this.car_no = car_no;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public CarData(String car_no, String name, String phone, String address, String in_time, String out_time) {
        this.car_no = car_no;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.in_time = in_time;
        this.out_time = out_time;
    }

    public String getCar_no() {
        return this.car_no;
    }

    public String getName() {
        return this.name;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getAddress() {
        return this.address;
    }

    public String getIn_time() { return this.in_time; }

    public String getOut_time() { return this.out_time; }
}
