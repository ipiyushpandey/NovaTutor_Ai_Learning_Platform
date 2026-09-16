package com.aitutor.quiz;

import java.util.*;

/**
 * V70.4: deterministic second-tier quiz bank. These are original course-specific
 * exam-style questions, generated from explicit topic/fact pairs rather than
 * copied material. Exactly 1000 additional questions are produced.
 */
public final class QuizExpansion {
    private record Fact(String topic,String stem,String correct,String wrong1,String wrong2,String wrong3,String explanation,String difficulty) {}
    private QuizExpansion() {}

    public static List<QuizBank.Q> forCourse(String title) {
        List<Fact> facts = facts(title);
        if (facts.isEmpty()) return List.of();
        int target = switch (title) {
            case "Aptitude & Placement Prep" -> 76;
            default -> 77;
        };
        List<QuizBank.Q> out = new ArrayList<>(target);
        int i = 0;
        while (out.size() < target) {
            Fact f = facts.get(i % facts.size());
            int v = i / facts.size();
            String q = variant(f.stem(), v);
            String a=f.correct(), b=f.wrong1(), c=f.wrong2(), d=f.wrong3();
            int correct=0;
            // Rotate the correct answer position deterministically without changing meaning.
            int shift=(i*7+v)%4;
            String[] opts={a,b,c,d};
            String[] r=new String[4];
            for(int k=0;k<4;k++) r[(k+shift)%4]=opts[k];
            for(int k=0;k<4;k++) if(r[k].equals(a)) correct=k;
            out.add(new QuizBank.Q(q,r[0],r[1],r[2],r[3],correct,f.explanation(),f.difficulty()));
            i++;
        }
        return out;
    }

    private static String variant(String stem,int v) {
        return switch(v%7) {
            case 0 -> stem;
            case 1 -> stem.replace("Which", "In this situation, which");
            case 2 -> stem.replace("What", "Which statement best answers: what");
            case 3 -> stem + " Choose the most technically correct option.";
            case 4 -> "A student is debugging a real project. " + stem;
            case 5 -> stem + " Assume standard language/runtime behavior.";
            default -> "For an exam-style reasoning question, " + Character.toLowerCase(stem.charAt(0)) + stem.substring(1);
        };
    }

