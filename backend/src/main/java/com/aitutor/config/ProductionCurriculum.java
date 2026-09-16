package com.aitutor.config;

import java.util.*;

public final class ProductionCurriculum {
    public record LessonSpec(String module, String stage, String title, String content) {}
    private static final List<String> STAGES = List.of("Concept & Mental Model", "How It Works", "Worked Example", "Implementation", "Edge Cases & Debugging", "Practice Lab", "Review & Interview Check");
    private static final Map<String, List<String>> MODULES = new LinkedHashMap<>();
    static {
        MODULES.put("HTML Mastery", List.of(
                "HTML document structure and parsing",
                "Semantic headings and content hierarchy",
                "Links, URLs and navigation",
                "Images, responsive media and captions",
                "Forms, labels and validation",
                "Tables and accessible tabular data",
                "Semantic layout elements",
                "Metadata, SEO and social previews",
                "Accessibility and ARIA",
                "HTML entities and international text",
                "Embedded content and security",
                "Interactive elements and dialogs",
                "Templates, fragments and reusable markup",
                "Browser DevTools and DOM debugging",
                "Capstone: accessible course landing page"
        ));
        MODULES.put("Java + Spring Boot", List.of(
                "JDK, JVM and program structure",
                "Variables, primitives and type conversion",
                "Operators, expressions and evaluation",
                "Conditionals and loops",
                "Methods, parameters and recursion",
                "Arrays and string processing",
                "Classes, objects and constructors",
                "Encapsulation and immutability",
                "Inheritance and composition",
                "Polymorphism and abstraction",
                "Exceptions and defensive programming",
                "Collections and generics",
                "Lambdas, functional interfaces and streams",
                "Spring Boot, dependency injection and configuration",
                "REST APIs, JPA, validation and security capstone"
        ));
        MODULES.put("DSA Fundamentals", List.of(
                "Complexity analysis and constraints",
                "Arrays and traversal patterns",
                "Strings and frequency counting",
                "Two pointers and partitioning",
                "Sliding window",
                "Prefix sums and difference arrays",
                "Linked lists",
                "Stacks and monotonic stacks",
                "Queues and deques",
                "Hashing and lookup design",
                "Trees and binary search trees",
                "Heaps and priority queues",
                "Graphs and traversal",
                "Greedy and dynamic programming",
                "Interview problem solving and mixed patterns"
        ));
        MODULES.put("Python Programming", List.of(
                "Python execution model and syntax",
                "Variables, types and mutability",
                "Operators and expressions",
                "Conditionals and loops",
                "Functions, scope and recursion",
                "Strings and text processing",
                "Lists, tuples and slicing",
                "Sets, dictionaries and comprehensions",
                "Modules, packages and virtual environments",
                "Exceptions, files and context managers",
                "Object-oriented Python",
                "Iterators, generators and decorators",
                "Testing, typing and clean code",
                "Automation with APIs and data processing",
                "Capstone automation project"
        ));
        MODULES.put("SQL & Database Mastery", List.of(
                "Relational model and SQL basics",
                "SELECT, filtering and sorting",
                "Aggregations and grouping",
                "Joins and relational reasoning",
                "Subqueries and CTEs",
                "Set operations and window functions",
                "Keys, constraints and schema design",
                "Normalization and functional dependencies",
                "Indexes and query planning",
                "Transactions and isolation",
                "Views, procedures and database objects",
                "Concurrency and locking",
                "MySQL performance and optimization",
                "Security, backups and reliability",
                "Capstone database design and reporting"
        ));
        MODULES.put("Computer Networks", List.of(
                "Network models and encapsulation",
                "Physical and data link foundations",
                "Ethernet, switching and ARP",
                "IP addressing and subnetting",
                "Routing and forwarding",
                "TCP fundamentals",
                "UDP and transport trade-offs",
                "DNS and name resolution",
                "HTTP and web protocols",
                "TLS and secure communication",
                "NAT, DHCP and network services",
                "Wireless and modern networking",
                "Sockets and application communication",
                "Network troubleshooting and observability",
                "Network security and capstone design"
        ));
        MODULES.put("Operating Systems", List.of(
                "OS role, kernel and system calls",
                "Processes and process states",
                "Threads and concurrency",
                "CPU scheduling",
                "Synchronization primitives",
                "Deadlocks and resource allocation",
                "Memory management",
                "Virtual memory and paging",
                "File systems and storage",
                "I/O and device management",
                "Protection and access control",
                "Inter-process communication",
                "Virtualization and containers",
                "Performance analysis and debugging",
                "OS design case study"
        ));
        MODULES.put("System Design Fundamentals", List.of(
                "Requirements and capacity estimation",
                "API design and contracts",
                "Data modeling and storage choices",
                "Caching strategies",
                "Load balancing and traffic management",
                "Queues and asynchronous processing",
                "Consistency and distributed systems",
                "Partitioning and replication",
                "Search and indexing systems",
                "Rate limiting and abuse control",
                "Observability and reliability",
                "Authentication and authorization architecture",
                "Scalability patterns and bottlenecks",
                "Failure handling and disaster recovery",
                "End-to-end system design case study"
        ));
        MODULES.put("Git & GitHub", List.of(
                "Git mental model and repositories",
                "Commits, history and clean changes",
                "Branches and merging",
                "Rebase and conflict resolution",
                "Remote repositories and collaboration",
                "Pull requests and code review",
                "Tags, releases and semantic versioning",
                "Ignore rules and repository hygiene",
                "Stashing, reflog and recovery",
                "Cherry-pick and advanced history editing",
                "GitHub Issues, Projects and workflows",
                "CI with GitHub Actions",
                "Secrets, security and branch protection",
                "Release automation and team workflow",
                "Capstone team repository workflow"
        ));
        MODULES.put("JavaScript & React", List.of(
                "JavaScript runtime and syntax",
                "Variables, types and coercion",
                "Functions, scope and closures",
                "Arrays, objects and modern syntax",
                "DOM and browser events",
                "Asynchronous JavaScript and promises",
                "Modules, fetch and API integration",
                "React components and JSX",
                "Props, state and controlled UI",
                "Effects, lifecycle and data fetching",
                "Forms, validation and user input",
                "Routing and application structure",
                "Reusable hooks and component architecture",
                "Performance, accessibility and testing",
                "Capstone React learning dashboard"
        ));
        MODULES.put("Spring Security & REST APIs", List.of(
                "HTTP API fundamentals and resource design",
                "DTOs, validation and error contracts",
                "Spring Boot REST controllers",
                "Service and repository architecture",
                "JPA persistence and relationships",
                "Authentication architecture",
                "JWT access tokens and sessions",
                "Authorization and ownership checks",
                "Password security and account lifecycle",
                "CORS, CSRF and browser security",
                "API rate limiting and abuse prevention",
                "Testing secure endpoints",
                "Logging, audit and observability",
                "Production configuration and secrets",
                "Capstone secure learning API"
        ));
        MODULES.put("Cloud & DevOps Fundamentals", List.of(
                "Linux and server fundamentals",
                "Processes, files and permissions",
                "Shell scripting and automation",
                "Git-based delivery workflow",
                "Docker images and containers",
                "Container networking and storage",
                "Docker Compose and local environments",
                "CI/CD pipelines",
                "Cloud compute and networking",
                "Cloud storage and databases",
                "Configuration and secrets management",
                "Monitoring, logs and alerts",
                "Infrastructure reliability and scaling",
                "Deployment strategies and rollback",
                "Capstone production deployment"
        ));
        MODULES.put("Aptitude & Placement Prep", List.of(
                "Quantitative fundamentals and estimation",
                "Percentages, ratios and proportions",
                "Averages, mixtures and alligation",
                "Profit, loss and discounts",
                "Time, work and pipes",
                "Speed, distance and relative motion",
                "Simple and compound interest",
                "Number systems and divisibility",
                "Permutation, combination and probability",
                "Data interpretation",
                "Logical reasoning and arrangements",
                "Critical reasoning and analytical puzzles",
                "Verbal ability and reading comprehension",
                "Coding aptitude and CS fundamentals",
                "Interview strategy and mock assessment"
        ));
    }

