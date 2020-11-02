package com.bezkoder.spring.datajpa.controller;


import com.bezkoder.spring.datajpa.DatabaseUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.Query;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api")
public class OtherBackendController {
    DatabaseUtil db_util = new DatabaseUtil();
    @PostMapping(path = "/sendnonce")
    public ResponseEntity<String> sendNonce(@RequestBody Map<String, String> in_var) throws SQLException {
        String patient_name = in_var.get("patient_name");
        String patient_idcard = in_var.get("patient_idcard");
        String nonce = RandomStringUtils.randomAlphanumeric(8);
        String patient_htoken = in_var.get("token");
        //send this nonce
        String ps = "select * from patient_info where patient_name = ? and patient_idcard = ?";
        ResultSet user = db_util.getQuery(ps, patient_name, patient_idcard);
        if(!user.next()) {
            db_util.getQuery("insert into patient_info(patient_name, patient_idcard, patient_salt1, patient_htoken) values(?,?,?,md5(?))", patient_name, patient_idcard, RandomStringUtils.randomAlphanumeric(4),patient_htoken);
        }
        return new ResponseEntity<String>(nonce, HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/sendcode")
    public ResponseEntity<String> sendCode(@RequestBody Map<String, String> in_var) throws SQLException {
        String tele_hash = in_var.get("patient_telephone");
        String vcode = RandomStringUtils.randomAlphanumeric(4);
        //send this code
        return new ResponseEntity<String>(vcode, HttpStatus.ACCEPTED);
    }


    @PostMapping(path = "/login")
    public ResponseEntity<String> verifyInfo(@RequestBody Map<String, String> input_data) throws SQLException {
        String patient_name = input_data.get("patient_name");
        //String patient_telephone = input_data.get("patient_telephone");
        String patient_token = input_data.get("patient_token");
        String patient_idcard = input_data.get("patient_idcard");
        String ps = "SELECT patient_id FROM patient_info where md5('" + patient_token + "') = patient_htoken and patient_name = ? and patient_idcard = ?";
        ResultSet patient_id = db_util.getQuery(ps, patient_name, patient_idcard);
        if(patient_id.next()){
            return new ResponseEntity<String>(patient_id.getString("patient_id"), HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/insert")
    public ResponseEntity<String> insert(@RequestBody Map<String, String> input_data) throws SQLException {
        String patient_id = input_data.get("patient_id");
        String record_id = input_data.get("record_id");
        String record_date = input_data.get("record_date");
        String record_content = input_data.get("record_content");
        String doctor_name = input_data.get("doctor_name");
        String hospital_name = input_data.get("hospital_name");
        String hospital_department = input_data.get("hospital_department");
        String ps = "insert into health_records values(?, ?, ?, ?, ?,?,?)";
        ResultSet rs = db_util.getQuery(ps, record_id, record_date, record_content, patient_id, doctor_name, hospital_name, hospital_department);
        return new ResponseEntity<String>("done", HttpStatus.ACCEPTED);
    }


    @PostMapping(path = "/filtereddata")
    public ResponseEntity<String> filteredData(@RequestBody Map<String, String> input_var) throws SQLException, JSONException {
        String patient_id = input_var.get("patient_id");
        String doctor_filter = input_var.get("doctor_filter");
        String department_filter = input_var.get("department_filter");
        String hospital_filter = input_var.get("hospital_filter");
        String ps = "select * from health_records where patient_id = '" + patient_id + "'";
        if(!doctor_filter.equals("all_doctors")){
            ps += " and doctor_name = '" + doctor_filter + "'";
        }
        if(!department_filter.equals("all_departments")){
            ps += " and hospital_department = '"+ department_filter +"'";
        }
        if(!hospital_filter.equals("all_hospitals")){
            ps += " and hospital_name = '" +hospital_filter + "'";
        }
        ResultSet rs = db_util.getQuery(ps);
        return new ResponseEntity<String>(db_util.resultSetToJson(rs), HttpStatus.ACCEPTED);
    }


    @PostMapping(path = "/updateinfo")
    public ResponseEntity<String> updateinfo(@RequestBody Map<String, String> in_var) {
        String old_patient_idcard = in_var.get("patient_idcard");
        String new_patient_idcard = in_var.get("patient_idcard_new");
        String ps = "update patient_info set patient_idcard = ? where patient_idcard = ?";
        db_util.getUpdate(ps,new_patient_idcard,old_patient_idcard);
        return new ResponseEntity<String>("update done", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/deleteinfo")
    public ResponseEntity<String> deleteinfo(@RequestBody Map<String, String> in_var) {
        String record_id = in_var.get("record_id");
        String ps = "delete from health_records where record_id = ?";
        db_util.getUpdate(ps, record_id);
        return new ResponseEntity<String>("delete done", HttpStatus.ACCEPTED);
    }


    @PostMapping(path = "/authorize")
    public ResponseEntity<String> authorize(@RequestBody Map<String, String> rights) {
        String patient_tel = rights.get("patient_tel");
        String doctor_tel = rights.get("doctor_tel");
        String report_id = rights.get("report_id");
        db_util.setRights(patient_tel,doctor_tel,report_id);
        return new ResponseEntity<String>("authorize done", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/unauthorize")
    public ResponseEntity<String> unauthorize(@RequestBody Map<String, String> rights) {
        String doctor_tel = rights.get("doctor_tel");
        db_util.unsetRights(doctor_tel);
        return new ResponseEntity<String>("unauthorize done", HttpStatus.ACCEPTED);
    }



    @GetMapping(path = "/getverifycode")
    public ResponseEntity<String> getVerifycode() {
        String verifycode = "0000";
        return new ResponseEntity<String>("verify code is sent...", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/getauthorize")
    public ResponseEntity<String> getauthorize(@RequestBody Map<String, String> rights) {
        String patient_tel = rights.get("patient_tel");
        String res = db_util.getAuth(patient_tel);
        return new ResponseEntity<String>(res, HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/update")
    public ResponseEntity<String> updateInfo(@RequestBody Map<String, String> personalinfo) {
        String oldtelephone = personalinfo.get("old_telephone");
        String oldtoken = personalinfo.get("old_token");
        String newtelephone = personalinfo.get("new_telephone");
        String newtoken = personalinfo.get("new_token");
        db_util.updateinfo(newtelephone,oldtelephone,newtoken,oldtoken);
        db_util.updateinfo2(newtelephone,oldtelephone);
        return new ResponseEntity<String>("done",HttpStatus.ACCEPTED);
    }
}


