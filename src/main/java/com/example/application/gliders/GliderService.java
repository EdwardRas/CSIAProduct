package com.example.application.gliders;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GliderService {
    private final DataSource dataSource;
    @Autowired
    public GliderService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
//
//    private final GliderRepository gliderRepository;
//
//    GliderService(GliderRepository gliderRepository) {
//        this.gliderRepository = gliderRepository;
//    }
//
//    @Transactional
//    public void createTask(String description, @Nullable LocalDate dueDate) {
//        var task = new Glider(description, Instant.now());
//        task.setDueDate(dueDate);
//        gliderRepository.saveAndFlush(task);
//    }
//
//    @Transactional(readOnly = true)
//    public List<Glider> list(Pageable pageable) {
//        return gliderRepository.findAllBy(pageable).toList();
//    }
public List<Glider> getAllGliders(){
    List<Glider> gliders = new ArrayList<>();
    try(
            Connection conn = dataSource.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM gliders");
    ){
        while(rs.next()){
            Glider glider = new Glider();
            glider.setId(rs.getLong("id"));
            glider.setRegistrationNumber(rs.getString("reg_number"));
            //etc.
            gliders.add(glider);
        }
    }
    catch(Exception e){
        System.out.println("Exception: " + e.getMessage());
    }
    return gliders;
}
}
