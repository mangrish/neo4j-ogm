/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.multidrivertest;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.neo4j.ogm.domain.education.Course;
import org.neo4j.ogm.domain.education.Student;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.StubDriver;

/**
 * @author Vince Bickers
 */
public class EducationTest {

	private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.education");
	private static final Neo4jSession session = (Neo4jSession) sessionFactory.openSession();

	@Test
	public void testTeachers() throws Exception {

		Map<String, Teacher> teachers = loadTeachers();

		Teacher mrThomas = teachers.get("Mr Thomas");
		Teacher mrsRoberts = teachers.get("Mrs Roberts");
		Teacher missYoung = teachers.get("Miss Young");

		checkCourseNames(mrThomas, "Maths", "English", "Physics");
		checkCourseNames(mrsRoberts, "English", "History", "PE");
		checkCourseNames(missYoung, "History", "Geography", "Philosophy and Ethics");
	}

	@Test
	public void testFetchCoursesTaughtByAllTeachers() throws Exception {

		Map<String, Teacher> teachers = loadTeachers();  // note: idempotent!

		// this response is for an imagined request: "match p = (c:COURSE)--(o) where id(c) in [....] RETURN p"
		// i.e. we have a set of partially loaded courses attached to our teachers which we now want to
		// hydrate by getting all their relationships
		hydrateCourses(teachers.values());

		Set<Course> courses = new HashSet<>();
		for (Teacher teacher : teachers.values()) {
			for (Course course : teacher.getCourses()) {
				if (!courses.contains(course)) {
					List<Student> students = course.getStudents();
					if (course.getName().equals("Maths")) checkMaths(students);
					else if (course.getName().equals("Physics")) checkPhysics(students);
					else if (course.getName().equals("Philosophy and Ethics")) checkPhilosophyAndEthics(students);
					else if (course.getName().equals("PE")) checkPE(students);
					else if (course.getName().equals("History")) checkHistory(students);
					else if (course.getName().equals("Geography")) checkGeography(students);
					else checkEnglish(students);
					courses.add(course);
				}
			}
		}
		assertEquals(7, courses.size());
	}

	private void test(long hash, List<Student> students) {
		for (Student student : students) {
			hash -= student.getId();
		}
		assertEquals(0, hash);
	}

	// all students study english
	private void checkEnglish(List<Student> students) {
		long hash = 0;
		for (int i = 101; i < 127; i++) {
			hash += i;
		}
		test(hash, students);
	}


	// all students whose ids modulo 100 are prime study geography. 1 is not considered prime
	private void checkGeography(List<Student> students) {
		long hash = 102 + 103 + 105 + 107 + 111 + 113 + 117 + 119 + 123;

		test(hash, students);
	}

	// all students with even ids study history
	private void checkHistory(List<Student> students) {
		long hash = 0;
		for (int i = 102; i < 127; i += 2) {
			hash += i;
		}
		test(hash, students);
	}

	// every 3rd student studies PE
	private void checkPE(List<Student> students) {
		long hash = 0;
		for (int i = 103; i < 127; i += 3) {
			hash += i;
		}
		test(hash, students);
	}

	// all students are deep thinkers
	private void checkPhilosophyAndEthics(List<Student> students) {
		long hash = 0;
		for (int i = 101; i < 127; i++) {
			hash += i;
		}
		test(hash, students);
	}

	// all students with odd ids study physics
	private void checkPhysics(List<Student> students) {
		long hash = 0;
		for (int i = 101; i < 127; i += 2) {
			hash += i;
		}
		test(hash, students);
	}

	// all students study maths
	private void checkMaths(List<Student> students) {
		long hash = 0;
		for (int i = 101; i < 127; i++) {
			hash += i;
		}
		test(hash, students);
	}

	private Map<String, Teacher> loadTeachers() throws Exception {

		Map<String, Teacher> teachers = new HashMap<>();

		session.setDriver(new TeacherRequest());
		Collection<Teacher> teacherList = session.loadAll(Teacher.class);

		for (Teacher teacher : teacherList) {
			teachers.put(teacher.getName(), teacher);
		}

		return teachers;
	}

	private void hydrateCourses(Collection<Teacher> teachers) throws Exception {

		session.setDriver(new TeacherRequest());
		session.setDriver(new CoursesRequest());

		session.loadAll(Course.class);
	}

