package com.aitutor.config;

import com.aitutor.entity.Course;
import com.aitutor.entity.Lesson;
import com.aitutor.entity.Progress;
import com.aitutor.entity.User;
import com.aitutor.quiz.QuizQuestion;
import com.aitutor.quiz.QuizQuestionRepository;
import com.aitutor.quiz.QuizBank;
import com.aitutor.quiz.QuizExpansion;
import com.aitutor.repository.CourseRepository;
import com.aitutor.repository.LessonRepository;
import com.aitutor.repository.ProgressRepository;
import com.aitutor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

/**
 * NovaTutor V69 foundation seeder.
 *
 * The old V68 seeder expanded every course to 105 lessons by copying a small
 * lesson set and appending generic "Deep Dive / Worked Example / Practice Lab"
 * variants. That created quantity without enough educational value.
 *
 * V69 replaces that path with a deterministic, course-specific 15-module x
 * 7-stage curriculum (105 lessons per course). Existing student progress is
 * preserved by lesson count and re-mapped to the new 105-lesson course total.
 */
@Configuration
public class DataSeeder {
    private static final String CURRICULUM_KEY = "novatutor_curriculum_v69_production_105";
    private static final String QUIZ_KEY = "novatutor_quiz_bank_v69_curated";

    @Bean
    @Order(2)
    CommandLineRunner seed(
            CourseRepository courses,
            LessonRepository lessons,
            QuizQuestionRepository quizzes,
            UserRepository users,
            ProgressRepository progress,
            PasswordEncoder encoder,
            AppMetaRepository meta,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        return args -> {
            if (!seedEnabled) return;
            List<Course> courseList = ensureCourses(courses);
            ensureAdmin(users, encoder, adminEmail, adminPassword);
            ensureProductionCurriculum(courses, lessons, progress, meta, courseList);
            ensureProductionQuizBank(quizzes, courses, meta);
        };
    }

    private List<Course> ensureCourses(CourseRepository repo) {
        List<Course> result = new ArrayList<>();
        result.add(ensureCourse(repo, "HTML Mastery", "Build a strong HTML foundation from semantic structure to accessible production pages.", "Beginner", "</>"));
        result.add(ensureCourse(repo, "Java + Spring Boot", "Learn Java deeply and build production-style Spring Boot APIs with validation, persistence and security.", "Intermediate", "☕"));
        result.add(ensureCourse(repo, "DSA Fundamentals", "Master algorithmic thinking, data structures, complexity and interview problem-solving patterns.", "Intermediate", "⌘"));
        result.add(ensureCourse(repo, "Python Programming", "Learn Python syntax, data structures, functions, OOP, testing and practical automation.", "Beginner", "🐍"));
        result.add(ensureCourse(repo, "SQL & Database Mastery", "Master relational modeling, SQL, normalization, indexing, transactions and performance.", "Intermediate", "🗄️"));
        result.add(ensureCourse(repo, "Computer Networks", "Understand TCP/IP, addressing, routing, DNS, HTTP, TLS and network troubleshooting.", "Intermediate", "🌐"));
        result.add(ensureCourse(repo, "Operating Systems", "Understand processes, threads, scheduling, synchronization, memory, storage and concurrency.", "Intermediate", "⚙️"));
        result.add(ensureCourse(repo, "System Design Fundamentals", "Learn requirements, APIs, storage, caching, queues, scalability and reliability through real designs.", "Advanced", "🏗️"));
        result.add(ensureCourse(repo, "Git & GitHub", "Master version control, branching, collaboration, reviews, CI and reliable release workflows.", "Beginner", "🌿"));
        result.add(ensureCourse(repo, "JavaScript & React", "Build modern web interfaces with JavaScript, React, state, effects, APIs and accessible components.", "Intermediate", "⚛️"));
        result.add(ensureCourse(repo, "Spring Security & REST APIs", "Build secure Spring Boot APIs with authentication, JWT, authorization, validation and testing.", "Advanced", "🔐"));
        result.add(ensureCourse(repo, "Cloud & DevOps Fundamentals", "Learn Linux, containers, CI/CD, cloud deployment, monitoring, secrets and rollback practices.", "Intermediate", "☁️"));
        result.add(ensureCourse(repo, "Aptitude & Placement Prep", "Practice quantitative aptitude, logical reasoning, verbal ability, coding aptitude and interview strategy.", "Beginner", "🎯"));
        return result;
    }

    private Course ensureCourse(CourseRepository repo, String title, String description, String level, String icon) {
        Course course = repo.findAll().stream()
                .filter(existing -> title.equals(existing.getTitle()))
                .findFirst()
                .orElseGet(() -> repo.save(new Course(title, description, level, icon)));
        course.setDescription(description);
        course.setLevel(level);
        course.setIcon(icon);
        return repo.save(course);
    }

    private void ensureAdmin(UserRepository users, PasswordEncoder encoder, String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return;
        String clean = email.trim().toLowerCase(Locale.ROOT);
        User owner = users.findByEmail(clean).orElse(null);
        if (owner == null) {
            owner = new User("NovaTutor Admin", clean, encoder.encode(password));
            owner.setRole("ADMIN");
            users.save(owner);
        } else if (!"ADMIN".equalsIgnoreCase(owner.getRole())) {
            owner.setRole("ADMIN");
            users.save(owner);
        }
    }