    private ProductionCurriculum() {}

    public static List<LessonSpec> forCourse(String course) {
        List<String> modules = MODULES.get(course);
        if (modules == null) return List.of();
        List<LessonSpec> result = new ArrayList<>(105);
        int number = 1;
        for (String module : modules) {
            for (int stage = 0; stage < STAGES.size(); stage++) {
                String title = module + " — " + STAGES.get(stage);
                result.add(new LessonSpec(module, STAGES.get(stage), title, buildContent(course, module, number, stage)));
                number++;
            }
        }
        return result;
    }

    public static Set<String> supportedCourses() { return Collections.unmodifiableSet(MODULES.keySet()); }

    private static String buildContent(String course, String module, int number, int stage) {
        String stageFocus = switch (stage) {
            case 0 -> "Define the terms, explain why the concept exists, and connect it to the previous module.";
            case 1 -> "Trace the mechanics step by step. Identify the rule, invariant, data flow or decision that must remain true.";
            case 2 -> "Solve one realistic example completely. Show intermediate reasoning instead of jumping to the final answer.";
            case 3 -> "Turn the idea into a small working artifact and explain each important step. Prefer a correct, readable baseline before optimization.";
            case 4 -> "Deliberately test boundary cases, invalid inputs and failure modes. Identify the observation that would distinguish one bug from another.";
            case 5 -> "Work independently for 15–25 minutes. Write the approach first, test it, and record one thing that initially went wrong.";
            default -> "Recall the definition, compare alternatives, answer interview/exam questions, and explain the concept without notes.";
        };
        return "# Lesson " + number + " — " + module + " — " + STAGES.get(stage) + "\n\n" +
               "## Learning objective\nBy the end of this lesson you should be able to explain, apply and verify **" + module + "** in the context of **" + course + "**.\n\n" +
               "## Core idea\n" + CORE.get(course) + "\n\n" +
               "## Topic-specific focus\n" + topicFocus(course, module, stage) + "\n\n" +
               "## What to do in this lesson\n" + stageFocus + "\n\n" +
               "## Worked context\n" + EXAMPLE.get(course) + "\n\n" +
               "## Practical reference\n```text\n" + REFERENCE.get(course) + "\n```\n\n" +
               "## Common mistakes\n- Memorizing terminology without being able to explain the mechanism.\n- Skipping inputs, constraints or assumptions before solving.\n- Treating the happy path as proof that an implementation is correct.\n- Ignoring relevant readability, security, accessibility or performance trade-offs.\n\n" +
               "## Practice task 🎯\n" + PRACTICE.get(course) + "\n\n" +
               "## Self-check\n1. Define **" + module + "** without looking at the notes.\n2. What problem does it solve, and when should you not use it?\n3. What is one failure mode or edge case?\n4. How would you verify the result?\n5. What changes if the input, traffic or constraints double?\n\n" +
               "## Completion standard\nDo not mark this lesson complete just because you read it. Mark it complete when you can explain the concept, finish the practice task, and answer the self-check from memory.";
    }