	private void checkCourseNames(Teacher teacher, String... courseNames) {

		int n = courseNames.length;
		List<String> test = Arrays.asList(courseNames);

		System.out.println(teacher.getName() + " courses: ");
		for (Course course : teacher.getCourses()) {
			System.out.println(course.getName() + ": " + course.hashCode());
			if (test.contains(course.getName())) {
				n--;
			}
		}
		System.out.println("---------------------");
		assertEquals(0, n);
	}

	static class CoursesRequest extends StubDriver {

		private static String[] jsonModel = {
				// English set : all
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"2\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"English\" } }, " +

						"{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
						"{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
						"{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
						"{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
						"{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
						"{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
						"{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
						"{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
						"{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
						"{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
						"{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
						"{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
						"{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
						"{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
						"{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
						"{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
						"{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
						"{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
						"{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
						"{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
						"{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
						"{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
						"{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
						"{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
						"{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }," +
						"{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"2101\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"101\",\"properties\":{}}," +
						"{\"id\":\"2102\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"102\",\"properties\":{}}," +
						"{\"id\":\"2103\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"103\",\"properties\":{}}," +
						"{\"id\":\"2104\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"104\",\"properties\":{}}," +
						"{\"id\":\"2105\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"105\",\"properties\":{}}," +
						"{\"id\":\"2106\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"106\",\"properties\":{}}," +
						"{\"id\":\"2107\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"107\",\"properties\":{}}," +
						"{\"id\":\"2108\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"108\",\"properties\":{}}," +
						"{\"id\":\"2109\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"109\",\"properties\":{}}," +
						"{\"id\":\"2110\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"110\",\"properties\":{}}," +
						"{\"id\":\"2111\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"111\",\"properties\":{}}," +
						"{\"id\":\"2112\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"112\",\"properties\":{}}," +
						"{\"id\":\"2113\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"113\",\"properties\":{}}," +
						"{\"id\":\"2114\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"114\",\"properties\":{}}," +
						"{\"id\":\"2115\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"115\",\"properties\":{}}," +
						"{\"id\":\"2116\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"116\",\"properties\":{}}," +
						"{\"id\":\"2117\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"117\",\"properties\":{}}," +
						"{\"id\":\"2118\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"118\",\"properties\":{}}," +
						"{\"id\":\"2119\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"119\",\"properties\":{}}," +
						"{\"id\":\"2120\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"120\",\"properties\":{}}," +
						"{\"id\":\"2121\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"121\",\"properties\":{}}," +
						"{\"id\":\"2122\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"122\",\"properties\":{}}," +
						"{\"id\":\"2123\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"123\",\"properties\":{}}," +
						"{\"id\":\"2124\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"124\",\"properties\":{}}," +
						"{\"id\":\"2125\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"125\",\"properties\":{}}," +
						"{\"id\":\"2126\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"126\",\"properties\":{}} " +
						"] " +
						"} }"
				,
				// Maths set : all
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"3\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Mathematics\" } }, " +

						"{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
						"{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
						"{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
						"{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
						"{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
						"{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
						"{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
						"{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
						"{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
						"{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
						"{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
						"{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
						"{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
						"{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
						"{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
						"{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
						"{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
						"{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
						"{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
						"{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
						"{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
						"{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
						"{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
						"{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
						"{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }," +
						"{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"3101\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"101\",\"properties\":{}}," +
						"{\"id\":\"3102\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"102\",\"properties\":{}}," +
						"{\"id\":\"3103\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"103\",\"properties\":{}}," +
						"{\"id\":\"3104\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"104\",\"properties\":{}}," +
						"{\"id\":\"3105\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"105\",\"properties\":{}}," +
						"{\"id\":\"3106\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"106\",\"properties\":{}}," +
						"{\"id\":\"3107\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"107\",\"properties\":{}}," +
						"{\"id\":\"3108\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"108\",\"properties\":{}}," +
						"{\"id\":\"3109\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"109\",\"properties\":{}}," +
						"{\"id\":\"3110\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"110\",\"properties\":{}}," +
						"{\"id\":\"3111\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"111\",\"properties\":{}}," +
						"{\"id\":\"3112\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"112\",\"properties\":{}}," +
						"{\"id\":\"3113\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"113\",\"properties\":{}}," +
						"{\"id\":\"3114\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"114\",\"properties\":{}}," +
						"{\"id\":\"3115\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"115\",\"properties\":{}}," +
						"{\"id\":\"3116\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"116\",\"properties\":{}}," +
						"{\"id\":\"3117\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"117\",\"properties\":{}}," +
						"{\"id\":\"3118\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"118\",\"properties\":{}}," +
						"{\"id\":\"3119\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"119\",\"properties\":{}}," +
						"{\"id\":\"3120\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"120\",\"properties\":{}}," +
						"{\"id\":\"3121\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"121\",\"properties\":{}}," +
						"{\"id\":\"3122\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"122\",\"properties\":{}}," +
						"{\"id\":\"3123\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"123\",\"properties\":{}}," +
						"{\"id\":\"3124\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"124\",\"properties\":{}}," +
						"{\"id\":\"3125\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"125\",\"properties\":{}}," +
						"{\"id\":\"3126\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"126\",\"properties\":{}} " +
						"] " +
						"} }"
				,
				// Physics set : odd(id)
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"4\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Physics\" } }, " +

						"{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
						"{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
						"{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
						"{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
						"{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
						"{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
						"{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
						"{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
						"{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
						"{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
						"{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
						"{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
						"{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"4101\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"101\",\"properties\":{}}," +
						"{\"id\":\"4103\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"103\",\"properties\":{}}," +
						"{\"id\":\"4106\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"105\",\"properties\":{}}," +
						"{\"id\":\"4107\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"107\",\"properties\":{}}," +
						"{\"id\":\"4109\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"109\",\"properties\":{}}," +
						"{\"id\":\"4111\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"111\",\"properties\":{}}," +
						"{\"id\":\"4113\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"113\",\"properties\":{}}," +
						"{\"id\":\"4115\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"115\",\"properties\":{}}," +
						"{\"id\":\"4117\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"117\",\"properties\":{}}," +
						"{\"id\":\"4119\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"119\",\"properties\":{}}," +
						"{\"id\":\"4121\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"121\",\"properties\":{}}," +
						"{\"id\":\"4123\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"123\",\"properties\":{}}," +
						"{\"id\":\"4125\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"125\",\"properties\":{}}" +
						"] " +
						"} }"

				,
				// Philosophy and Ethics set: all
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"5\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Philosophy and Ethics\" } }, " +

						"{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
						"{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
						"{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
						"{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
						"{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
						"{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
						"{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
						"{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
						"{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
						"{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
						"{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
						"{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
						"{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
						"{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
						"{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
						"{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
						"{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
						"{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
						"{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
						"{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
						"{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
						"{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
						"{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
						"{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
						"{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }," +
						"{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"5101\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"101\",\"properties\":{}}," +
						"{\"id\":\"5102\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"102\",\"properties\":{}}," +
						"{\"id\":\"5103\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"103\",\"properties\":{}}," +
						"{\"id\":\"5104\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"104\",\"properties\":{}}," +
						"{\"id\":\"5105\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"105\",\"properties\":{}}," +
						"{\"id\":\"5106\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"106\",\"properties\":{}}," +
						"{\"id\":\"5107\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"107\",\"properties\":{}}," +
						"{\"id\":\"5108\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"108\",\"properties\":{}}," +
						"{\"id\":\"5109\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"109\",\"properties\":{}}," +
						"{\"id\":\"5110\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"110\",\"properties\":{}}," +
						"{\"id\":\"5111\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"111\",\"properties\":{}}," +
						"{\"id\":\"5112\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"112\",\"properties\":{}}," +
						"{\"id\":\"5113\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"113\",\"properties\":{}}," +
						"{\"id\":\"5114\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"114\",\"properties\":{}}," +
						"{\"id\":\"5115\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"115\",\"properties\":{}}," +
						"{\"id\":\"5116\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"116\",\"properties\":{}}," +
						"{\"id\":\"5117\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"117\",\"properties\":{}}," +
						"{\"id\":\"5118\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"118\",\"properties\":{}}," +
						"{\"id\":\"5119\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"119\",\"properties\":{}}," +
						"{\"id\":\"5120\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"120\",\"properties\":{}}," +
						"{\"id\":\"5121\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"121\",\"properties\":{}}," +
						"{\"id\":\"5122\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"122\",\"properties\":{}}," +
						"{\"id\":\"5123\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"123\",\"properties\":{}}," +
						"{\"id\":\"5124\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"124\",\"properties\":{}}," +
						"{\"id\":\"5125\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"125\",\"properties\":{}}," +
						"{\"id\":\"5126\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"126\",\"properties\":{}} " +
						"] " +
						"} }"
				,
				// PE set: isInteger((id modulo 100) / 3)
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"6\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"PE\" } }, " +

						"{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
						"{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
						"{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
						"{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
						"{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
						"{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
						"{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
						"{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"6103\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"103\",\"properties\":{}}," +
						"{\"id\":\"6106\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"106\",\"properties\":{}}," +
						"{\"id\":\"6109\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"109\",\"properties\":{}}," +
						"{\"id\":\"6112\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"112\",\"properties\":{}}," +
						"{\"id\":\"6115\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"115\",\"properties\":{}}," +
						"{\"id\":\"6118\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"118\",\"properties\":{}}," +
						"{\"id\":\"6121\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"121\",\"properties\":{}}," +
						"{\"id\":\"6124\",\"type\":\"ENROLLED\",\"startNode\":\"6\",\"endNode\":\"124\",\"properties\":{}}" +
						"] " +
						"} }"

				,
				// History set even(id)
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"7\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"History\" } }, " +

						"{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
						"{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
						"{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
						"{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
						"{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
						"{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
						"{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
						"{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
						"{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
						"{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
						"{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
						"{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
						"{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"7102\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"102\",\"properties\":{}}," +
						"{\"id\":\"7104\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"104\",\"properties\":{}}," +
						"{\"id\":\"7106\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"106\",\"properties\":{}}," +
						"{\"id\":\"7108\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"108\",\"properties\":{}}," +
						"{\"id\":\"7110\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"110\",\"properties\":{}}," +
						"{\"id\":\"7112\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"112\",\"properties\":{}}," +
						"{\"id\":\"7114\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"114\",\"properties\":{}}," +
						"{\"id\":\"7116\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"116\",\"properties\":{}}," +
						"{\"id\":\"7118\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"118\",\"properties\":{}}," +
						"{\"id\":\"7120\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"120\",\"properties\":{}}," +
						"{\"id\":\"7122\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"122\",\"properties\":{}}," +
						"{\"id\":\"7124\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"124\",\"properties\":{}}," +
						"{\"id\":\"7126\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"126\",\"properties\":{}} " +
						"] " +
						"} }"
				,
				// Geography set : isPrime(id modulo 100)
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"8\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Geography\" } }, " +

						"{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
						"{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
						"{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
						"{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
						"{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
						"{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
						"{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
						"{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
						"{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }" +

						"], " +
						"\"relationships\": [" +
						"{\"id\":\"8102\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"102\",\"properties\":{}}," +
						"{\"id\":\"8103\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"103\",\"properties\":{}}," +
						"{\"id\":\"8105\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"105\",\"properties\":{}}," +
						"{\"id\":\"8107\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"107\",\"properties\":{}}," +
						"{\"id\":\"8111\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"111\",\"properties\":{}}," +
						"{\"id\":\"8113\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"113\",\"properties\":{}}," +
						"{\"id\":\"8117\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"117\",\"properties\":{}}," +
						"{\"id\":\"8119\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"119\",\"properties\":{}}," +
						"{\"id\":\"8123\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"123\",\"properties\":{}}" +
						"] " +
						"} }"
		};

