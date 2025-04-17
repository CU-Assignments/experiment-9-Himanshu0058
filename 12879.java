import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration as HibernateConfiguration;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

// ---------------------------------------
// Easy Level: Spring DI with Java-based Configuration
// ---------------------------------------

class Course {
    private String courseName;
    private int duration;

    public Course(String courseName, int duration) {
        this.courseName = courseName;
        this.duration = duration;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Course [courseName=" + courseName + ", duration=" + duration + " months]";
    }
}

class Student {
    private String name;
    private Course course;

    public Student(String name, Course course) {
        this.name = name;
        this.course = course;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "Student [name=" + name + ", course=" + course + "]";
    }
}

@Configuration
class AppConfig {

    @Bean
    public Course course() {
        return new Course("Java Programming", 6);
    }

    @Bean
    public Student student() {
        return new Student("John Doe", course());
    }
}

public class MainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        Student student = context.getBean(Student.class);
        System.out.println(student);

        context.close();
    }
}

// ---------------------------------------
// Medium Level: Hibernate CRUD Operations
// ---------------------------------------

@Entity
class StudentEntity {

    @Id
    private int id;
    private String name;
    private int age;

    public StudentEntity() {}

    public StudentEntity(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "StudentEntity [id=" + id + ", name=" + name + ", age=" + age + "]";
    }
}

class StudentDAO {

    private static SessionFactory factory;

    static {
        factory = new HibernateConfiguration().configure("hibernate.cfg.xml").addAnnotatedClass(StudentEntity.class).buildSessionFactory();
    }

    public void saveStudent(StudentEntity student) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        session.save(student);
        session.getTransaction().commit();
    }

    public StudentEntity getStudent(int id) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        StudentEntity student = session.get(StudentEntity.class, id);
        session.getTransaction().commit();
        return student;
    }

    public void updateStudent(StudentEntity student) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        session.update(student);
        session.getTransaction().commit();
    }

    public void deleteStudent(int id) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        StudentEntity student = session.get(StudentEntity.class, id);
        if (student != null) {
            session.delete(student);
        }
        session.getTransaction().commit();
    }
}

// ---------------------------------------
// Hard Level: Spring + Hibernate Transaction Management
// ---------------------------------------

@Entity
class Account {
    @Id
    private int id;
    private String accountHolder;
    private double balance;

    public Account() {}

    public Account(int id, String accountHolder, double balance) {
        this.id = id;
        this.accountHolder = accountHolder;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

@Entity
class Transaction {
    @Id
    private int id;
    private int fromAccountId;
    private int toAccountId;
    private double amount;

    public Transaction() {}

    public Transaction(int fromAccountId, int toAccountId, double amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    public int getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(int fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public int getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(int toAccountId) {
        this.toAccountId = toAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

class AccountDAO {
    private static SessionFactory factory;

    static {
        factory = new HibernateConfiguration().configure("hibernate.cfg.xml").addAnnotatedClass(Account.class).addAnnotatedClass(Transaction.class).buildSessionFactory();
    }

    public Account getAccount(int id) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        Account account = session.get(Account.class, id);
        session.getTransaction().commit();
        return account;
    }

    public void updateAccount(Account account) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        session.update(account);
        session.getTransaction().commit();
    }

    public void saveTransaction(Transaction transaction) {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        session.save(transaction);
        session.getTransaction().commit();
    }
}

class BankService {

    private AccountDAO accountDAO;

    public BankService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Transactional
    public void transferMoney(int fromAccountId, int toAccountId, double amount) {
        Account fromAccount = accountDAO.getAccount(fromAccountId);
        Account toAccount = accountDAO.getAccount(toAccountId);

        if (fromAccount.getBalance() >= amount) {
            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);

            Transaction transaction = new Transaction(fromAccountId, toAccountId, amount);
            accountDAO.saveTransaction(transaction);
        } else {
            throw new RuntimeException("Insufficient funds for transfer");
        }
    }
}

public class MainApp {
    public static void main(String[] args) {
        // Easy Level: Spring DI example
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Student student = context.getBean(Student.class);
        System.out.println(student);
        context.close();

        // Medium Level: Hibernate CRUD example
        StudentDAO studentDAO = new StudentDAO();
        StudentEntity student1 = new StudentEntity(1, "John", 22);
        studentDAO.saveStudent(student1);
        System.out.println(studentDAO.getStudent(1));

        // Hard Level: Spring + Hibernate Transaction example
        AccountDAO accountDAO = new AccountDAO();
        BankService bankService = new BankService(accountDAO);
        
        try {
            bankService.transferMoney(1, 2, 500);
            System.out.println("Transaction successful!");
        } catch (RuntimeException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }
}
