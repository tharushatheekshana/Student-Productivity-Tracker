/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.controller;

import com.tracker.mvc.dto.TimetableDto;
import com.tracker.mvc.model.TimetableModel;
import java.util.ArrayList;

/**
 *
 * @author Theekshana
 */
public class TimetableController {

    private TimetableModel timetableModel;

    public TimetableController() throws Exception {
        timetableModel = new TimetableModel();
    }

    public String saveSubject(TimetableDto dto) throws Exception {
        return timetableModel.saveSubject(dto);
    }

    public ArrayList<TimetableDto> getAllSubjects() throws Exception {
        return timetableModel.getAllSubjects();
    }

    public TimetableDto searchSubject(int id) throws Exception {
        return timetableModel.searchSubject(id);
    }

    public String updateSubject(TimetableDto dto) throws Exception {
        return timetableModel.updateSubject(dto);
    }

    public String deleteSubject(int id) throws Exception {
        return timetableModel.deleteSubject(id);
    }
}