    private void ensureProductionCurriculum(
            CourseRepository courses,
            LessonRepository lessons,
            ProgressRepository progress,
            AppMetaRepository meta,
            List<Course> courseList
    ) {
        if (meta.existsById(CURRICULUM_KEY)) return;
        // Never rebuild or delete an existing production curriculum implicitly.
        // Fresh empty databases may be seeded; populated databases require an
        // explicit migration rather than a startup wipe.
        if (lessons.count() > 0) return;

        for (Course course : courseList) {
            List<ProductionCurriculum.LessonSpec> specs = ProductionCurriculum.forCourse(course.getTitle());
            if (specs.size() != 105) {
                throw new IllegalStateException("Production curriculum for " + course.getTitle() + " must contain exactly 105 lessons; found " + specs.size());
            }

            List<Lesson> batch = new ArrayList<>(105);
            int order = 1;
            for (ProductionCurriculum.LessonSpec spec : specs) {
                Lesson lesson = new Lesson();
                lesson.setCourse(course);
                lesson.setOrderIndex(order++);
                lesson.setTopic(spec.module());
                lesson.setTitle("Lesson " + (order - 1) + " — " + spec.title());
                lesson.setContent(spec.content());
                batch.add(lesson);
            }
            lessons.saveAll(batch);
            course.setTotalLessons(105);
            courses.save(course);
        }

        for (Progress p : progress.findAll()) {
            Course course = p.getCourse();
            if (course == null) continue;
            int completed = Math.min(Math.max(0, p.getCompletedLessons()), 105);
            p.setTotalLessons(105);
            p.setCompletedLessons(completed);
            p.setPercent(completed * 100.0 / 105.0);
        }
        progress.flush();
        meta.save(new AppMeta(CURRICULUM_KEY, "13 courses x 105 production curriculum lessons; topic-specific content and practice; legacy generic expansion removed"));
    }

    private void ensureProductionQuizBank(QuizQuestionRepository quizzes, CourseRepository courses, AppMetaRepository meta) {
        if (meta.existsById(QUIZ_KEY)) {
            ensureQuizExpansion(quizzes, courses, meta);
            return;
        }
        // Never delete an existing quiz bank during startup. Seed only a fresh DB.
        if (quizzes.count() > 0) return;
        for (Course course : courses.findAll()) {
            for (QuizBank.Q item : QuizBank.forCourse(course.getTitle())) {
                QuizQuestion q = new QuizQuestion();
                q.setCourse(course);
                q.setQuestion(item.question());
                q.setOptionA(item.a());
                q.setOptionB(item.b());
                q.setOptionC(item.c());
                q.setOptionD(item.d());
                q.setCorrectOption(item.correct());
                q.setExplanation(item.explanation());
                q.setTopic(conceptFromQuestion(course, item.question()));
                q.setDifficulty(item.difficulty());
                quizzes.save(q);
            }
        }
        meta.save(new AppMeta(QUIZ_KEY, "Curated course-specific quiz bank restored after curriculum foundation cleanup"));
        ensureQuizExpansion(quizzes, courses, meta);
    }

    private void ensureQuizExpansion(QuizQuestionRepository quizzes, CourseRepository courses, AppMetaRepository meta) {
        final String key = "v70.4.quiz.expansion.1000";
        if (meta.existsById(key)) return;
        int added = 0;
        for (Course course : courses.findAll()) {
            for (QuizBank.Q item : QuizExpansion.forCourse(course.getTitle())) {
                QuizQuestion q = new QuizQuestion();
                q.setCourse(course);
                q.setQuestion(item.question());
                q.setOptionA(item.a());
                q.setOptionB(item.b());
                q.setOptionC(item.c());
                q.setOptionD(item.d());
                q.setCorrectOption(item.correct());
                q.setExplanation(item.explanation());
                q.setTopic(conceptFromQuestion(course, item.question()));
                q.setDifficulty(item.difficulty());
                quizzes.save(q);
                added++;
            }
        }
        meta.save(new AppMeta(key, "Added exactly " + added + " original course-specific exam-style questions."));
    }

    private String conceptFromQuestion(Course course, String question) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        String[][] map = {
                {"spring", "Spring Boot"}, {"rest", "REST APIs"}, {"security", "Security"}, {"jwt", "JWT"},
                {"sql", "SQL"}, {"database", "Database Design"}, {"normaliz", "Normalization"}, {"index", "Indexing"},
                {"process", "Processes"}, {"thread", "Threads"}, {"memory", "Memory Management"}, {"deadlock", "Deadlocks"},
                {"tcp", "TCP"}, {"http", "HTTP"}, {"dns", "DNS"}, {"routing", "Routing"},
                {"array", "Arrays"}, {"linked", "Linked Lists"}, {"tree", "Trees"}, {"graph", "Graphs"}, {"queue", "Queues"}, {"stack", "Stacks"},
                {"binary search", "Binary Search"}, {"hash", "Hashing"}, {"recursion", "Recursion"}, {"dynamic", "Dynamic Programming"},
                {"html", "HTML Semantics"}, {"css", "CSS"}, {"react", "React"}, {"javascript", "JavaScript"},
                {"git", "Git"}, {"docker", "Containers"}, {"cloud", "Cloud"}, {"python", "Python"}, {"aptitude", "Aptitude"}
        };
        for (String[] pair : map) if (q.contains(pair[0])) return pair[1];
        if (course != null && course.getTitle() != null) {
            if (course.getTitle().contains("DSA")) return "Core DSA";
            if (course.getTitle().contains("Java")) return "Core Java";
        }
        return "Core Concepts";
    }
}