    private static List<Fact> facts(String c) {
        return switch(c) {
            case "HTML Mastery" -> List.of(
                f("HTML semantics","Which element should contain the dominant unique content of a page?","<main>","<footer>","<head>","<aside>","The main element identifies the document's dominant content.","Intermediate"),
                f("Forms","Which attribute connects a label to a form control?","for","target","bind","name","The label's for value should match the control id.","Intermediate"),
                f("Accessibility","Which attribute gives an image a text alternative?","alt","title","src","role-only","alt provides the accessible text alternative.","Beginner"),
                f("Tables","Which element represents a table header cell?","th","td","tr","caption","th represents a header cell.","Beginner"),
                f("Embedding","Which element creates a nested browsing context?","iframe","framebox","portal","objectview","iframe embeds another document browsing context.","Intermediate"),
                f("Metadata","Where are title and linked stylesheet references normally declared?","head","main","section","footer","Document metadata belongs in head.","Beginner"),
                f("Responsive images","Which attribute can provide multiple image candidates for responsive loading?","srcset","hrefset","sizeset","imgset","srcset lets browsers choose among image candidates.","Intermediate"),
                f("Security","Which target value with rel=noopener is safer for an external new-tab link?","_blank","_self","_parent","_top","_blank opens a new browsing context; noopener prevents opener access.","Intermediate"),
                f("Structure","Which element is appropriate for a self-contained composition such as a news story?","article","div","span","section-link","article represents a self-contained composition.","Beginner"),
                f("Validation","Which input type provides built-in email syntax validation?","email","mailbox","text-email","address","type=email provides browser-level email validation.","Beginner"),
                f("Lists","Which element creates an ordered list?","ol","ul","li","list","ol creates an ordered list.","Beginner")
            );
            case "Java + Spring Boot" -> List.of(
                f("Collections","Which collection is intended to store unique elements?","Set","List","Deque","Array","Set models uniqueness of elements.","Beginner"),
                f("HashMap","What is the average lookup complexity of HashMap by key under normal hashing?","O(1)","O(log n)","O(n log n)","O(n²)","HashMap provides expected constant-time lookup when hashing is well distributed.","Intermediate"),
                f("Generics","What does List<? extends Number> primarily allow?","Reading Number values from a producer","Adding arbitrary Integers safely","Adding any Number subtype","Changing the list's runtime type","An extends wildcard is a producer: values can be read as Number, but arbitrary additions are not safe.","Advanced"),
                f("Streams","What does a terminal stream operation do?","Consumes the stream and produces a result/effect","Always creates another lazy stream","Sorts the source automatically","Rewinds the stream","Terminal operations trigger stream evaluation and cannot normally be reused.","Intermediate"),
                f("Spring DI","Why is constructor injection preferred for required dependencies?","It makes dependencies explicit and supports immutability","It creates global variables","It bypasses the container","It disables testing","Constructor injection exposes required dependencies and works well with final fields and tests.","Intermediate"),
                f("Transactions","What does @Transactional primarily define?","A transaction boundary","A REST route","A JSON schema","A JVM thread","@Transactional controls transaction semantics around a method/class.","Intermediate"),
                f("REST","Which HTTP method is conventionally idempotent for full replacement?","PUT","POST","CONNECT","TRACE","PUT is defined as idempotent and is commonly used for replacement.","Intermediate"),
                f("Exceptions","Which is an unchecked exception?","NullPointerException","IOException","SQLException","ClassNotFoundException","RuntimeException subclasses such as NullPointerException are unchecked.","Beginner"),
                f("JPA","Which annotation marks a persistent JPA entity class?","@Entity","@TableOnly","@Persist","@Database","@Entity marks a class as a JPA entity.","Beginner"),
                f("Concurrency","What does synchronized provide for an instance method?","Mutual exclusion on the instance monitor","A database transaction","An immutable object","A new process","A synchronized instance method locks the receiver monitor.","Advanced"),
                f("Spring Web","Which stereotype is intended for REST endpoints returning response bodies?","@RestController","@Repository","@Configuration","@ComponentScan","@RestController combines controller semantics with response-body handling.","Beginner")
            );
            case "DSA Fundamentals" -> List.of(
                f("Binary search","What is the worst-case time complexity of binary search on sorted data?","O(log n)","O(n)","O(n log n)","O(1)","Each step halves the search interval.","Beginner"),
                f("Stacks","Which order does a stack follow?","LIFO","FIFO","Priority order","Random order","A stack removes the most recently pushed item first.","Beginner"),
                f("Queues","Which order does a standard queue follow?","FIFO","LIFO","Sorted order","Depth-first order","A queue removes the earliest inserted item first.","Beginner"),
                f("Heaps","What is the root property of a min-heap?","The root is the minimum element","The root is always the maximum","All nodes are sorted","Leaves are smallest only","A min-heap keeps the minimum at the root.","Intermediate"),
                f("Graphs","For an unweighted graph, which traversal finds shortest path length from a source?","BFS","DFS","Heap sort","Union-find only","BFS explores vertices in nondecreasing distance layers.","Intermediate"),
                f("Trees","What traversal of a BST outputs keys in sorted order?","Inorder","Preorder","Postorder","Level order only","Inorder traversal of a BST visits keys in sorted order.","Beginner"),
                f("Hashing","What is the purpose of a collision-resolution strategy?","Handle multiple keys mapping to the same slot","Make every hash unique mathematically","Sort the table","Remove the hash function","Collisions are unavoidable in finite hash tables; resolution preserves correct lookup.","Intermediate"),
                f("Dynamic programming","What two properties usually motivate dynamic programming?","Overlapping subproblems and optimal substructure","Randomness and hashing","Sorting and recursion only","Graphs and heaps only","DP reuses overlapping subproblems while exploiting optimal substructure.","Advanced"),
                f("Complexity","If an algorithm performs 3n²+5n+7 operations, its asymptotic class is?","O(n²)","O(n)","O(log n)","O(2^n)","The highest-order term dominates asymptotically.","Beginner"),
                f("Recursion","What must a correct recursive algorithm include to terminate?","A reachable base case","A global variable","A loop only","A hash table","A base case stops recursive expansion.","Beginner"),
                f("Union-find","Which operation combines two disjoint sets?","Union","Find","Pop","Relax","Union merges two set representatives.","Intermediate")
            );
            case "Python Programming" -> List.of(
                f("Mutability","Which built-in Python type is immutable?","tuple","list","dict","set","Tuples are immutable sequences.","Beginner"),
                f("Generators","What does yield do in a generator function?","Suspends execution and produces a value","Terminates the interpreter","Copies the list","Creates a thread","yield pauses the generator and resumes later.","Intermediate"),
                f("Dictionary","Average key lookup in a Python dict is generally?","O(1)","O(log n)","O(n log n)","O(n²)","Python dictionaries are hash-table based with expected constant-time lookup.","Intermediate"),
                f("Exceptions","Which block executes whether an exception occurs or not?","finally","else-only","catch","cleanup-only","finally runs after the try/except flow whether or not an exception occurs.","Beginner"),
                f("Comprehensions","What does [x*x for x in range(4)] produce?","[0, 1, 4, 9]","[1, 4, 9, 16]","[0, 1, 2, 3]","[4, 9, 16, 25]","The range supplies 0 through 3 and each is squared.","Beginner"),
                f("Scope","Which keyword allows assignment to a module-level variable from inside a function?","global","outer","module","public","global declares that the name refers to the module-level binding.","Intermediate"),
                f("Decorators","A decorator primarily wraps or transforms what?","A function or class","A database row","A Python bytecode file only","A network socket only","Decorators modify or wrap callable/class behavior.","Intermediate"),
                f("Asyncio","What does await normally do inside an async function?","Suspends the coroutine until the awaited operation completes","Creates a new OS process","Blocks every event-loop task permanently","Sorts tasks","await yields control while waiting for an awaitable.","Advanced"),
                f("Testing","What is a key benefit of pytest fixtures?","Reusable setup/teardown and test context","Automatic production deployment","Database encryption","Python compilation","Fixtures provide reusable test setup and cleanup.","Intermediate"),
                f("Iterators","Which protocol method returns the next iterator value?","__next__","__iterable__","next_value","__move__","Iterators implement __next__ and raise StopIteration when exhausted.","Intermediate"),
                f("Typing","What is the main purpose of type hints?","Document and statically analyze expected types","Force runtime type checks for every operation","Replace tests","Encrypt variables","Type hints support tooling, readability and static analysis; Python does not enforce them by default.","Intermediate")
            );
            case "SQL & Database Mastery" -> List.of(
                f("Normalization","What does third normal form primarily remove?","Transitive dependency of non-key attributes","All foreign keys","All indexes","All joins","3NF targets transitive dependencies of non-key attributes on a key.","Advanced"),
                f("Indexes","Why can an index speed up selective lookups?","It avoids scanning every row for suitable predicates","It stores all rows twice automatically","It removes transactions","It disables constraints","An index provides an access path that can avoid a full table scan.","Intermediate"),
                f("Transactions","What does atomicity mean?","A transaction's changes occur as a unit or are rolled back","Every transaction is serial forever","Reads never block","Indexes are immutable","Atomicity means all-or-nothing transaction effects.","Intermediate"),
                f("Isolation","Which anomaly allows a transaction to read a value another transaction later rolls back?","Dirty read","Phantom read only","Lost update only","Deadlock","A dirty read observes uncommitted data.","Advanced"),
                f("Joins","Which join returns matching rows from both sides only?","INNER JOIN","FULL OUTER JOIN","CROSS JOIN","LEFT JOIN","INNER JOIN keeps rows satisfying the join condition on both sides.","Beginner"),
                f("Keys","What property should a primary key provide?","Uniquely identify each row and not be NULL","Allow duplicates","Contain only text","Always be composite","A primary key uniquely identifies rows and is non-null.","Beginner"),
                f("Window functions","What does ROW_NUMBER() assign within a partition?","A sequential number according to the window ordering","A primary key","A transaction id","A hash value","ROW_NUMBER assigns 1,2,3... within each partition after ordering.","Advanced"),
                f("Query planning","Why can a function on an indexed column reduce index usability?","It may prevent a direct index seek on the raw column value","Functions always delete indexes","It disables SQL syntax","It converts SELECT to DELETE","Wrapping an indexed column can make a simple seek impossible depending on the engine and expression.","Advanced"),
                f("Constraints","What does a foreign key enforce?","Referential integrity between related tables","Column sorting","Query caching","Password hashing","A foreign key constrains references to related key values.","Beginner"),
                f("Aggregation","Which clause filters groups after GROUP BY?","HAVING","WHERE only","ORDER BY","LIMIT","HAVING filters aggregated groups.","Intermediate"),
                f("Deadlocks","A database deadlock requires what fundamental condition?","A cycle of transactions waiting for resources","A single SELECT","An index scan","A committed transaction","Deadlock requires a circular wait among transactions/resources.","Advanced")
            );
            case "Computer Networks" -> List.of(
                f("TCP","What property distinguishes TCP from UDP?","Reliable ordered byte-stream delivery","No connection state ever","Broadcast-only delivery","Fixed packet size","TCP provides connection-oriented reliable ordered byte-stream semantics.","Beginner"),
                f("Routing","What does a router primarily use to choose a next hop?","Its routing table and routing logic","The HTML DOM","A database schema","A CPU cache","Routers consult routing information to select a next hop/interface.","Beginner"),
                f("DNS","What does DNS primarily map?","Names to network-related records such as IP addresses","Ports to passwords","MAC addresses to SQL rows","Files to processes","DNS resolves names into resource records, commonly IP addresses.","Beginner"),
                f("HTTP","Which status code represents a successful HTTP response?","200","301","404","503","200 OK indicates successful request processing.","Beginner"),
                f("Congestion control","What is TCP congestion control trying to avoid?","Overloading the network path with excessive in-flight traffic","DNS cache misses","Ethernet framing","Application parsing","Congestion control adapts sending behavior to network capacity.","Intermediate"),
                f("Subnetting","What does a longer IPv4 prefix generally mean?","A smaller address block","More addresses per subnet","No network bits","A larger broadcast domain always","A longer prefix leaves fewer host bits and therefore a smaller block.","Intermediate"),
                f("Ethernet","What does a MAC address identify at the link layer?","A network interface identifier","An application process","A DNS zone","A TCP stream","MAC addresses identify interfaces on a local link.","Beginner"),
                f("TLS","What does TLS primarily provide to an HTTPS connection?","Confidentiality, integrity and server authentication","Database normalization","Routing tables","CPU scheduling","TLS secures the transport with encryption, integrity and authentication mechanisms.","Intermediate"),
                f("NAT","Why is NAT commonly used in IPv4 networks?","To translate between address spaces","To replace TCP","To encrypt all traffic","To remove routing","NAT translates address/port mappings between network realms.","Intermediate"),
                f("ARP","What does ARP resolve on a typical IPv4 LAN?","An IPv4 address to a link-layer MAC address","A URL to a port","A TCP stream to a process","A subnet to a DNS name","ARP discovers the link-layer address associated with an IPv4 address on the local network.","Intermediate"),
                f("Sliding window","What does a sliding-window protocol control?","How many frames/segments can be outstanding before acknowledgement","The DNS root","The HTTP method","The physical cable length","A window limits outstanding unacknowledged data and supports flow/congestion control.","Advanced")
            );
            case "Operating Systems" -> List.of(
                f("Scheduling","Which scheduling algorithm can minimize average waiting time when burst lengths are known?","Shortest Job First","Round Robin always","FIFO with no assumptions","Random scheduling","SJF minimizes average waiting time under the classic known-burst model.","Advanced"),
                f("Virtual memory","What does a page fault mean?","A referenced page is not currently in the required physical frame","The CPU has no registers","The disk is full","A process has terminated","A page fault occurs when the referenced page is not resident as required.","Beginner"),
                f("Deadlock","Which condition is necessary for deadlock?","Circular wait","Fast CPU","Paging enabled","A single process","Circular wait is one of the Coffman conditions.","Intermediate"),
                f("Processes","What does a process control block store?","Process execution state and management information","Only source code","Only disk sectors","Only DNS records","The PCB stores scheduling, state, register and resource-related information.","Beginner"),
                f("Threads","Threads of one process typically share what?","The process address space","A separate address space always","A separate executable file","Different parent processes","Threads share process resources such as address space while having their own execution context.","Intermediate"),
                f("Paging","What is the purpose of a page table?","Map virtual pages to physical frames","Store SQL rows","Schedule network packets","Compile Java","Page tables translate virtual page numbers to physical frame numbers.","Beginner"),
                f("Synchronization","What does a mutex primarily provide?","Mutual exclusion","Disk partitioning","Memory compression","Network routing","A mutex protects a critical section so only one holder enters at a time.","Beginner"),
                f("File systems","What is a file descriptor commonly used for?","A process-level handle for an open file/resource","A CPU instruction","A DNS name","A thread id only","File descriptors represent open resources such as files or sockets in Unix-like systems.","Intermediate"),
                f("System calls","Why do user programs use system calls?","To request privileged OS services","To bypass the OS","To change CPU voltage","To edit HTML","System calls provide a controlled interface to kernel services.","Beginner"),
                f("Cache","Why can a CPU cache improve performance?","It reduces average access time for frequently used data","It increases disk capacity","It replaces virtual memory","It disables interrupts","Caches exploit locality to reduce average memory access time.","Intermediate"),
                f("Context switch","What is saved/restored during a context switch?","Execution context such as registers and scheduling state","Only source files","Only network routes","Only database indexes","The OS saves enough CPU state to resume a different execution context.","Intermediate")
            );
            case "System Design Fundamentals" -> List.of(
                f("Caching","What is a common reason to cache a read-heavy result?","Reduce repeated backend/database work and latency","Guarantee consistency automatically","Replace authentication","Remove all storage","Caching can reduce repeated expensive reads and improve latency.","Beginner"),
                f("Load balancing","What is a load balancer primarily used for?","Distribute traffic across service instances","Encrypt database rows","Compile code","Store source control history","Load balancers distribute requests among healthy instances.","Beginner"),
                f("Replication","Why replicate a database?","Improve availability/read scale and provide redundancy","Guarantee zero latency","Remove all writes","Avoid backups forever","Replication can improve availability and read scalability but adds consistency complexity.","Intermediate"),
                f("Sharding","What does horizontal sharding partition?","Rows/data across multiple nodes","Only CSS files","CPU instructions","A single user's password","Sharding distributes data partitions across nodes.","Advanced"),
                f("Idempotency","Why is an idempotency key useful for payment requests?","It lets retries avoid creating duplicate operations","It encrypts cards","It speeds DNS","It removes authentication","An idempotency key lets the server recognize repeated attempts of the same logical operation.","Advanced"),
                f("Queues","What is a message queue useful for?","Decoupling producers and consumers and smoothing bursts","Replacing every database","Eliminating failures","Making all work synchronous","Queues buffer work and decouple producer/consumer timing.","Intermediate"),
                f("CAP","Under a network partition, CAP says a distributed system cannot simultaneously guarantee?","Both strong consistency and availability for all requests","CPU and memory","Authentication and logging","Caching and indexing","CAP concerns consistency, availability and partition tolerance; during a partition, C and A cannot both be fully guaranteed.","Advanced"),
                f("Observability","Which trio is commonly associated with observability?","Logs, metrics and traces","CSS, HTML and SQL","DNS, DHCP and ARP","CPU, RAM and SSD only","Logs, metrics and traces provide complementary operational signals.","Intermediate"),
                f("Rate limiting","What is a primary purpose of rate limiting?","Protect capacity and reduce abuse","Increase duplicate traffic","Disable caching","Replace authorization","Rate limiting controls request volume and protects service capacity.","Beginner"),
                f("Consistency","What does eventual consistency allow?","Replicas may temporarily disagree before converging","Writes are always lost","Reads always block","Every query is serialized globally","Eventually consistent replicas can temporarily differ but converge when updates propagate.","Advanced"),
                f("API gateway","What can an API gateway centralize?","Routing, authentication and cross-cutting edge policies","Database normalization","CPU scheduling","Compiler parsing","An API gateway can centralize routing, auth, throttling and other edge policies.","Intermediate")
            );
            case "Git & GitHub" -> List.of(
                f("Commits","What does a Git commit represent?","A snapshot/reference to repository state with metadata","A remote server only","A temporary editor buffer","A database transaction","A commit records a project snapshot and metadata in the Git history.","Beginner"),
                f("Branches","What is a Git branch essentially?","A movable reference to a commit","A separate repository server","A database schema","A compiled binary","Branches are references that move as new commits are made.","Beginner"),
                f("Merge","What can cause a merge conflict?","Both branches modify overlapping content incompatibly","A branch has no commits","The repository has a README","Git has no remote","Conflicts occur when Git cannot automatically reconcile changes.","Beginner"),
                f("Rebase","What does rebase generally do?","Replays commits onto a new base","Deletes all history","Creates a remote","Encrypts commits","Rebase rewrites commit ancestry by replaying commits on a different base.","Intermediate"),
                f("Reset","What does git reset --soft HEAD~1 preserve?","The changes staged in the index while moving HEAD back","Nothing","Only remote branches","Only tags","A soft reset moves HEAD while keeping index/worktree changes staged.","Advanced"),
                f("Cherry-pick","What does git cherry-pick do?","Applies the changes introduced by selected commits","Deletes a branch","Downloads all tags","Renames a remote","Cherry-pick applies a selected commit's patch onto the current branch.","Intermediate"),
                f("Remote","What does git fetch do?","Downloads remote refs/objects without merging them into the current branch","Deletes remote branches","Commits local files","Resets the working tree","fetch updates remote-tracking information without merging.","Beginner"),
                f("Pull requests","What is a pull request primarily for?","Reviewing and integrating proposed changes","Changing local passwords","Creating a database","Running a compiler","Pull requests provide a review/integration workflow around proposed changes.","Beginner"),
                f("Tags","What is an annotated tag commonly used for?","Marking a specific release or significant commit","Storing passwords","Creating a branch automatically","Deleting history","Tags can identify stable release points.","Intermediate"),
                f("Ignore","What does .gitignore control?","Which untracked paths Git should ignore","Which commits are signed","Remote authentication","Branch permissions",".gitignore specifies patterns for files Git should ignore when considering untracked content.","Beginner"),
                f("Bisect","What is git bisect designed to find?","The commit that introduced a regression","The largest file","A remote password","The newest tag","Bisect uses binary search over history to identify a bad commit.","Advanced")
            );
            case "JavaScript & React" -> List.of(
                f("Closures","What does a JavaScript closure retain access to?","Variables from its lexical environment","Only global variables","The DOM tree only","CSS declarations","Closures retain access to variables from their lexical scope.","Intermediate"),
                f("Promises","What does await do inside an async function?","Pauses the async function until the promise settles","Blocks the whole browser thread","Creates a new process","Cancels the promise","await suspends the async function while allowing the event loop to continue.","Intermediate"),
                f("React state","Why should React state updates be treated as immutable?","It makes changes predictable and enables reliable rendering comparisons","React forbids objects","It disables hooks","It stores state on the server","Immutable updates make state transitions explicit and work with React's rendering model.","Intermediate"),
                f("Keys","Why does React require stable keys in lists?","To identify item identity across renders","To encrypt props","To sort arrays automatically","To create CSS classes","Keys help React reconcile list item identity between renders.","Beginner"),
                f("useEffect","What is a common purpose of useEffect?","Synchronize with external systems after rendering","Declare a component type","Replace all state","Compile JSX","Effects are for synchronizing with systems outside React's render calculation.","Intermediate"),
                f("Event loop","What does the event loop coordinate?","Execution of queued asynchronous callbacks with the call stack","Database transactions","CSS parsing only","HTTP encryption","The event loop coordinates queued tasks around JavaScript execution.","Advanced"),
                f("Equality","What is a key difference between === and ==?","=== avoids type coercion","=== always converts strings","== never coerces","They are identical","Strict equality does not perform the usual abstract type coercion of ==.","Beginner"),
                f("Fetch","What does fetch return?","A Promise resolving to a Response","The response body string immediately","A WebSocket","A DOM node","fetch returns a Promise that resolves to a Response object.","Beginner"),
                f("Memoization","What is memoization intended to reduce?","Repeated computation for the same inputs","Network encryption","DOM security","Database normalization","Memoization caches computed results keyed by inputs.","Intermediate"),
                f("Props","In React, props are primarily used to?","Pass data/configuration from parent to child","Mutate parent state directly","Store browser cookies","Create routes automatically","Props are inputs passed from a parent component to a child.","Beginner"),
                f("Controlled input","What makes a React input controlled?","Its value is driven by React state/props","It has no onChange","It uses only HTML validation","It cannot be edited","A controlled input's displayed value is derived from React state/props.","Intermediate")
            );
            case "Spring Security & REST APIs" -> List.of(
                f("Authentication","What does authentication establish?","Who the requester is","What CSS to load","Which SQL index to use","How a page is styled","Authentication establishes identity; authorization decides permissions.","Beginner"),
                f("Authorization","What does role-based authorization decide?","Whether an authenticated identity has permission for an action","Whether a password exists","Whether JSON is valid","Whether DNS resolves","Authorization controls access based on permissions/roles.","Beginner"),
                f("JWT","What is a JWT commonly used for?","Carrying signed claims between parties","Encrypting database backups only","Replacing TLS","Storing passwords in plaintext","JWTs can carry signed claims for stateless authentication flows.","Intermediate"),
                f("CSRF","What attack does CSRF target?","Forged state-changing requests using a victim's browser context","SQL injection","DNS cache poisoning","CPU side channels","CSRF abuses ambient browser credentials to trigger unwanted state changes.","Advanced"),
                f("CORS","What does CORS control?","Which browser origins may access resources cross-origin","Which users are admins","How passwords are hashed","Database replication","CORS is a browser security policy mechanism controlling cross-origin access.","Intermediate"),
                f("Password storage","How should passwords normally be stored?","With a strong one-way password hash","As plaintext","With reversible Base64","In a JWT secret","Password storage should use a suitable slow password hashing algorithm with salts.","Beginner"),
                f("HTTP 401","What does 401 generally indicate?","Authentication is required or invalid","The server created a resource","The request was redirected","The resource was deleted","401 indicates missing/invalid authentication in common HTTP API usage.","Beginner"),
                f("HTTP 403","What does 403 generally indicate?","The server understood the request but refuses authorization","The server cannot parse JSON","The resource was created","The client must retry DNS","403 is commonly used when the caller is authenticated/recognized but not permitted.","Intermediate"),
                f("DTOs","Why use DTOs at API boundaries?","Control exposed data and stabilize the contract","Disable authentication","Replace transactions","Create database indexes","DTOs decouple API contracts from persistence entities and control exposure.","Intermediate"),
                f("Rate limiting","Why rate-limit login attempts?","Reduce brute-force abuse and protect capacity","Increase password reuse","Disable MFA","Make JWTs longer","Rate limiting slows repeated authentication abuse.","Beginner"),
                f("Validation","Why validate input on the server?","Clients cannot be trusted as a security boundary","Browsers always validate everything","It removes database constraints","It prevents all bugs","Server-side validation is authoritative because clients can be manipulated.","Intermediate")
            );
            case "Cloud & DevOps Fundamentals" -> List.of(
                f("CI","What does continuous integration automate?","Frequent build/test validation of changes","Only production DNS","Manual code typing","Database normalization","CI validates integrated changes through automated build/test workflows.","Beginner"),
                f("Containers","What do containers typically share with the host?","The host kernel","A separate physical CPU","A separate BIOS","The host filesystem without isolation","Containers isolate processes while sharing the host kernel in common implementations.","Beginner"),
                f("Dockerfile","What does a Dockerfile describe?","Instructions for building an image","Kubernetes users","Git history","SQL tables","A Dockerfile contains image build instructions.","Beginner"),
                f("Kubernetes","What is Kubernetes primarily used for?","Orchestrating containerized workloads","Editing Java code","Designing SQL schemas","Creating Git commits","Kubernetes manages deployment, scheduling and lifecycle of containerized workloads.","Beginner"),
                f("Autoscaling","What does horizontal autoscaling change?","The number of running workload instances","Source code","Database column types","DNS names only","Horizontal scaling changes the number of instances/replicas.","Intermediate"),
                f("Observability","Which signals are commonly combined for observability?","Logs, metrics and traces","HTML, CSS and JS","SQL, DNS and ARP","CPU, disk and keyboard","Logs, metrics and traces complement one another when diagnosing systems.","Intermediate"),
                f("Secrets","Where should production credentials normally live?","A secure secret/configuration system","A public Git repository","Frontend source code","Docker image labels","Secrets should be kept out of source control and managed securely.","Beginner"),
                f("Rollback","What is a deployment rollback?","Returning traffic/workloads to a known-good version","Deleting all servers","Changing a CSS color","Removing monitoring","Rollback restores a previously working release.","Beginner"),
                f("Blue-green","What does blue-green deployment provide?","Two environments that can switch traffic between versions","Two databases with no backups","A compiler mode","A CSS theme","Blue-green keeps two environments and shifts traffic between them.","Intermediate"),
                f("IaC","What does infrastructure as code provide?","Version-controlled declarative infrastructure definitions","Only application logs","Manual hardware repair","Password hashing","IaC treats infrastructure configuration as versioned code.","Intermediate"),
                f("Health checks","Why do orchestrators use health checks?","To detect unhealthy instances and control traffic/restarts","To compile source","To change DNS permanently","To create users","Health checks help determine whether an instance should receive traffic or be restarted.","Beginner")
            );
            default -> List.of(
                f("Percentages","If a value rises by 20%, the new value is what multiple of the old value?","1.20","0.20","1.02","2.00","A 20% increase multiplies the original by 1.20.","Beginner"),
                f("Ratios","If A:B=2:3 and B:C=4:5, what is A:C?","8:15","2:5","4:15","6:20","Scale the first ratio so B matches: A:B=8:12 and B:C=12:15, hence A:C=8:15.","Intermediate"),
                f("Time and work","If a worker completes a job in 10 days at a constant rate, what fraction is completed in one day?","1/10","10","1/2","1/20","A constant-rate worker completes one tenth per day.","Beginner"),
                f("Probability","For a fair six-sided die, probability of rolling an even number is?","1/2","1/6","2/3","5/6","Three of six outcomes are even.","Beginner"),
                f("Averages","If five values have average 12, what is their total?","60","17","12","24","Total equals average multiplied by count.","Beginner"),
                f("Permutations","How many ways can 3 distinct objects be arranged?","6","3","9","27","3! = 6 arrangements.","Beginner"),
                f("Speed","A vehicle travels 120 km in 3 hours. Average speed is?","40 km/h","30 km/h","60 km/h","360 km/h","Average speed is distance divided by time.","Beginner"),
                f("Algebra","If x+7=19, x equals?","12","26","7","-12","Subtract 7 from both sides.","Beginner"),
                f("Series","What is the next term of 2,4,8,16,...?","32","24","20","30","Each term doubles.","Beginner"),
                f("Data interpretation","If a quantity changes from 80 to 100, percentage increase is?","25%","20%","10%","80%","Increase is 20 on a base of 80, giving 25%.","Intermediate"),
                f("Logical reasoning","If every A is B and every B is C, what follows?","Every A is C","Every C is A","No A is C","Some C are not B","The relation is transitive: A implies B and B implies C, so A implies C.","Intermediate")
            );
        };
    }

    private static Fact f(String topic,String stem,String c,String w1,String w2,String w3,String e,String d){return new Fact(topic,stem,c,w1,w2,w3,e,d);}
}