		public String[] getResponse() {
			return jsonModel;
		}
	}

	static class TeacherRequest extends StubDriver {

		private static String[] jsonModel = {

				// mr thomas results
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
						"{\"id\" : \"2\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"English\" } } " +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"202\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"2\",\"properties\":{}}" +
						"] " +
						"} }"
				,
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
						"{\"id\" : \"3\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Maths\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"203\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"3\",\"properties\":{}}" +
						"] " +
						"} }"
				,
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
						"{\"id\" : \"4\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Physics\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"204\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"4\",\"properties\":{}} " +
						"] " +
						"} }"

				,
				// mrs roberts results

				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
						"{\"id\" : \"6\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"PE\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"212\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"2\",\"properties\":{}}" +
						"] " +
						"} }",
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
						"{\"id\" : \"2\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"English\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"216\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"6\",\"properties\":{}}" +
						"] " +
						"} }",
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
						"{\"id\" : \"7\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"History\" } } " +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"217\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"7\",\"properties\":{}} " +
						"] " +
						"} }",

				// miss young results
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
						"{\"id\" : \"5\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Philosophy and Ethics\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"225\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"5\",\"properties\":{}}" +
						"] " +
						"} }",
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
						"{\"id\" : \"7\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"History\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"227\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"7\",\"properties\":{}}" +
						"] " +
						"} }",
				"{\"graph\": { " +
						"\"nodes\" :[ " +
						"{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
						"{\"id\" : \"8\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Geography\" } }" +
						"], " +
						"\"relationships\": [" +
						"{\"id\":\"228\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"8\",\"properties\":{}}" +
						"] " +
						"} }"
		};

		public String[] getResponse() {
			return jsonModel;
		}
	}
}
