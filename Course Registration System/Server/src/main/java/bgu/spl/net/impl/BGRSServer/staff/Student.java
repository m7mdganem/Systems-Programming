package bgu.spl.net.impl.BGRSServer.staff;

import bgu.spl.net.api.User;
import bgu.spl.net.impl.BGRSServer.Database;

import java.util.LinkedList;

public class Student implements User {

    private boolean status;
    private final String username;
    private final String password;
    private final LinkedList<Integer> registeredCourses;
    private final Database database;
    private final Object loginLock = new Object();

    public Student(String username,String password){
        this.username = username;
        this.password = password;
        registeredCourses = new LinkedList<>();
        database = Database.getInstance();
        status = false;
    }

    @Override
    public boolean login(String password) {
        synchronized (loginLock) { //To prevent logging into the same profile from two clients
            if (!isLoggedIn() & this.password.equals(password)) {
                status = true;
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean logout() {
        if(isLoggedIn()){
            status = false;
            return true;
        }
        return false;
    }

    public LinkedList<Integer> getRegisteredCourses(){
            return registeredCourses;
    }

    public String getRegisteredCoursesOrdered(){
        synchronized (registeredCourses) {

            LinkedList<Integer> orderdCoursesNumbers = database.getorderedCourses();
            LinkedList<Integer> coursesOutput = new LinkedList<>();

            for(Integer course : orderdCoursesNumbers){
                if (registeredCourses.contains(course)){
                    coursesOutput.add(course);
                }
            }
            //we use replaceAll("\\s","") to remove all redundant spaces that the toString method creates
            return coursesOutput.toString().replaceAll("\\s","");
        }
    }

    public boolean registerToCourse(Integer courseNum){
        /* synchronization is needed for the "registeredCourses" linked-list because
         * there might be two different clients, the student and an admin, where the
         * student is registering to a course and the admin is checking the student-stat
         * and two of these methods are dealing with the same linked-list.
         * There are no two clients who are logged in to the same profile, then there may
         * not be a case where two threads are trying to write to the same linked-list,
         * which means, the contains method will always return the right answer.
         */
        if(!registeredCourses.contains(courseNum)) {
            synchronized (registeredCourses) {
                registeredCourses.add(courseNum);
                return true;
            }
        }
        return false;

    }

    public boolean unregisterToCourse(Integer courseNum){
        synchronized (registeredCourses) {
            return registeredCourses.remove(courseNum);
        }
    }

    public boolean ckeckKdamCourses(LinkedList<Integer> kdamCoursesList){
        /* The only thread that will use the ckeckKdamCourses method is the same thread
         * who is trying to register the student to a course. which means there may not be
         * any modifications on the "registeredCourses" linked-list while iterating over
         * it on this method. thus synchronization is not needed here for "registeredCourses".
         * "kdamCoursesList" is not modified in all the program life-time, thus no need to synchronize.
         */

        for (Integer course : kdamCoursesList) {
            if (!registeredCourses.contains(course)) {
                return false;
            }
        }
        return true;

    }

    @Override
    public boolean isLoggedIn() {
        return status;
    }

    public String getUsername(){return username;}
}
