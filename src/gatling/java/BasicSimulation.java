// Gatling Java DSL 임포트
// - core: 시나리오, 인젝션(부하 주입), check 등 공통 DSL
// - http: HTTP 프로토콜/요청 DSL
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import java.time.Duration;

public class BasicSimulation extends Simulation {

    // 1) HTTP 프로토콜 설정: 모든 요청에 공통 적용되는 baseUrl/헤더 등
    HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080")          // 부하 테스트 대상 서버 주소
        .acceptHeader("application/json");

    // 2) 시나리오 정의: 가상 유저 한 명이 수행할 동작
    ScenarioBuilder scn = scenario("Basic Test")
        .exec(
            http("API 요청")
                .get("/api/booths")
                .check(status().is(200))
        );

    // 3) 부하 주입(injection) 설정
    {
        setUp(
            // ⚠️ 핵심 수정 포인트:
            // Gatling 3.14.x 부터 .inject() 가 .injectOpen() / .injectClosed() 로 분리됨.
            // rampUsersPerSec / constantUsersPerSec 같은 "도착률(arrival rate)" 기반은
            // 오픈 모델이므로 injectOpen() 을 사용한다.
            scn.injectOpen(
                // 30초 동안 초당 유저 수를 1 → 10 으로 점진적으로 증가(워밍업)
                rampUsersPerSec(1).to(10).during(Duration.ofSeconds(30)),
                // 이후 60초 동안 초당 50명의 유저를 일정하게 유지
                constantUsersPerSec(50).during(Duration.ofSeconds(60))
            )
        ).protocols(httpProtocol);
    }
}
