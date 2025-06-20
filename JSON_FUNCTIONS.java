package timetable;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSON_FUNCTIONS {
    private JSONObject jsonData;
    private final String filePath;
    private final JSONParser parser = new JSONParser();

    public JSON_FUNCTIONS() {
        this("C:\\Users\\afsha\\OneDrive\\Desktop\\basetimetablejson.txt");
    }

    public JSON_FUNCTIONS(String filePath) {
        this.filePath = filePath;
        this.jsonData = new JSONObject();
        this.jsonData.put("batches", new JSONObject());
        loadData();
    }

    private void loadData() {
        try (FileReader reader = new FileReader(filePath)) {
            Object parsed = parser.parse(reader);
            if (parsed instanceof JSONObject) {
                this.jsonData = (JSONObject) parsed;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error loading JSON data: " + e.getMessage());
        }
    }

    public void saveData() {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonData.toJSONString());
        } catch (IOException e) {
            System.err.println("Error saving JSON data: " + e.getMessage());
        }
    }

    public List<String> getBatches() {
        List<String> batches = new ArrayList<>();
        JSONObject batchesObj = getBatchesObject();
        if (batchesObj != null) {
            batches.addAll(batchesObj.keySet());
        }
        return batches;
    }


    private JSONObject getBatchesObject() {
        Object batches = jsonData.get("batches");
        return batches instanceof JSONObject ? (JSONObject) batches : null;
    }

    public List<String> getDepartments(String batchId) {
        List<String> departments = new ArrayList<>();
        JSONObject deptsObj = getDepartmentsObject(batchId);
        if (deptsObj != null) {
            departments.addAll(deptsObj.keySet());
        }
        return departments;
    }


    // Section operations
    public List<String> getSections(String batchId, String deptId) {
        List<String> sections = new ArrayList<>();
        JSONArray sectionsArray = getSectionsArray(batchId, deptId);
        if (sectionsArray != null) {
            for (Object section : sectionsArray) {
                if (section instanceof String) {
                    sections.add((String) section);
                }
            }
        }
        return sections;
    }

    // Course operations
    public List<String> getCourses(String batchId, String deptId) {
        List<String> courses = new ArrayList<>();
        JSONArray coursesArray = getCoursesArray(batchId, deptId);
        if (coursesArray != null) {
            for (Object course : coursesArray) {
                if (course instanceof String) {
                    courses.add((String) course);
                }
            }
        }
        return courses;
    }




    private JSONObject getDepartmentsObject(String batchId) {
        JSONObject batchesObj = getBatchesObject();
        if (batchesObj != null && batchesObj.containsKey(batchId)) {
            Object depts = batchesObj.get(batchId);
            if (depts instanceof JSONObject) {
                Object departments = ((JSONObject) depts).get("departments");
                return departments instanceof JSONObject ? (JSONObject) departments : null;
            }
        }
        return null;
    }

    private JSONArray getSectionsArray(String batchId, String deptId) {
        JSONObject deptsObj = getDepartmentsObject(batchId);
        if (deptsObj != null && deptsObj.containsKey(deptId)) {
            Object dept = deptsObj.get(deptId);
            if (dept instanceof JSONObject) {
                Object sections = ((JSONObject) dept).get("sections");
                return sections instanceof JSONArray ? (JSONArray) sections : null;
            }
        }
        return null;
    }

    private JSONArray getCoursesArray(String batchId, String deptId) {
        JSONObject deptsObj = getDepartmentsObject(batchId);
        if (deptsObj != null && deptsObj.containsKey(deptId)) {
            Object dept = deptsObj.get(deptId);
            if (dept instanceof JSONObject) {
                Object courses = ((JSONObject) dept).get("courses");
                return courses instanceof JSONArray ? (JSONArray) courses : null;
            }
        }
        return null;
    }

    // Utility methods
    public String getJsonString() {
        return jsonData.toJSONString();
    }

    public Map<String, List<String>> getAllCoursesByDepartment(String batchId) {
        Map<String, List<String>> result = new HashMap<>();
        for (String dept : getDepartments(batchId)) {
            result.put(dept, getCourses(batchId, dept));
        }
        return result;
    }

    // Add this method to add a new section
    public boolean addSection(String batchId, String deptId, String section) {
        JSONArray sectionsArray = getSectionsArray(batchId, deptId);
        if (sectionsArray != null && !sectionsArray.contains(section)) {
            sectionsArray.add(section);
            saveData();
            return true;
        }
        return false;
    }

    public Map<String, List<String>> getAllSectionsByDepartment(String batchId) {
        Map<String, List<String>> result = new HashMap<>();
        for (String dept : getDepartments(batchId)) {
            result.put(dept, getSections(batchId, dept));
        }
        return result;
    }

    public boolean deleteSection(String batchId, String deptId, String section) {

        // First, check if the section exists in the department
        JSONArray sectionsArray = getSectionsArray(batchId, deptId);
        if (sectionsArray != null && sectionsArray.contains(section)) {
            // Remove the section from the department's sections
            sectionsArray.remove(section);

            // Now, we need to remove the section from each student's timetable in this department
            // We'll go through all the students (or sections in this department) and delete the section
            JSONObject deptsObj = getDepartmentsObject(batchId);
            if (deptsObj != null && deptsObj.containsKey(deptId)) {
                Object dept = deptsObj.get(deptId);
                if (dept instanceof JSONObject) {
                    JSONArray studentsArray = (JSONArray) ((JSONObject) dept).get("sections");
                    if (studentsArray != null) {
                        studentsArray.remove(section);  // Remove section from all enrolled students
                    }
                }
            }

            // Finally, save the changes to the file
            saveData();
            return true;  // Successfully deleted the section
        }
        return false;  // Section not found or could not be deleted
    }


}