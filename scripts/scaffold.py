from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
services = {
    'auth': (8081, True, False),
    'user': (8082, True, False),
    'product': (8083, True, False),
    'order': (8084, True, False),
    'payment': (8085, True, True),
    'notification': (8086, True, True),
    'gateway': (8080, False, False),
}
for name, (port, database, rabbit) in services.items():
    folder = ROOT / (name + '-service' if name != 'gateway' else 'gateway')
    deps = ['common', 'spring-boot-starter-web', 'spring-boot-starter-actuator',
            'spring-boot-starter-validation', 'springdoc-openapi-starter-webmvc-ui',
            'spring-boot-starter-test']
    if database:
        deps += ['spring-boot-starter-jdbc', 'h2']
    if rabbit:
        deps += ['spring-boot-starter-amqp']
    if name == 'auth':
        deps += ['spring-security-crypto']
    xml = []
    for dep in deps:
        if dep == 'common':
            xml.append('<dependency><groupId>lab.store</groupId><artifactId>common</artifactId><version>${project.version}</version></dependency>')
        elif dep == 'springdoc-openapi-starter-webmvc-ui':
            xml.append('<dependency><groupId>org.springdoc</groupId><artifactId>' + dep + '</artifactId><version>${springdoc.version}</version></dependency>')
        elif dep == 'h2':
            xml.append('<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>')
        elif dep == 'spring-security-crypto':
            xml.append('<dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-crypto</artifactId></dependency>')
        elif dep == 'spring-boot-starter-test':
            xml.append('<dependency><groupId>org.springframework.boot</groupId><artifactId>' + dep + '</artifactId><scope>test</scope></dependency>')
        else:
            xml.append('<dependency><groupId>org.springframework.boot</groupId><artifactId>' + dep + '</artifactId></dependency>')
    (folder / 'pom.xml').parent.mkdir(parents=True, exist_ok=True)
    (folder / 'pom.xml').write_text('''<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"><modelVersion>4.0.0</modelVersion><parent><groupId>lab.store</groupId><artifactId>store-platform</artifactId><version>1.0.0</version></parent><artifactId>''' + folder.name + '''</artifactId><dependencies>''' + ''.join(xml) + '''</dependencies><build><plugins><plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId><executions><execution><goals><goal>repackage</goal></goals></execution></executions></plugin></plugins></build></project>''', encoding='utf-8')
    resources = folder / 'src/main/resources'
    resources.mkdir(parents=True, exist_ok=True)
    config = f'''server.port: ${{PORT:{port}}}
spring.application.name: {folder.name}
app.jwt-secret: ${{JWT_SECRET}}
app.internal-key: ${{INTERNAL_KEY}}
management.endpoints.web.exposure.include: health,info
logging.pattern.console: '%d{{yyyy-MM-dd HH:mm:ss.SSS}} service={folder.name} level=%-5level requestId=%X{{requestId}} %logger{{36}} - %msg%n'
'''
    if database:
        config += f'''spring.datasource.url: ${{DB_URL:jdbc:h2:file:./{name};DB_CLOSE_ON_EXIT=FALSE}}
spring.datasource.driver-class-name: org.h2.Driver
spring.datasource.username: sa
spring.sql.init.mode: always
'''
    if rabbit:
        config += '''spring.rabbitmq.host: ${RABBIT_HOST:localhost}
spring.rabbitmq.port: ${RABBIT_PORT:5672}
spring.rabbitmq.username: ${RABBIT_USER}
spring.rabbitmq.password: ${RABBIT_PASSWORD}
'''
    for peer, peer_port in [('auth',8081), ('user',8082), ('product',8083), ('order',8084), ('payment',8085), ('notification',8086)]:
        if peer != name:
            config += f'services.{peer}-url: ${{{peer.upper()}_URL:http://localhost:{peer_port}}}\n'
    if name == 'auth':
        config += 'app.admin-email: ${ADMIN_EMAIL:admin@example.com}\napp.admin-password: ${ADMIN_PASSWORD}\n'
    (resources / 'application.yml').write_text(config, encoding='utf-8')
    (folder / 'Dockerfile').write_text('FROM eclipse-temurin:21-jre\nWORKDIR /app\nCOPY target/*.jar app.jar\nENTRYPOINT ["java","-jar","/app/app.jar"]\n', encoding='utf-8')
