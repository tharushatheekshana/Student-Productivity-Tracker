/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc;

import com.tracker.mvc.form.Welcome;
import com.tracker.mvc.view.DashboardView;

/**
 *
 * @author Theekshana
 */
public class Main {

    public static void main(String[] args) {
        DashboardView dashboard = new DashboardView();
//        dashboard.setVisible(true);
//        new TimetableView().setVisible(true);
//        new TaskView().setVisible(true);
//        dashboard.setResizable(false);
        new Welcome().setVisible(true);
    }
}
