# Payment Bill Generation Solutions for Spring Boot 3

Here are several feasible solutions for generating payment bills on a regular basis:

## 1. **Spring Scheduler (Built-in Solution)** ⭐ Recommended for Simple Cases

```java
@Configuration
@EnableScheduling
public class SchedulerConfig {
}

@Service
@Slf4j
public class BillGenerationService {

    @Scheduled(cron = "0 0 0 1 * *") // Run at midnight on the 1st of every month
    public void generateMonthlyBills() {
        log.info("Starting monthly bill generation...");
        // Your bill generation logic
    }
    
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    public void generateWeeklyBills() {
        // Weekly billing logic
    }
}
```

---

## 2. **Quartz Scheduler** ⭐ Recommended for Complex Scheduling

### Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### Configuration
```java
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail billGenerationJobDetail() {
        return JobBuilder.newJob(BillGenerationJob.class)
                .withIdentity("billGenerationJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger billGenerationTrigger(JobDetail billGenerationJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(billGenerationJobDetail)
                .withIdentity("billGenerationTrigger")
                .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 0, 0))
                .build();
    }
}

@Component
public class BillGenerationJob extends QuartzJobBean {

    @Autowired
    private BillService billService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        billService.generateBills();
    }
}
```

### application.yml
```yaml
spring:
  quartz:
    job-store-type: jdbc  # Persists jobs to database
    jdbc:
      initialize-schema: always
    properties:
      org.quartz.jobStore.isClustered: true  # For multiple instances
```

---

## 3. **ShedLock (For Distributed Systems)** ⭐ Prevents Duplicate Execution

### Dependencies
```xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.10.0</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.10.0</version>
</dependency>
```

### Configuration
```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }
}

@Service
public class BillGenerationService {

    @Scheduled(cron = "0 0 0 1 * *")
    @SchedulerLock(name = "monthlyBillGeneration", 
                   lockAtLeastFor = "PT5M", 
                   lockAtMostFor = "PT30M")
    public void generateMonthlyBills() {
        // Safe to run in clustered environment
    }
}
```

### SQL Table
```sql
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
```

---

## 4. **Complete Bill Generation Architecture**

```java
@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String billNumber;
    private Long customerId;
    private BigDecimal amount;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private LocalDateTime generatedAt;
    
    @Enumerated(EnumType.STRING)
    private BillStatus status; // GENERATED, SENT, PAID, OVERDUE
}

@Service
@Transactional
@Slf4j
public class BillGenerationService {

    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;
    private final UsageService usageService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 2 1 * *") // 2 AM on 1st of each month
    @SchedulerLock(name = "monthlyBillGeneration")
    public void generateMonthlyBills() {
        LocalDate billingDate = LocalDate.now();
        LocalDate periodStart = billingDate.minusMonths(1).withDayOfMonth(1);
        LocalDate periodEnd = billingDate.minusDays(1);

        log.info("Generating bills for period: {} to {}", periodStart, periodEnd);

        List<Customer> activeCustomers = customerRepository.findAllActive();
        
        for (Customer customer : activeCustomers) {
            try {
                generateBillForCustomer(customer, periodStart, periodEnd);
            } catch (Exception e) {
                log.error("Failed to generate bill for customer: {}", customer.getId(), e);
                // Continue with next customer
            }
        }
    }

    private void generateBillForCustomer(Customer customer, 
                                          LocalDate periodStart, 
                                          LocalDate periodEnd) {
        // Calculate usage/charges
        BigDecimal amount = usageService.calculateCharges(customer, periodStart, periodEnd);
        
        Bill bill = Bill.builder()
                .billNumber(generateBillNumber())
                .customerId(customer.getId())
                .amount(amount)
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .generatedAt(LocalDateTime.now())
                .status(BillStatus.GENERATED)
                .build();
        
        billRepository.save(bill);
        
        // Send notification
        notificationService.sendBillNotification(customer, bill);
    }
}
```

---

## 5. **PDF Bill Generation with OpenPDF**

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>1.3.30</version>
</dependency>
```

```java
@Service
public class PdfBillGenerator {

    public byte[] generateBillPdf(Bill bill, Customer customer) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Add header
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        document.add(new Paragraph("INVOICE", headerFont));
        document.add(new Paragraph("Bill Number: " + bill.getBillNumber()));
        document.add(new Paragraph("Date: " + bill.getGeneratedAt()));
        
        // Add customer details
        document.add(new Paragraph("\nBill To:"));
        document.add(new Paragraph(customer.getName()));
        document.add(new Paragraph(customer.getAddress()));
        
        // Add billing details table
        PdfPTable table = new PdfPTable(2);
        table.addCell("Description");
        table.addCell("Amount");
        table.addCell("Service Charges");
        table.addCell(bill.getAmount().toString());
        document.add(table);
        
        // Add total
        document.add(new Paragraph("\nTotal: $" + bill.getAmount()));
        
        document.close();
        return baos.toByteArray();
    }
}
```

---

## Comparison Table

| Solution | Best For | Clustering | Persistence | Complexity |
|----------|----------|------------|-------------|------------|
| **Spring Scheduler** | Simple, single instance | ❌ | ❌ | Low |
| **Quartz** | Complex schedules, persistence | ✅ | ✅ | Medium |
| **ShedLock + Spring** | Distributed systems | ✅ | ✅ | Low |
| **Jobrunr** | Background jobs, dashboard | ✅ | ✅ | Medium |

---

## Recommendation

**For most cases, I recommend:**

1. **Single Instance**: Spring Scheduler (simplest)
2. **Multiple Instances/Microservices**: Spring Scheduler + ShedLock
3. **Complex Requirements**: Quartz with JDBC JobStore

Would you like me to elaborate on any specific solution or show you how to implement email notifications, retry mechanisms, or monitoring?