    private static String topicFocus(String course, String module, int stage) {
        String m = module.toLowerCase(Locale.ROOT);
        if ("DSA Fundamentals".equals(course)) {
            if (m.contains("complexity")) return "Use concrete constraints such as n=10^5 to compare O(n), O(n log n) and O(n^2), and decide which approaches are realistic.";
            if (m.contains("arrays")) return "Practice traversal, prefix/suffix state and in-place updates on arrays; explicitly handle empty arrays, duplicates and boundary indices.";
            if (m.contains("strings")) return "Turn characters into a frequency/state representation, then use it to solve a concrete anagram or first-unique-character style problem.";
            if (m.contains("two pointers")) return "Trace two indices moving under an invariant; use a sorted pair-sum example and explain why neither pointer needs to move backwards.";
            if (m.contains("sliding window")) return "Maintain a valid window while expanding and shrinking it; solve a longest-subarray-with-constraint example and identify the invariant.";
            if (m.contains("prefix sums")) return "Build prefix state so a range query can be answered without rescanning the range; compare O(n) preprocessing with O(1) query time.";
            if (m.contains("linked lists")) return "Trace node references while reversing or detecting a cycle; draw the pointer changes before writing code.";
            if (m.contains("stacks")) return "Use a stack to preserve unresolved elements and derive a next-greater-element or bracket-validation solution.";
            if (m.contains("queues")) return "Use FIFO order for level-by-level processing; connect a queue/deque to BFS and explain why insertion/removal positions matter.";
            if (m.contains("hashing")) return "Choose a key that represents the information you need to remember, then solve a duplicate/frequency lookup problem with expected O(1) access.";
            if (m.contains("trees")) return "Compare preorder, inorder, postorder and level-order traversal, then use BST ordering to search or validate a tree.";
            if (m.contains("heaps")) return "Use a heap when you repeatedly need the smallest/largest item; compare heap operations with sorting the entire collection.";
            if (m.contains("graphs")) return "Model entities as vertices and relationships as edges, then choose BFS/DFS based on the question being asked.";
            if (m.contains("greedy")) return "Identify a local-choice strategy, then justify why the choice is safe instead of assuming greedy is correct because it works on one example.";
            if (m.contains("dynamic programming")) return "Write the state, transition, base cases and answer location explicitly; compare memoization and tabulation on one problem.";
            return "For the mixed-pattern module, classify a new problem by input constraints, observable structure and reusable pattern before choosing an algorithm.";
        }
        if ("SQL & Database Mastery".equals(course)) {
            if (m.contains("relational model")) return "Model students, courses and enrollments as relations and identify primary/foreign keys before writing a query.";
            if (m.contains("select")) return "Practice WHERE, ORDER BY and LIMIT on a realistic orders table and predict how NULL values affect filtering.";
            if (m.contains("aggregations")) return "Use COUNT/SUM/AVG with GROUP BY and HAVING to answer a business question without accidentally mixing row-level and group-level filters.";
            if (m.contains("joins")) return "Join students to enrollments and courses, inspect row multiplication, and decide when INNER JOIN versus LEFT JOIN is correct.";
            if (m.contains("subqueries")) return "Use a subquery or CTE to separate an intermediate result, then compare readability and execution behavior with a join.";
            if (m.contains("window")) return "Use ROW_NUMBER, RANK or SUM OVER to compute per-student rankings or running totals without collapsing rows.";
            if (m.contains("keys")) return "Design a schema with primary, foreign, unique and check constraints so invalid learning records are rejected by the database.";
            if (m.contains("normalization")) return "Start from a deliberately duplicated enrollment table, identify functional dependencies, then decompose it toward 3NF.";
            if (m.contains("indexes")) return "Compare a query before and after an index, and explain why selectivity, column order and the WHERE/ORDER BY pattern matter.";
            if (m.contains("transactions")) return "Use a money-transfer or course-enrollment example to show atomicity and what can go wrong when two writes are not in one transaction.";
            if (m.contains("concurrency")) return "Trace two concurrent updates and identify a lost update, lock wait or deadlock; state the isolation/locking response.";
            if (m.contains("performance")) return "Read an EXPLAIN plan, find the expensive operation and choose one measurable optimization rather than guessing.";
            if (m.contains("security")) return "Separate application authorization from database permissions, protect secrets, and define a backup/restore test rather than assuming backups work.";
            return "Design a small learning database, write representative reports, add constraints/indexes, and justify every schema decision.";
        }
        if ("HTML Mastery".equals(course)) {
            if (m.contains("structure")) return "Write a valid doctype/html/head/body skeleton and explain what belongs in head versus body.";
            if (m.contains("headings")) return "Build a course article with one logical h1 and nested h2/h3 sections; verify the outline remains meaningful without CSS.";
            if (m.contains("links")) return "Create internal, external and fragment links and explain href, target, rel and safe new-tab behavior.";
            if (m.contains("images")) return "Add an image with useful alt text and a responsive media example; decide when an image is decorative versus informative.";
            if (m.contains("forms")) return "Build a signup form with label/id associations, required fields and native validation; test keyboard-only completion.";
            if (m.contains("tables")) return "Create a marks table with th, scope and caption so a screen reader can understand row and column relationships.";
            if (m.contains("semantic layout")) return "Choose between main, nav, header, footer, section, article and aside for a course dashboard instead of defaulting to div.";
            if (m.contains("metadata")) return "Set title, description, lang and social metadata for a course page and explain which values affect search/share previews.";
            if (m.contains("accessibility")) return "Navigate a page without a mouse, identify focus order and missing names, and fix the markup before adding ARIA.";
            if (m.contains("entities")) return "Handle reserved characters and multilingual content correctly, including UTF-8 and the lang attribute.";
            if (m.contains("embedded")) return "Embed external content with iframe/embed where appropriate and discuss sandboxing, permissions and trusted origins.";
            if (m.contains("interactive")) return "Use details/summary or dialog for progressive disclosure and explain keyboard, focus and close behavior.";
            if (m.contains("templates")) return "Separate reusable markup from page-specific data and explain how template cloning differs from copy-pasting HTML.";
            if (m.contains("devtools")) return "Inspect the DOM, accessibility tree, computed attributes and network-loaded document resources to diagnose a markup bug.";
            return "Build an accessible course landing page using semantic structure, forms, media, navigation, metadata and a final keyboard audit.";
        }
        if ("Java + Spring Boot".equals(course)) {
            if (m.contains("jdk")) return "Compile and run a tiny Java program, inspect the class/JVM model, and explain source-to-bytecode-to-runtime flow.";
            if (m.contains("variables")) return "Compare primitive values, reference variables and widening/narrowing conversions with examples that expose precision loss.";
            if (m.contains("operators")) return "Trace precedence, short-circuit evaluation and integer arithmetic; predict results before running the code.";
            if (m.contains("conditionals")) return "Implement validation rules with if/switch/loops and keep each branch mutually understandable and testable.";
            if (m.contains("methods")) return "Design small methods with clear parameters/return values and use recursion only when the recursive state is easy to prove.";
            if (m.contains("arrays")) return "Traverse arrays and strings, compare String with StringBuilder, and test boundary indices and empty input.";
            if (m.contains("classes")) return "Model a learning domain object, choose constructor responsibilities and explain object identity versus value data.";
            if (m.contains("encapsulation")) return "Protect invariants with private state and controlled methods; compare mutable setters with immutable value objects.";
            if (m.contains("inheritance")) return "Compare inheritance with composition on a realistic service model and identify where an is-a relationship is actually valid.";
            if (m.contains("polymorphism")) return "Use an interface with multiple implementations and trace dynamic dispatch without relying on instanceof everywhere.";
            if (m.contains("exceptions")) return "Distinguish checked and unchecked failures, validate boundaries and preserve useful context without swallowing exceptions.";
            if (m.contains("collections")) return "Choose List, Set, Map or Queue based on access requirements, then add generics so invalid element types are caught early.";
            if (m.contains("lambdas")) return "Transform a collection with a lambda/stream, then decide whether the declarative form remains readable and testable.";
            if (m.contains("spring boot")) return "Trace a request through controller, service and repository beans and explain how dependency injection supplies each dependency.";
            return "Build a small REST API with DTO validation, JPA persistence and security boundaries, then test both success and failure paths.";
        }
        if ("Python Programming".equals(course)) {
            if (m.contains("execution")) return "Run a small script and inspect names, indentation, imports and the interpreter's execution flow.";
            if (m.contains("variables")) return "Demonstrate aliasing and mutability with lists/dicts and explain why two variables can reference the same object.";
            if (m.contains("operators")) return "Trace comparisons, boolean short-circuiting, identity versus equality and numeric operations.";
            if (m.contains("conditionals")) return "Build a small input validator with if/elif/else and loops, then test empty and invalid input.";
            if (m.contains("functions")) return "Write functions with explicit parameters/returns and trace recursion using a small base case before scaling it.";
            if (m.contains("strings")) return "Use slicing, split/join and formatting to transform real text while handling Unicode and empty strings.";
            if (m.contains("lists")) return "Compare list copying, slicing and mutation; use tuple when the record should be immutable.";
            if (m.contains("sets")) return "Use sets for uniqueness and dictionaries for keyed state, then replace a nested lookup with an appropriate comprehension.";
            if (m.contains("modules")) return "Create a small package, isolate dependencies in a virtual environment and explain import resolution.";
            if (m.contains("exceptions")) return "Read and write a file with a context manager and design exception handling that reports the actual failure cause.";
            if (m.contains("object-oriented")) return "Model a small domain object, choose instance/class behavior deliberately and avoid inheritance when composition is clearer.";
            if (m.contains("iterators")) return "Trace next() and generator yield behavior, then compare a generator with building a complete list in memory.";
            if (m.contains("testing")) return "Add a focused test for a function, type its boundary where useful, and refactor one confusing responsibility.";
            if (m.contains("automation")) return "Call a small API, parse JSON and write a useful report while handling timeouts, missing fields and rate limits.";
            return "Combine input validation, reusable functions, file/API processing and tests into a small automation tool.";
        }
        return "Turn the module title into a concrete question or artifact. Identify the input, expected result, governing rule, edge cases and a way to verify the answer before marking the lesson complete.";
    }
    private static final Map<String,String> CORE = new HashMap<>();
    private static final Map<String,String> EXAMPLE = new HashMap<>();
    private static final Map<String,String> PRACTICE = new HashMap<>();
    private static final Map<String,String> REFERENCE = new HashMap<>();
    static {
        CORE.put("HTML Mastery", "HTML elements describe document structure and semantics; the browser parses that structure into a DOM that other platform features can use.");
        CORE.put("Java + Spring Boot", "Java gives you a strongly typed object-oriented language, while Spring Boot provides dependency injection, HTTP and persistence infrastructure around your application code.");
        CORE.put("DSA Fundamentals", "An algorithm is a precise strategy whose correctness and time/space cost must be matched to the input constraints.");
        CORE.put("Python Programming", "Python emphasizes readable syntax, dynamic typing and expressive built-in data structures; good Python still requires explicit reasoning about state, errors and performance.");
        CORE.put("SQL & Database Mastery", "SQL expresses relational operations over structured data; correct SQL depends on understanding rows, keys, joins, NULLs, constraints and the database execution plan.");
        CORE.put("Computer Networks", "Networking is layered: application protocols depend on transport, addressing, routing and link-level delivery, and each layer creates different failure signals.");
        CORE.put("Operating Systems", "An operating system manages CPU, memory, storage and devices while isolating processes and coordinating shared resources.");
        CORE.put("System Design Fundamentals", "System design balances functionality, latency, throughput, consistency, cost and reliability under real traffic and failure conditions.");
        CORE.put("Git & GitHub", "Git records snapshots and references, allowing developers to make small reversible changes and collaborate by exchanging commit histories.");
        CORE.put("JavaScript & React", "JavaScript provides the runtime behavior and React models UI as components driven by explicit state, props and events.");
        CORE.put("Spring Security & REST APIs", "A secure REST API authenticates the caller, authorizes the requested action, validates inputs and exposes deliberate data/error contracts.");
        CORE.put("Cloud & DevOps Fundamentals", "DevOps makes software delivery reproducible and observable by automating builds, environments, deployments, health checks and rollback.");
        CORE.put("Aptitude & Placement Prep", "Placement problems become easier when natural language is converted into variables, constraints, a mathematical/logical model and a sanity check.");
        EXAMPLE.put("HTML Mastery", "Create a course page section around the module. Choose semantic elements first, then add attributes, inspect the DOM and verify keyboard/accessibility behavior.");
        EXAMPLE.put("Java + Spring Boot", "Implement a small Java/Spring example around the module. Define input/output, write a clean baseline, then test a normal case and two edge cases.");
        EXAMPLE.put("DSA Fundamentals", "Solve a representative problem around the module. Write brute force first, identify the bottleneck, derive an invariant, optimize, then state O(time) and O(space).");
        EXAMPLE.put("Python Programming", "Write a small Python program around the module. Use clear built-ins, test normal and empty/invalid inputs, and explain any mutability or exception behavior.");
        EXAMPLE.put("SQL & Database Mastery", "Create a small schema/query exercise around the module. Predict the result first, run the query, inspect duplicates/NULLs, then explain the execution logic.");
        EXAMPLE.put("Computer Networks", "Trace a realistic client request around the module. Name protocol, address, port and state at each step, then identify one observable signal that would confirm your diagnosis.");
        EXAMPLE.put("Operating Systems", "Model a small OS scenario around the module. Draw the relevant state/resource transition, explain kernel responsibilities and identify a possible race or resource failure.");
        EXAMPLE.put("System Design Fundamentals", "Design a learning-platform component around the module. Start with requirements and a rough load estimate, choose a simple architecture, then find its first bottleneck and failure mode.");
        EXAMPLE.put("Git & GitHub", "Use a temporary repository to practice the module. Make small inspectable commits, review diff/history after each operation, and record a safe recovery path for one mistake.");
        EXAMPLE.put("JavaScript & React", "Build a focused UI interaction around the module. Keep state ownership explicit, handle loading/error/empty states and test repeated renders plus keyboard interaction.");
        EXAMPLE.put("Spring Security & REST APIs", "Implement or review one API path around the module. Trace HTTP boundary, authentication, authorization, validation, service and persistence before returning the response.");
        EXAMPLE.put("Cloud & DevOps Fundamentals", "Automate a small delivery workflow around the module. Make configuration reproducible, add a health check and document how to roll back safely.");
        EXAMPLE.put("Aptitude & Placement Prep", "Solve a placement-style problem around the module. Translate wording into variables/equations, estimate first, calculate carefully, then verify with an alternate check.");
        PRACTICE.put("HTML Mastery", "Build a small accessible course section and validate heading order, labels, links, keyboard focus and responsive behavior.");
        PRACTICE.put("Java + Spring Boot", "Write a small method or endpoint, add validation, test normal/edge/error cases and explain why the layer boundaries are correct.");
        PRACTICE.put("DSA Fundamentals", "Solve one timed problem, record brute force and optimized approaches, prove the key invariant and test adversarial inputs.");
        PRACTICE.put("Python Programming", "Create a small utility, add clear functions and error handling, test it, then refactor one repeated or unclear part.");
        PRACTICE.put("SQL & Database Mastery", "Write 3 queries for the module, verify expected rows, inspect the plan when relevant and explain any index/transaction trade-off.");
        PRACTICE.put("Computer Networks", "Use a network diagnostic thought experiment or local tool to identify where a request fails and what evidence would distinguish each layer.");
        PRACTICE.put("Operating Systems", "Trace a process/thread/resource scenario, identify the state transitions, then explain how you would reproduce and diagnose the failure.");
        PRACTICE.put("System Design Fundamentals", "Produce a one-page design with requirements, estimates, APIs, data flow, bottleneck, failure mode and one measurable SLO.");
        PRACTICE.put("Git & GitHub", "Create a branch, make focused commits, open a review, resolve a conflict safely and verify the final diff before merging.");
        PRACTICE.put("JavaScript & React", "Implement the UI behavior, include loading/error/empty states, add keyboard-accessible interaction and test a repeated-update case.");
        PRACTICE.put("Spring Security & REST APIs", "Secure one endpoint end-to-end, test unauthenticated/forbidden/valid requests and verify that ownership comes from the authenticated identity.");
        PRACTICE.put("Cloud & DevOps Fundamentals", "Package a small service, automate build/deploy, add health/metrics and perform a rollback drill.");
        PRACTICE.put("Aptitude & Placement Prep", "Solve 5 timed questions, classify the pattern, explain one wrong answer and derive a faster verification method.");
        REFERENCE.put("HTML Mastery", "semantic element -> attributes -> DOM -> browser/accessibility check");
        REFERENCE.put("Java + Spring Boot", "input -> typed model -> validation -> service -> persistence/HTTP response");
        REFERENCE.put("DSA Fundamentals", "constraints -> brute force -> bottleneck -> invariant -> optimized algorithm -> complexity");
        REFERENCE.put("Python Programming", "input -> function -> data structure -> exception handling -> test");
        REFERENCE.put("SQL & Database Mastery", "business question -> relational operations -> query -> result validation -> execution plan");
        REFERENCE.put("Computer Networks", "application -> protocol -> address/port -> transport -> IP -> link -> response");
        REFERENCE.put("Operating Systems", "process/thread -> system call -> kernel resource -> state change -> synchronization");
        REFERENCE.put("System Design Fundamentals", "requirements -> estimate -> API -> data -> bottleneck -> scaling -> failure handling");
        REFERENCE.put("Git & GitHub", "working tree -> staging -> commit -> branch -> remote -> review -> release");
        REFERENCE.put("JavaScript & React", "event/data -> state -> render -> effect/network -> loading/error -> accessible UI");
        REFERENCE.put("Spring Security & REST APIs", "HTTP request -> CORS -> authentication -> authorization -> validation -> service -> response");
        REFERENCE.put("Cloud & DevOps Fundamentals", "source -> build -> artifact -> deploy -> health check -> metrics -> rollback");
        REFERENCE.put("Aptitude & Placement Prep", "words -> variables -> model -> estimate -> calculation -> sanity check");
    }
}