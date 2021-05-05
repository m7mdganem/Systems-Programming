package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.BGRSServer.staff.Admin;
import bgu.spl.net.impl.BGRSServer.staff.Student;
import bgu.spl.net.api.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {
	private static class DatabaseHolder {
		private static final Database instance = new Database();
	}

	/*
	 * In the {code profiles} field we save user's profiles, such that for each
	 * username we map it to it's appropriate profile.
	 */
	private final ConcurrentHashMap<String, User> profiles = new ConcurrentHashMap<>();
	/*
	 * In the {code courses} field we save courses info, such that for each course number
	 * we map it to a Hash-Map where we save all courses info in the following format:
	 * 1) "courseNum" -> the number of the course
	 * 2) "courseName" -> the name of the course
	 * 3) "kdamCoursesList" -> the kdam course of the course
	 * 4) "numOfMaxStudents" -> the maximum number of students that can register to the course
	 */
	private final ConcurrentHashMap<Integer,ConcurrentHashMap<String,Object>> courses = new ConcurrentHashMap<>();
	/*
	 * In the {code courseRegisteredStudents} field we save the list of registered students to each course.
	 * We map each course number to the appropriate list.
	 */
	private final ConcurrentHashMap<Integer,LinkedList<String>> courseRegisteredStudents = new ConcurrentHashMap<>();
	/*
	 * In the {code numOfSeatsAvailable} field we save number of available seats for each course.
	 * We map each course number to the number of available seats for that course.
	 */
	private final ConcurrentHashMap<Integer,Integer> numOfSeatsAvailable = new ConcurrentHashMap<>();
	/*
	 * In the {code orderedCoursesNumbers} field we save numbers of courses ordered as in the Courses.txt file.
	 */
	private final LinkedList<Integer> orderedCoursesNumbers = new LinkedList<>();


	//to prevent user from creating new Database
	private Database() {
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Database getInstance() {
		return DatabaseHolder.instance;
	}

	/**
	 * Retrieves the profile of the user with username {@code username}
	 * @param username The username of the user we want to get his profile.
	 * @return The profile of the user with username {@code username} if it exists, and null otherwise.
	 */
	public User getProfile(String username){
		if (username != null)
			return profiles.get(username);
		return null;
	}

	/**
	 * Retrieves the info of the course with course number {@code courseNumber}
	 * @param courseNumber The course number of the course we want to get its info
	 * @return {@link ConcurrentHashMap} which holds all the info of the course.
	 */
	public ConcurrentHashMap<String,Object> getCourseInfo(Integer courseNumber){
		return courses.get(courseNumber);
	}

	/**
	 * Register a new {@link Admin} in the database
	 * @param username The username of the admin we want to register
	 * @param password The password of the admin we want to register
	 * @return true if registration passed successfully, and false otherwise
	 */
	public boolean addAdmin(String username,String password){
		return profiles.putIfAbsent(username, new Admin(username,password)) == null;

	}

	/**
	 * Register a new {@link Student} in the database
	 * @param username The username of the student we want to register
	 * @param password The password of the student we want to register
	 * @return true if registration passed successfully, and false otherwise
	 */
	public boolean addStudent(String username,String password){
		return profiles.putIfAbsent(username, new Student(username,password)) == null;
	}

	/**
	 * Retrieves if the course with the course number {@code courseNum} exists in the database.
	 * @param courseNum The course number of the course we want to check if exists
	 * @return True iff the course with the course number {@code courseNum} exists
	 */
	public boolean courseExistes(short courseNum){
		return courses.containsKey((int) courseNum);
	}

	/**
	 * Retrieves the number of available seats for the course with course number {@code courseNum}.
	 * @param courseNum The course number of the course we want to check
	 * @return The number of available seats for that course
	 */
	public Integer getNumOfSeatsAvailable(Integer courseNum){
		return numOfSeatsAvailable.get(courseNum);
	}

	/**
	 * Retrieves a string representing the list of registered students to the course with course number {@code courseNum}
	 * @param courseNum The course number of the course we want to check
	 * @return A string representing the list of registered students, ordered alphabetically
	 */
	public String getRegisteredStudentsOrderedString(short courseNum){
		/*
		 * The sort method modifies the list, and the toString method iterates over it,
		 * thus there may be other students who are trying to register to the same course,
		 * and registering to course also modifies the list, thus we must lock the list until
		 * we finish.
		 */

		LinkedList<String> registeredStudents = courseRegisteredStudents.get((int)courseNum);
		Collections.sort(registeredStudents);//sort the list alphabetically
		//we use replaceAll("\\s","") to remove all redundant spaces that the toString method creates
		return registeredStudents.toString().replaceAll("\\s","");

	}

	/**
	 * Retrieves the list of courses existing in the database ordered as in the Courses.txt file
	 * @return {@link LinkedList<Integer>} contains numbers of courses ordered as in the Courses.txt file
	 */
	public LinkedList<Integer> getorderedCourses(){
		return orderedCoursesNumbers;
	}

	/**
	 * Unregister the student with the username {@code username} from the course with course number {@code courseNum}
	 * @param courseNum The number of the course we want to unregister from
	 * @param username The username of the student we want to unregister
	 * @return True iff the unregistration passed successfully
	 */
	public boolean unregisterToCourse(Integer courseNum,String username){

		synchronized (courseRegisteredStudents.get(courseNum)) {
			synchronized (numOfSeatsAvailable.get(courseNum)) {
				/*
				 * if the students succeeds to unregister to the course, then continue
				 * in the unregistration procedure. this if-statement will fail only if the
				 * student is not registered to the course, and thus we will not continue.
				 *
				 */
				if (((Student) profiles.get(username)).unregisterToCourse(courseNum)) {
						courseRegisteredStudents.get(courseNum).remove(username);
						//decrease the number of available students in the course
						numOfSeatsAvailable.replace(courseNum, numOfSeatsAvailable.get(courseNum) + 1);
						return true;
				}
				return false;
			}
		}
	}

	/**
	 * Register the student with the username {@code username} to the course with course number {@code courseNum}
	 * @param courseNum The number of the course we want to register to
	 * @param username The username of the student we want to register
	 * @return True iff the registration passed successfully
	 */
	public boolean registerToCourse(Integer courseNum,String username){

		synchronized (courseRegisteredStudents.get(courseNum)) {
			synchronized (numOfSeatsAvailable.get(courseNum)) {
				/*
				 * if there are available seats and the student succeeds to register to the course,
				 * then continue in the registration procedure. this if-statement will fail only if the
				 * student is already registered to the course, and thus we will not continue
				 */
				Student student = (Student) profiles.get(username);
				LinkedList<Integer> kdam = (LinkedList<Integer>) courses.get(courseNum).get("kdamCoursesList");
				if (numOfSeatsAvailable.get(courseNum) > 0 && student.ckeckKdamCourses(kdam) && student.registerToCourse(courseNum)) {
					//add the student with the username to the list of registered students
					courseRegisteredStudents.get(courseNum).add(username);
					//decrease the number of available students in the course
					numOfSeatsAvailable.replace(courseNum, numOfSeatsAvailable.get(courseNum) - 1);
					return true;
				}
				return false;
			}
		}
	}

	/**
	 * loades the courses from the file path specified 
	 * into the Database, returns true if successful.
	 */
	boolean initialize(String coursesFilePath) {
			File myObj = new File(coursesFilePath);
			try(Scanner scanner = new Scanner(myObj)) {
				while (scanner.hasNextLine()) {
					String data = scanner.nextLine();
					ConcurrentHashMap<String, Object> courseInfo = extractCourseInfo(data);
					courses.put((Integer) courseInfo.get("courseNum"), courseInfo);
				}
				return true;
			}catch (FileNotFoundException e){
				e.printStackTrace();
			}
		return false;
	}

	/**
	 * Retrieves a {@link ConcurrentHashMap} containing all the information in the {@link String} {@code info}
	 * @param info A {@link String} containing all the required information for the course
	 * @return A {@link ConcurrentHashMap} containing all the information of the course
	 */
	private ConcurrentHashMap<String,Object> extractCourseInfo(String info){
		String[] courseInfo = info.split("\\|");
		Integer courseNum = Integer.parseInt(courseInfo[0]);//get course number
		String courseName = courseInfo[1];//get course name
		Integer courseCapacity = Integer.parseInt(courseInfo[3]);//get number of max students in the course
		LinkedList<Integer> kdamCourses = new LinkedList<>();
		if(courseInfo[2].length()>2) {
			//get the numbers of the kdam courses as strings array
			String[] KdamStrings = (courseInfo[2].substring(1, courseInfo[2].length() - 1)).split(",");
			for (String kdam : KdamStrings) {
				Integer num = Integer.parseInt(kdam);
				kdamCourses.add(num);
			}
		}
		ConcurrentHashMap<String,Object> courseInfoOutput = new ConcurrentHashMap<>();
		courseInfoOutput.put("courseNum",courseNum);
		courseInfoOutput.put("courseName",courseName);
		courseInfoOutput.put("kdamCoursesList",kdamCourses);
		courseInfoOutput.put("numOfMaxStudents",courseCapacity);
		numOfSeatsAvailable.putIfAbsent(courseNum,courseCapacity);
		orderedCoursesNumbers.add(courseNum); //this list will hold the courses order the same as the file
		//this new empty list will hold the students registered to the course
		courseRegisteredStudents.putIfAbsent(courseNum, new LinkedList<>());

		return courseInfoOutput;
	}

}
