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
        String ps = "select patient_salt1 from patient_info where patient_name = ? and patient_idcard = ?";
        ResultSet user = db_util.getQuery(ps, patient_name, patient_idcard);
        String salt;
        if(!user.next()) {
            salt = RandomStringUtils.randomAlphanumeric(4);
            db_util.getQuery("insert into patient_info(patient_name, patient_idcard, patient_salt1, patient_htoken) values(?,?,?,md5(?))", patient_name, patient_idcard, salt,patient_htoken);
        } else {
            salt = user.getString("patient_salt1");
        }

        return new ResponseEntity<String>("{\"nonce\": \""+ nonce +"\", \"salt\":\"" + salt+"\"}", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/sendcode")
    public ResponseEntity<String> sendCode(@RequestBody Map<String, String> in_var) throws SQLException {
        String tele_hash = in_var.get("patient_telephone");
        //verify the tele_hash, if true send the verification.
        //to be done, if you have to verify the hash, you need to rewrite this.
        if(true){
            String vcode = RandomStringUtils.randomAlphanumeric(4);
            //send this code
            return new ResponseEntity<String>(vcode, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<String>("invalid telephone", HttpStatus.NOT_FOUND);
        }
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
    public ResponseEntity<String> insert(@RequestBody Map<String, String> input_data) throws SQLException, JSONException {
        String total_hash = input_data.get("request");
        // this should be modified if you use it
        // you should implement the logic that fetch data from hospital extranet
        if(true){
            String patient_id = "1";
            String record_id = "test";
            String record_date = "10/10/2020";
            String record_content = "test";
            String doctor_name = "test";
            String hospital_name = "test";
            String hospital_department = "test";
            String ps = "insert into health_records values(?, now(), ?, '"+patient_id+ "', ?,?,?)";
            ResultSet rs = db_util.getQuery(ps, record_id, record_content, doctor_name, hospital_name, hospital_department);
            String res_str = db_util.resultSetToJson(db_util.getQuery("select * from health_records where record_id = ?", record_id));
            return new ResponseEntity<String>(res_str, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<String>("not done", HttpStatus.ACCEPTED);
        }
    }


    @PostMapping(path = "/filtereddata")
    public ResponseEntity<String> filteredData(@RequestBody Map<String, String> input_var) throws SQLException, JSONException {
        String patient_id = input_var.get("patient_id");
        String doctor_filter = input_var.get("doctor_filter");
        String department_filter = input_var.get("department_filter");
        String hospital_filter = input_var.get("hospital_filter");
        String date_filter = input_var.get("date_filter");
        String[] date_arr = date_filter.split(" - ");
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
        ps += " and record_date > '" + date_arr[0] + "' and record_date < '" + date_arr[1]+"'";
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

    @PostMapping(path = "/updatepin")
    public ResponseEntity<String> updatepin(@RequestBody Map<String, String> in_var) {
        String patient_idcard = in_var.get("patient_idcard");
        String pin = in_var.get("pin_code");
        String ps = "update patient_info set patient_htoken = md(?) where patient_idcard = ?";
        db_util.getUpdate(ps,pin,patient_idcard);
        return new ResponseEntity<String>("update done", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/deleteinfo")
    public ResponseEntity<String> deleteinfo(@RequestBody Map<String, String> in_var) {
        String record_id = in_var.get("record_id");
        String ps = "delete from health_records where record_id = ?";
        db_util.getUpdate(ps, record_id);
        return new ResponseEntity<String>("delete done", HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/setauth")
    public ResponseEntity<String> setauth(@RequestBody Map<String, String> in_var) {
        String auth_by = in_var.get("auth_by");
        String auth_token = in_var.get("auth_token");
        String auth_records = in_var.get("auth_params");
        String ps = "insert into auth_records(auth_by, auth_date, auth_token, auth_records) values(" + auth_by + ", now(), ?, ?)";
        db_util.getQuery(ps,auth_token,auth_records);
        return new ResponseEntity<String>("auth done", HttpStatus.ACCEPTED);
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


